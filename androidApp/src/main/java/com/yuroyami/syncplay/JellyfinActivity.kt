package com.yuroyami.syncplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yuroyami.syncplay.jellyfin.JellyfinContainer
import com.yuroyami.syncplay.models.JoinInfo
import com.yuroyami.syncplay.watchroom.homeCallback

class JellyfinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            JellyfinContainer(
                onJoinRoom = { joinInfo ->
                    // Pass the joinInfo back to HomeActivity
                    homeCallback?.onJoin(joinInfo)
                    finish()
                }
            )
        }
    }
}
