package com.yuroyami.syncplay.lyricist.localizations

import com.yuroyami.syncplay.lyricist.Strings
import com.yuroyami.syncplay.utils.format

val RuStrings = object : Strings {

    override val yes = "Да"
    override val no = "Нет"
    override val okay = "Окей"
    override val cancel = "Отмена"
    override val dontShowAgain = "Не показывать снова"
    override val play = "Воспроизвести"
    override val pause = "Пауза"
    override val delete = "Удалить"
    override val confirm = "Подтвердить"
    override val done = "Готово"
    override val close = "Закрыть"
    override val off = "Выкл"
    override val on = "Вкл"
    override val tabConnect = "Подключение"
    override val tabSettings = "Настройки"
    override val tabAbout = "О приложении"
    override val connectUsernameA = "Введите ваше имя пользователя:"
    override val connectUsernameB = "Имя пользователя"
    override val connectUsernameC = "Имя по вашему выбору"
    override val connectRoomnameA = "Введите название комнаты:"
    override val connectRoomnameB = "Название комнаты"
    override val connectRoomnameC = "Комната, где вы и ваши друзья смотрите"
    override val connectServerA = "Выберите сервер Syncplay:"
    override val connectServerB = "Адрес сервера"
    override val connectServerC = "Убедитесь, что вы и ваши друзья подключены к одному и тому же серверу."
    override val connectButtonJoin = "Присоединиться к комнате"
    override val connectButtonSaveshortcut = "Сохранить текущую конфигурацию как ярлык на главном экране"
    override val connectButtonCurrentEngine = { p0: String -> "Текущий движок: %s".format(p0) }
    override val connectFootnote = "Неофициальный клиент Syncplay для Android"
    override val connectUsernameEmptyError = "Имя пользователя не должно быть пустым"
    override val connectRoomnameEmptyError = "Название комнаты не должно быть пустым"
    override val connectAddressEmptyError = "Адрес сервера не должен быть пустым"
    override val connectPortEmptyError = "Введите порт!"
    override val connectEnterCustomServer = "Введите сторонний сервер"
    override val connectCustomServerPassword = "Пароль (если не требуется, оставьте пустым)"
    override val connectPort = "Порт"
    override val connectNightmodeswitch = "Переключить режим день/ночь"
    override val connectSolomode = "Войти в режим соло (только видеоплеер)"

    override val roomSelectedVid = { p0: String -> "Выбран видео файл: %s".format(p0) }
    override val roomSelectedSub = { p0: String -> "Загружен файл субтитров: %s".format(p0) }
    override val roomSelectedSubError = "Неверный файл субтитров. Поддерживаемые форматы: 'SRT', 'TTML', 'ASS', 'SSA', 'VTT'"
    override val roomSubErrorLoadVidFirst = "Сначала загрузите видео"
    override val roomTypeMessage = "Введите ваше сообщение..."
    override val roomReady = "Готово"
    override val roomNotReady = "Не готово"
    override val roomPingConnected = { p0: String -> "Подключено - PING: %s мс".format(p0) }
    override val roomPingDisconnected = "Отключено"
    override val roomEmptyMessageError = "Введите что-то!"
    override val roomAttemptingConnect = { p0: String, p1: String -> "Попытка подключения к %1s:%2s".format(p0, p1) }
    override val roomConnectedToServer = "Успешно подключено к серверу."
    override val roomConnectionFailed = "Не удалось подключиться."
    override val roomAttemptingReconnection = "Потеряно соединение с сервером."
    override val roomAttemptingTls = "Попытка установить защищенное соединение"
    override val roomTlsSupported = "Установлено защищенное соединение (TLS)"
    override val roomTlsNotSupported = "Сервер не поддерживает TLS"
    override val roomGuyPlayed = { p0: String -> "%s возобновил воспроизведение".format(p0) }
    override val roomGuyPaused = { p0: String, p1: String -> "%1s поставил на паузу на %2s".format(p0, p1) }
    override val roomSeeked = { p0: String, p1: String, p2: String -> "%1s перешел с %2s на %3s".format(p0, p1, p2) }
    override val roomRewinded = { p0: String -> "Перемотано из-за разницы во времени с %s".format(p0) }
    override val roomGuyLeft = { p0: String -> "%s покинул комнату.".format(p0) }
    override val roomGuyJoined = { p0: String -> "%s присоединился к комнате.".format(p0) }
    override val roomIsplayingfile = { p0: String, p1: String, p2: String -> "%1s воспроизводит '%2s' (%3s)".format(p0, p1, p2) }
    override val roomYouJoinedRoom = { p0: String -> "Вы присоединились к комнате: %s".format(p0) }
    override val roomScalingFitScreen = "Режим изменения размера: ПОДГОН К ЭКРАНУ"
    override val roomScalingFixedWidth = "Режим изменения размера: ФИКСИРОВАННАЯ ШИРИНА"
    override val roomScalingFixedHeight = "Режим изменения размера: ФИКСИРОВАННАЯ ВЫСОТА"
    override val roomScalingFillScreen = "Режим изменения размера: ЗАПОЛНЕНИЕ ЭКРАНА"
    override val roomScalingZoom = "Режим изменения размера: Масштабирование"
    override val roomSubTrackChanged = { p0: String -> "Дорожка с субтитрами изменёна на: %s".format(p0) }
    override val roomAudioTrackChanged = { p0: String -> "Звуковая дорожка изменена на: %s".format(p0) }
    override val roomAudioTrackNotFound = "Звуковая дорожка не найдена!"
    override val roomSubTrackDisable = "Отключить субтитры"
    override val roomTrackTrack = "Дорожка"
    override val roomSubTrackNotfound = "Субтитры не найдены!"
    override val roomDetailsCurrentRoom = { p0: String -> "Комната: %s".format(p0) }
    override val roomDetailsNofileplayed = "(Нет воспроизводимого файла)"
    override val roomDetailsFileProperties = { p0: String, p1: String -> "Длительность: %1s - Размер: %2s МБ".format(p0, p1) }
    override val roomFileMismatchWarningCore = { p0: String -> "Ваш файл отличается от файла %s следующим образом: ".format(p0) }
    override val roomFileMismatchWarningName = "Имя."
    override val roomFileMismatchWarningDuration = "Длительность."
    override val roomFileMismatchWarningSize = "Размер."
    override val roomSharedPlaylist = "Общий список вопроизведения комнаты"
    override val roomSharedPlaylistBrief = "Импортируйте файл или папку, чтобы включить название файла (названия файлов) в список вопроизведения. Щелкните по строке с файлом чтобы дать всем пользователям вопроизвести его ."
    override val roomSharedPlaylistUpdated = { p0: String -> "%s обновил список вопроизведения".format(p0) }
    override val roomSharedPlaylistChanged = { p0: String -> "%s изменил выделение в текущем списке воспроизведения".format(p0) }
    override val roomSharedPlaylistNoDirectories = "Вы не указали папки с медиа файдами для общих списков вопроизведения. Добавьте файлы вручную."
    override val roomSharedPlaylistNotFound = "Syncplay не может найти текущий воспроизводимый файл из общего списка воспроизведения в ваших папках с медиа."
    override val roomSharedPlaylistNotFeatured = "В этой комнате или на сервере, функция Общих списков вопроизведения не включена."
    override val roomSharedPlaylistButtonAddFile = "Добавить файл(ы) в конец списка воспроизведения"
    override val roomSharedPlaylistButtonAddFolder = "Добавить папку в список вопроизведения (и в папки с медиа)"
    override val roomSharedPlaylistButtonAddUrl = "Добавить URL(ы) в конец списка воспроизведения"
    override val roomSharedPlaylistButtonShuffle = "Перемешать весь список вопроизведения"
    override val roomSharedPlaylistButtonShuffleRest = "Перемешать остальные файлы списка вопроизведения"
    override val roomSharedPlaylistButtonOverflow = "Дополнительные настройки Общего списка воспроизведения"
    override val roomSharedPlaylistButtonPlaylistImport = "Загрузить список вопроизведения из файла"
    override val roomSharedPlaylistButtonPlaylistImportNShuffle = "Загрузить список вопроизведения из файла и перемешать"
    override val roomSharedPlaylistButtonPlaylistExport = "Сохранить список вопроизведения в файл"
    override val roomSharedPlaylistButtonSetMediaDirectories = "Задать папки с медиа"
    override val roomSharedPlaylistButtonSetTrustedDomains = "Задать доверенные домены"
    override val roomSharedPlaylistButtonUndo = "Отменить последнее изменение"
    override val roomButtonDescAspectRatio = "Соотношение сторон"
    override val roomButtonDescSharedPlaylist = "Общий список вопроизведения"
    override val roomButtonDescAudioTracks = "Звуковые дорожки"
    override val roomButtonDescSubtitleTracks = "Дорожки с субтитрами"
    override val roomButtonDescRewind = "Перемотка назад"
    override val roomButtonDescToggle = "."
    override val roomButtonDescPlay = "Воспроизвести"
    override val roomButtonDescPause = "Пауза"
    override val roomButtonDescFfwd = "Перемотка вперед"
    override val roomButtonDescAdd = "Добавить медиа-файл"
    override val roomButtonDescLock = "Блокировка экрана"
    override val roomButtonDescMore = "Дополнительные настройки"
    override val roomAddmediaOffline = "Из хранилища телефона"
    override val roomAddmediaOnline = "Из сетевого URL"
    override val roomAddmediaOnlineUrl = "URL адрес"
    override val roomSkipMinuteAndHalfButton = "Пропустить 1 минуту и 30 секунд"
    override val roomOverflowTitle = "Дополнительные опции..."
    override val roomOverflowPip = "режим картинка в картинке"
    override val roomOverflowMsghistory = "История чата"
    override val roomOverflowToggleNightmode = "Переключить ночной режим"
    override val roomOverflowLeaveRoom = "Покинуть комнату"
    override val roomCardTitleUserInfo = "Сведения о пользователе"
    override val roomCardTitleInRoomPrefs = "Настройки комнаты"

    override val mediaDirectories = "Папки с медиа для общего списка воспроизведения"
    override val mediaDirectoriesBrief = "Syncplay будет производить поиск среди любых папок с медиа указанных вами тут, чтобы найти название, которое воспроизводится в общем списке воспроизведения. Желательно указывать небольшие папки, так как операция поиска может вызвать задержки и быть очень медленной."
    override val mediaDirectoriesSettingSummary = "Syncplay будет искать среди любых указанных вами здесь папок с медиа, чтобы найти имя, которое воспроизводится в общем списке воспроизведения."
    override val mediaDirectoriesSave = "Сохранить и выйти"
    override val mediaDirectoriesClearAll = "Очистить все"
    override val mediaDirectoriesClearAllConfirm = "Вы уверены, что хотите очистить список ?"
    override val mediaDirectoriesAddFolder = "Добавить папку"
    override val mediaDirectoriesDelete = "Удалить из списка"
    override val mediaDirectoriesShowFullPath = "Показывать полный путь"

    override val settingsCategGeneral = "Общие"
    override val settingsCategExoplayer = "Exoplayer"
    override val settingsCategLanguage = "Язык"
    override val settingsCategSyncing = "Синхронизация"
    override val settingsCategNetwork = "Сеть"
    override val settingsCategAdvanced = "Расширенные"
    override val uisettingCategChatColors = "Цвета чата"
    override val uisettingCategChatProperties = "Свойства чата"
    override val uisettingCategPlayerSettings = "Настройки видеоплеера"
    override val uisettingCategMpv = "mpv"
    override val uisettingMpvHardwareAccelerationTitle = "Аппаратное ускорение"
    override val uisettingMpvHardwareAccelerationSummary = "Отключите это чтобы использовать вместо этого программное ускорение."
    override val uisettingMpvGpunextTitle = "Использовать gpu-next"
    override val uisettingMpvGpunextSummary = "Принудительное использование mpv нового бэкэнда для видеообработки на основе libplacebo."
    override val uiSettingMpvDebugTitle = "Включить отладку"
    override val uiSettingMpvDebugSummary = "Показывать статистику и сведения о текущем бэкэнде."
    override val uiSettingMpvInterpolationTitle = "Интерполяция частоты кадров"
    override val uiSettingMpvInterpolationSummary = "Снижает дрожание включив интерполяцию частоты кадров. Это может не работать хорошо в некоторых случаях."

    override val settingNightModeTitle = "Ночной режим"
    override val settingNightModeSummary = "Выберите поведение ночного режима."
    override val settingRememberJoinInfoTitle = "Запомнить информацию о присоединении"
    override val settingRememberJoinInfoSummary = "Включено по умолчанию. Это позволит SyncPlay сохранить ваш последний логин, имя комнаты и последний используемый официальный сервер."
    override val settingEraseShortcutsTitle = "Удалить все ярлыки домашнего экрана"
    override val settingEraseShortcutsSummary = "Это удалит все 'быстрые запуски' ярлыки домашнего экрана, которые вы создали через основной экран, чтобы сохранить настройки комнаты."
    override val settingEraseShortcutsDialog = "Вы действительно хотите удалить все существующие ярлыки?"
    override val settingDisplayLanguageTitle = "Язык отображения"
    override val settingDisplayLanguageSummry = "Выберите язык, на котором отображается Syncplay."
    override val settingDisplayLanguageToast = "Язык изменён. Перезапустите приложение, чтобы изменения вступили в силу полностью."
    override val settingAudioDefaultLanguageTitle = "Предпочтительный язык звуковой дорожки"
    override val settingAudioDefaultLanguageSummry = "Автоматически загружать звуковую дорожку с установленным вами здесь языковым кодом." +
            " Например, для английского код - 'en-US', для японского - 'ja-JP'. Произведите поиск в Google по запросу 'IETF BCP 47 codes' для получения дополнительной информации."
    override val settingCcDefaultLanguageTitle = "Предпочтительный язык субтитров"
    override val settingCcDefaultLanguageSummry = "Автоматически загружайте субтитры с установленным вами здесь языковым кодом." +
            " Например, для английского код - 'en-US', для японского - 'ja-JP'. Произведите поиск в Google по запросу 'IETF BCP 47 codes' для получения дополнительной информации."
    override val settingUseBufferTitle = "Использовать пользовательские размеры буфера"
    override val settingUseBufferSummary = "Если вы не удвлетворены временем загрузки видео до и во время воспроизведения, вы можете использовать пользовательские размеры буфера (Используйте на свой страх и риск)."
    override val settingMaxBufferTitle = "Пользовательский максимальный размер буфера"
    override val settingMaxBufferSummary =
        "По умолчанию 30 (30000 миллисекунд). Определяет максимальный размер буфера перед началом воспроизведения видео. Если вы не знаете что это, не изменяйте."
    override val settingMinBufferSummary =
        "По умолчанию 15 (15000 миллисекунд). Уменьшите это значение, чтобы воспроизводить видео быстрее, но есть вероятность, что плеер может сбоить или даже выйти из строя. Изменяйте на свой страх и риск."
    override val settingMinBufferTitle = "Пользовательский минимальный размер буфера"
    override val settingPlaybackBufferSummary =
        "По умолчанию 2500 миллисекунд. Это представляет собой размер буфера при ПЕРЕМОТКЕ или ВОЗОБНОВЛЕНИИ воспроизведения видео. Измените это значение, если вы не удовлетворены небольшой задержкой при перемотке видео."
    override val settingPlaybackBufferTitle = "Пользовательский размер буфера воспроизведения (мс)"
    override val settingReadyFirsthandSummary = "Включите это, если хотите, чтобы вас автоматически устанавливали в режим готовности сразу после входа в комнату."
    override val settingReadyFirsthandTitle = "Устанавливать готовность первым"
    override val settingRewindThresholdSummary = "Если кто-то отстает на выбранное вами значение, ваше видео будет перемотано, чтобы соответствовать тому, что позади."
    override val settingRewindThresholdTitle = "Порог перемотки"
    override val settingTlsSummary = "Syncplay сделает попытку установить безопасное шифрованное соединение (главным образом TLSv1.3, но может переключится на более старые версии) с сервером если он это поддерживает."
    override val settingTlsTitle = "Использовать защищенное соединение (TLS)"
    override val settingResetdefaultTitle = "Сбросить до настроек по умолчанию"
    override val settingResetdefaultSummary = "Сбросить всё к значениям по умолчанию (Рекомендуется)"
    override val settingResetdefaultDialog = "Вы уверены, что хотите очистить настройки для этого экрана ?"
    override val settingPauseIfSomeoneLeftTitle = "Приостанавливать если кто-то уходит"
    override val settingPauseIfSomeoneLeftSummary = "Включите это, если хотите, чтобы воспроизведение приостанавливалось/останавливалось, если кто-то уходит, когда вы смотрите."
    override val settingWarnFileMismatchTitle = "Предупреждение о несоответствии файлов"
    override val settingWarnFileMismatchSummary =
        "Включено по умолчанию. Предупредить вас в случае если вы загружаете файл отличный от файла других пользователей в группе (по имени, длительности или размеру, а не по всем параметрам)."
    override val settingFileinfoBehaviourNameTitle = "Отправка информации о имени файла"
    override val settingFileinfoBehaviourNameSummary = "Выберите метод, с помощью которого вы покажете другим пользователям имя вашего добавленного медиафайла."
    override val settingFileinfoBehaviourSizeTitle = "Отправка информации о размере файла"
    override val settingFileinfoBehaviourSizeSummary = "Выберите метод, с помощью которого вы покажете другим пользователям размер вашего добавленного медиафайла."

    override val uisettingApply = "ПРИМЕНИТЬ"
    override val uisettingTimestampSummary = "Отключите это, чтобы скрыть временные метки в начале сообщений чата."
    override val uisettingTimestampTitle = "Временные метки чата"
    override val uisettingMsgoutlineSummary = "Включите это, чтобы придать контуры сообщениям чата и предотвратить их слияние с фоновым видео."
    override val uisettingMsgoutlineTitle = "Контур сообщения в чате"
    override val uisettingMsgshadowSummary = "Включите это, чтобы сообщения чата имели тень и избежать их слияния с фоновым видео."
    override val uisettingMsgshadowTitle = "Тень сообщения в чате"
    override val uisettingMsgboxactionSummary =
        "Когда включено, нажатие кнопки 'OK' на клавиатуре отправит сообщение. Когда отключено, оно просто закроет клавиатуру без действий."
    override val uisettingMsgboxactionTitle = "Реакция на нажатие кнопки 'OK' на клавиатуре"
    override val uisettingOverviewAlphaSummary = "По умолчанию 40 (почти прозрачно), измените это, если хотите сделать панель Подробнее о комнате более читаемой, увеличив прозрачность."
    override val uisettingOverviewAlphaTitle = "Прозрачность заднего фона панели Подробнее о комнате"
    override val uisettingMessageryAlphaSummary = "По умолчанию 0 (прозрачно). Максимальное значение 255 (100% непрозрачность). Улучшить читаемость сообщений увеличив непрозрачность заднего фона."
    override val uisettingMessageryAlphaTitle = "Прозрачность заднего фона сообщений"
    override val uisettingMsgsizeSummary = "Изменяет размер текста сообщения. По умолчанию 10."
    override val uisettingMsgsizeTitle = "Размер шрифта сообщения"
    override val uisettingMsgcountSummary = "По умолчанию 10. Ограничивает количество сообщений."
    override val uisettingMsgcountTitle = "Максимальное количество сообщений"
    override val uisettingMsglifeSummary = "При получении сообщения в чат или в комнату, сообщение начнет исчезать в течение заданного времени."
    override val uisettingMsglifeTitle = "Время отображения сообщений чата"
    override val uisettingTimestampColorSummary = "Настройка цвета текста временных меток сообщений (по умолчанию - розовый)"
    override val uisettingTimestampColorTitle = "Цвет текста временных меток"
    override val uisettingSelfColorSummary = "Настройка цвета текста подписи вашего имени (по умолчанию - темно-красный)"
    override val uisettingSelfColorTitle = "Цвет подписи вашего имени"
    override val uisettingFriendColorSummary = "Настройка цвета текста подписей имён друзей (по умолчанию - синий)"
    override val uisettingFriendColorTitle = "Цвет текста подписей друзей"
    override val uisettingSystemColorSummary = "Настройка цвета текста системных сообщений комнаты (по умолчанию - белый)"
    override val uisettingSystemColorTitle = "Цвет текста системных сообщений"
    override val uisettingHumanColorSummary = "Настройка цвета текста пользовательских сообщений (по умолчанию - белый)"
    override val uisettingHumanColorTitle = "Цвет текста пользовательских сообщений"
    override val uisettingErrorColorSummary = "Настройка цвета текста сообщений об ошибках (по умолчанию - красный)"
    override val uisettingErrorColorTitle = "Цвет текста сообщений об ошибках"
    override val uisettingSubtitleSizeSummary =
        "Изменит размер субтитров для загружаемых субтитров (при загрузке из файла). По умолчанию 16. Размер встроенных субтитров изменить нельзя."
    override val uisettingSubtitleSizeTitle = "Размер субтитров"
    override val uisettingSubtitleDelaySummary = "По умолчанию 0. Это значение сместит субтитры назад или в перёд. Используйте отрицательные значения для ускорения."
    override val uisettingSubtitleDelayTitle = "Задержка субтитров (миллисекунды)"
    override val uisettingAudioDelaySummary = "По умолчанию 0. Это значение сместит звуковую дорожку назад или вперёд. Используйте отрицательные значения для сдвига вперёд."
    override val uisettingAudioDelayTitle = "Задержка звуковой дорожки (миллисекунды)"
    override val uisettingSeekForwardJumpSummary = "Указывает, сколько секунд промотается при перемотке вперед. По умолчанию 10 секунд."
    override val uisettingSeekForwardJumpTitle = "Величина скачка перемотки вперед (секунды)"
    override val uisettingSeekBackwardJumpSummary = "Указывает, сколько секунд промотается при перемоке назад. По умолчанию 10 секунд."
    override val uisettingSeekBackwardJumpTitle = "Величина скачка перемотки назад (секунды)"
    override val uisettingPipSummary = "Определяет, должен ли проигрыватель переходить в оконный режим картинка-в-картинке при сворачивании приложения. По умолчанию - да"
    override val uisettingPipTitle = "Режим картинка в картинке"
    override val uisettingReconnectIntervalSummary = "Сколько секунд ждать повторного подключения при каждом неудавшимся подключении или отключении. По умолчанию - 2 секунды."
    override val uisettingReconnectIntervalTitle = "Интервал повторного подключения"
    override val uisettingResetdefaultSummary = "Сбросить все настройки выше на значения по умолчанию."
    override val uisettingResetdefaultTitle = "Сброс до настроек по умолчанию"
    override val settingFileinfoBehaviorA = "Отправить как есть"
    override val settingFileinfoBehaviorB = "Отправить в виде хеша"
    override val settingFileinfoBehaviorC = "Не отправлять"
}