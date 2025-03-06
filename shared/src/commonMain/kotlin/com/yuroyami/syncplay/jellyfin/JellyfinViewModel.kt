package com.yuroyami.syncplay.jellyfin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.lyricist.Lyricist
import com.yuroyami.syncplay.lyricist.Stringies
import com.yuroyami.syncplay.models.JoinInfo
import com.yuroyami.syncplay.models.MediaFile
import com.yuroyami.syncplay.settings.DataStoreKeys
import com.yuroyami.syncplay.settings.valueSuspendingly
import com.yuroyami.syncplay.settings.writeValue
import com.yuroyami.syncplay.watchroom.viewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class JellyfinViewModel(
    private val repository: JellyfinRepository,
    private val onJoinRoom: (JoinInfo) -> Unit
) {
    private val viewModelScope = CoroutineScope(Job() + Dispatchers.Main)
    private val lyricist = Lyricist("en", Stringies)
    private var config: JellyfinConfig? = null

    var uiState by mutableStateOf<JellyfinUiState>(JellyfinUiState.Login)
        private set

    var loginState by mutableStateOf<LoginState>(LoginState.Initial)
        private set

    var libraries by mutableStateOf<List<JellyfinMediaItem>>(emptyList())
        private set

    var selectedLibrary by mutableStateOf<JellyfinMediaItem?>(null)
        private set

    var mediaItems by mutableStateOf<List<JellyfinMediaItem>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // Restore saved Jellyfin config if available
        viewModelScope.launch {
            val savedUrl = valueSuspendingly(DataStoreKeys.JELLYFIN_SERVER_URL, "")
            val savedApiKey = valueSuspendingly(DataStoreKeys.JELLYFIN_API_KEY, "")
            val savedUserId = valueSuspendingly(DataStoreKeys.JELLYFIN_USER_ID, "")
            
            if (savedUrl.isNotEmpty() && savedApiKey.isNotEmpty() && savedUserId.isNotEmpty()) {
                config = JellyfinConfig(
                    serverUrl = savedUrl,
                    apiKey = savedApiKey,
                    userId = savedUserId
                )
                loadLibraries()
                uiState = JellyfinUiState.Browser
            }
        }
    }

    private suspend fun saveConfig(config: JellyfinConfig) {
        writeValue(DataStoreKeys.JELLYFIN_SERVER_URL, config.serverUrl)
        writeValue(DataStoreKeys.JELLYFIN_API_KEY, config.apiKey)
        writeValue(DataStoreKeys.JELLYFIN_USER_ID, config.userId)
    }

    private suspend fun loadSyncplayConfig(): JoinInfo {
        return JoinInfo(
            username = valueSuspendingly(DataStoreKeys.MISC_JOIN_USERNAME, ""),
            roomname = valueSuspendingly(DataStoreKeys.MISC_JOIN_ROOMNAME, ""),
            address = valueSuspendingly(DataStoreKeys.MISC_JOIN_SERVER_ADDRESS, ""),
            port = valueSuspendingly(DataStoreKeys.MISC_JOIN_SERVER_PORT, 8997),
            password = valueSuspendingly(DataStoreKeys.MISC_JOIN_SERVER_PW, "")
        )
    }

    fun login(serverUrl: String, username: String, password: String) {
        loginState = LoginState.Loading
        
        viewModelScope.launch {
            repository.authenticate(serverUrl, username, password)
                .onSuccess { jellyfinConfig ->
                    config = jellyfinConfig
                    saveConfig(jellyfinConfig)
                    loginState = LoginState.Success
                    loadLibraries()
                    uiState = JellyfinUiState.Browser
                }
                .onFailure { error ->
                    loginState = LoginState.Error(error.message ?: lyricist.strings.roomLoginError)
                }
        }
    }

    private fun loadLibraries() {
        viewModelScope.launch {
            repository.getLibraries()
                .onSuccess { items ->
                    libraries = items
                }
                .onFailure { error ->
                    errorMessage = error.message
                }
        }
    }

    fun selectLibrary(library: JellyfinMediaItem) {
        selectedLibrary = library
        loadMediaItems(library.id)
    }

    private fun loadMediaItems(parentId: String) {
        viewModelScope.launch {
            repository.getMediaItems(parentId)
                .onSuccess { items ->
                    mediaItems = items
                }
                .onFailure { error ->
                    errorMessage = error.message
                }
        }
    }

    fun selectMedia(item: JellyfinMediaItem) {
        viewModelScope.launch {
            repository.getStreamUrl(item.id)
                .onSuccess { streamUrl ->
                    // Create MediaFile for the selected Jellyfin item
                    viewmodel?.media = MediaFile(
                        url = streamUrl,
                        fileName = item.name
                    )
                    
                    // Create room info with saved Syncplay settings
                    val syncplayConfig = loadSyncplayConfig()
                    val joinInfo = syncplayConfig.copy(
                        roomname = "jellyfin-${item.name}"
                    )
                    
                    onJoinRoom(joinInfo)
                }
                .onFailure { error ->
                    errorMessage = error.message
                }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}

sealed class JellyfinUiState {
    data object Login : JellyfinUiState()
    data object Browser : JellyfinUiState()
}

sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
