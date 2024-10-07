package com.lonx

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.lonx.ui.app
import com.lonx.utils.*
import com.moriafly.salt.ui.popup.rememberPopupState
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
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
        var loginIn = mutableStateOf(true)
        var loginOut = mutableStateOf(false)
        val checkbox = mutableStateOf(AutoStartUp.isAutoStartUp())
        private var showWindow = mutableStateOf(true)
        private var windowSub = mutableStateOf("")
        private var id = mutableStateOf("")
        private var pwd = mutableStateOf("")
        private var isp = mutableStateOf(1)
        private val autoExit = mutableStateOf(false)

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
                val scrollState = remember { ScrollState(0) }
                val trayState = rememberMyTrayState()
                val settings: Settings = PreferencesSettings(Preferences.userNodeForPackage(Main::class.java))
                val trayIcon = painterResource("icon.svg")
                initialize(settings)

                val windowState = rememberWindowState(
                    width = 380.dp,
                    height = 450.dp,
                    position = WindowPosition.Aligned(Alignment.Center)
                )

                if (args.isNotEmpty()) {
                    when (args[0]) {
                        "--startup" -> {
                            showWindow.value = false
                            autoExit.value = true
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
                    windowSub = windowSub,
                    popupState = rememberPopupState()
                )

                LaunchedEffect(windowSub.value){
                    trayState.sendNotification(Notification("登录状态", windowSub.value, Notification.Type.None))
                }
                if (loginOut.value) {
                    GlobalCoroutineScopeImpl.ioCoroutineDispatcher.launch {
                        try {
                            val netState = LoginService.getState()
                            windowSub.value = when (netState) {
                                1 -> "您似乎没有网络连接"
                                3 -> LoginService.loginOut()
                                4 -> LoginService.loginOut()
                                else -> "您已经处于注销状态"
                            }
                        } catch (e: Exception) {
                                windowSub.value = "注销失败，捕获到异常：$e"
                        }
                        loginOut.value = false
                    }
                }
                if (loginIn.value) {
                    GlobalCoroutineScopeImpl.ioCoroutineDispatcher.launch {
                        try {
                            val netState = LoginService.getState()
                            windowSub.value = when (netState) {
                                1 -> "没有网络连接"
                                3 -> LoginService.login(id.value, pwd.value, isp.value)
                                4 -> "网络已连接"
                                else -> "连接的似乎不是校园网"
                            }
                        } catch (e: Exception) {
                                windowSub.value = "登录失败，捕获到异常：$e"
                        }
                        loginIn.value = false
                    }
                }

                MyTray(
                    icon = trayIcon,
                    state = trayState,
                    tooltip = "华交校园网登录",
                    onAction = {
                        showWindow.value = !showWindow.value
                    },
                    mouseListener = object : MouseListener {
                        override fun mouseEntered(e: MouseEvent?) {}

                        override fun mouseExited(e: MouseEvent?) {}

                        override fun mouseClicked(e: MouseEvent?) {}

                        override fun mousePressed(e: MouseEvent?) {
                            if (e?.button == MouseEvent.BUTTON1) {
                                showWindow.value = !showWindow.value
                            }
                        }
                        override fun mouseReleased(e: MouseEvent?) {}
                    },
                    menu = {
                        Item(text = "登录",
                            onClick = { loginIn.value = true}
                        )
                        Item(text = "注销",
                            onClick = { loginOut.value = true}
                        )
                        CheckboxItem(
                            text = "开机自启",
                            checked = checkbox.value,
                            onCheckedChange = { checkboxState ->
                                checkbox.value = checkboxState
                                if (checkboxState){
                                    AutoStartUp.makeAutoStartUp()
                                    windowSub.value = "已设置开机自启"
                                } else {
                                    AutoStartUp.removeAutoStartUp()
                                    windowSub.value = "已取消开机自启"
                                }
                            },
                        )
                        Separator()
                        Item(text = "退出",
                            onClick = {
                            AppSingleton.releaseLock()
                            exitApplication() }
                        )
                    }
                )
                if (autoExit.value){
                    LaunchedEffect(Unit){
                        delay(30000)
                        AppSingleton.releaseLock()
                        exitApplication()
                    }
                }
            }
        }
    }
}
