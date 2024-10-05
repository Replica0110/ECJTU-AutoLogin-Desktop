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
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.system.exitProcess

const val AppName = "ECJTUAutoLogin"

object AppSingleton {
    private val lockFile: Path = Path.of(System.getProperty("java.io.tmpdir"), "ECJTUAutoLogin.lock")

    init {
        if (!createLockFile()) {
            // If the lock file already exists, the application is already running
            println("Application is already running.")
            LoginMain.showMainWindow()
            exitProcess(0) // Exit the new instance
        }
    }

    private fun createLockFile(): Boolean {
        return try {
            Files.createFile(lockFile)
            true
        } catch (e: Exception) {
            println(e)
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
        private var login = mutableStateOf(false)
        private var id = mutableStateOf("")
        private var pwd = mutableStateOf("")
        private var isp = mutableStateOf(1)

        private fun initialize(settings: Settings) {
            id.value = settings.getString("id", "")
            pwd.value = settings.getString("pwd", "")
            isp.value = settings.getInt("isp", 1)
        }
        fun showMainWindow() {
            showWindow.value = true // 设置为显示窗口
        }

        @JvmStatic
        fun main(args: Array<String>) {
            showWindow.value = true
            AppSingleton

            application {
                val coroutineScope = rememberCoroutineScope()
                val trayState = rememberTrayState()
                val scrollState = remember { ScrollState(0) }
                val settings: Settings = PreferencesSettings(Preferences.userNodeForPackage(Main::class.java))

                initialize(settings)

                val windowState = rememberWindowState(
                    width = 380.dp,
                    height = 380.dp,
                    position = WindowPosition.Aligned(Alignment.Center)
                )

                if (args.isNotEmpty()) {
                    when (args[0]) {
                        "--startup" -> showWindow.value = false
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

                LaunchedEffect(id.value) {
                    login.value = true
                }

                if (login.value) {
                    coroutineScope.launch {
                        try {
                            val netState = LoginService().getState()
                            val rstTxt = when (netState) {
                                1 -> "您似乎没有网络连接"
                                3 -> LoginService().login(id.value, pwd.value, isp.value)
                                4 -> "您已经处于登录状态"
                                else -> "您连接的wifi似乎不是校园网"
                            }
                                windowSub.value = rstTxt

                                trayState.sendNotification(Notification("华交校园网登录", rstTxt, Notification.Type.None))

                        } catch (e: Exception) {
                                windowSub.value = "登录失败，捕获到异常：$e"

                                trayState.sendNotification(
                                    Notification("华交校园网登录", "登录失败，捕获到异常：$e", Notification.Type.None)
                                )

                        }
                        login.value = false
                    }
                }

                Tray(
                    icon = painterResource("icon.svg"),
                    state = trayState,
                    tooltip = "华交校园网登录",
                    onAction = {
                        showWindow.value = !showWindow.value
                    },
                    menu = {
                        Item("login", onClick = { login.value = true})
                        Item("exit", onClick = {
                            AppSingleton.releaseLock()
                            exitApplication()
                        })
                    }
                )
            }
        }
    }
}
