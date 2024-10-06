package com.lonx

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.lonx.ui.app
import com.lonx.utils.LoginService
import com.moriafly.salt.ui.popup.rememberPopupState
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.system.exitProcess

const val AppName = "ECJTULoginTool"

object AppSingleton {
    private val lockFile: Path = Path.of(System.getProperty("java.io.tmpdir"), "ECJTUAutoLogin.lock")

    init {
        if (!createLockFile()) {
            // If the lock file already exists, the application is already running
            println("Application is already running.")
            exitProcess(0) // Exit the new instance
        }
    }

    private fun createLockFile(): Boolean {
        return try {
            Files.createFile(lockFile)
            true
        } catch (e: Exception) {
            false // File already exists
        }
    }
    fun releaseLock() {
        try {
            Files.deleteIfExists(lockFile)
        } catch (e: Exception) {
            println("Failed to delete lock file: $e")
        }
    }
}

class LoginMain {
    companion object {
        private var showWindow = mutableStateOf(true)
        private var windowSub = mutableStateOf("")
        private var login = mutableStateOf(true)
        private var id = mutableStateOf("")
        private var pwd = mutableStateOf("")
        private var isp = mutableStateOf(1)
        private val loginService = LoginService()

        private fun initialize(settings: Settings) {
            id.value = settings.getString("id", "")
            pwd.value = settings.getString("pwd", "")
            isp.value = settings.getInt("isp", 1)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            showWindow.value = true

            AppSingleton

            application {
                val coroutineScope = rememberCoroutineScope()
                val scrollState = remember { ScrollState(0) }
                val trayState = rememberTrayState()
                val settings: Settings = PreferencesSettings(Preferences.userNodeForPackage(Main::class.java))
                val trayIcon = painterResource("icon.svg")
                initialize(settings)

                val windowState = rememberWindowState(
                    width = 380.dp,
                    height = 400.dp,
                    position = WindowPosition.Aligned(Alignment.Center)
                )

                if (args.isNotEmpty()) {
                    when (args[0]) {
                        "--startup" -> {
                            showWindow.value = false
                        }
                    }
                }
                app(
                    showWindow = showWindow,
                    settings = settings,
                    isp = isp,
                    scrollState = scrollState,
                    windowState = windowState,
                    id = id,
                    pwd = pwd,
                    login = login,
                    windowSub = windowSub,
                    popupState = rememberPopupState()
                )

                LaunchedEffect(windowSub.value){
                    trayState.sendNotification(Notification("华交校园网登录工具", windowSub.value, Notification.Type.None))
                }
                if (login.value) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val netState = loginService.getState()
                            windowSub.value = when (netState) {
                                1 -> "您似乎没有网络连接"
                                3 -> loginService.login(id.value, pwd.value, isp.value)
                                4 -> "您已经处于登录状态"
                                else -> "您连接的wifi似乎不是校园网"
                            }
                        } catch (e: Exception) {
                                windowSub.value = "登录失败，捕获到异常：$e"
                        }
                        login.value = false
                    }
                }

                Tray(
                    icon = trayIcon,
                    state = trayState,
                    tooltip = "华交校园网登录",
                    onAction = {
                        showWindow.value = !showWindow.value
                    },
                    menu = {
                        Item("login", onClick = { login.value = true})
                        Item("exit", onClick = {
                            AppSingleton.releaseLock()
                            exitProcess(0)
                        })
                    }
                )
            }
        }
    }
}
