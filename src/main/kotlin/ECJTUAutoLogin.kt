package com.lonx

import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.prefs.Preferences
import kotlin.system.exitProcess

const val AppName = "ECJTULoginTool"

object AppSingleton {
    private val lockFile: Path = Path.of(System.getProperty("java.io.tmpdir"), "${ECJTUAutoLogin}.lock")

    init {
        if (!createLockFileWithPid()) {
            if (isAnotherInstanceRunning()) {
                exitProcess(0)
            }
        }
    }

    private fun createLockFileWithPid(): Boolean {  // 创建锁文件并写入进程PID
        return try {
            Files.write(lockFile, "${ProcessHandle.current().pid()}\n".toByteArray(), StandardOpenOption.CREATE_NEW)
            true
        } catch (e: Exception) {
            false
        }
    }
    private fun isAnotherInstanceRunning(): Boolean { // 检查锁文件是否正在被另一个进程占用
        return try {
            val pid = Files.readAllLines(lockFile).first().toInt()
            ProcessHandle.of(pid.toLong()).isPresent && ProcessHandle.of(pid.toLong()).get().isAlive
        } catch (e: Exception) {
            false
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


class ECJTUAutoLogin {
    companion object {
        var loginIn = mutableStateOf(true)
        var loginOut = mutableStateOf(false)
        val autoStartUp = mutableStateOf(AutoStartUp.isAutoStartUp())
        private var showWindow = mutableStateOf(true)
        private var windowSub = mutableStateOf("")
        private var studentId = mutableStateOf("")
        private var password = mutableStateOf("")
        private var isp = mutableStateOf(1)
        private var showNotification = mutableStateOf(true)
        private val autoExit = mutableStateOf(false)
        private val loginService = LoginService()

        private fun initialize(settings: Settings) {
            studentId.value = settings.getString("id", "")
            password.value = settings.getString("pwd", "")
            isp.value = settings.getInt("isp", 1)
            showNotification.value = settings.getBoolean("notification", true)
        }
        // 退出并释放锁
        fun exit() {
            AppSingleton.releaseLock()
            exitProcess(0)
        }
        @JvmStatic
        fun main(args: Array<String>) {
            // 确保只有一个实例在运行
            AppSingleton

            // 创建应用
            application {
                val scrollState = remember { ScrollState(0) }
                val trayState = rememberMyTrayState()
                val settings: Settings = PreferencesSettings(Preferences.userNodeForPackage(ECJTUAutoLogin::class.java))
                val trayIcon = painterResource("icon.svg")
                initialize(settings)
                // 如果包含自动退出参数，延迟10秒后自动退出
                LaunchedEffect(Unit){
                    delay(10000)
                    if (autoExit.value){
                        exit()
                    }
                }
                val windowState = rememberWindowState(
                    width = 380.dp,
                    height = 530.dp,
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
                    scrollState = scrollState,
                    isp = isp,
                    windowState = windowState,
                    studentId = studentId,
                    password = password,
                    showNotification = showNotification,
                    windowSub = windowSub
                )
                LaunchedEffect(windowSub.value){
                     if (showNotification.value) {
                        trayState.sendNotification(Notification("网络状态", windowSub.value, Notification.Type.None))
                    }
                }
                if (loginOut.value) {
                    GlobalCoroutineScopeImpl.ioCoroutineDispatcher.launch {
                        try {
                            val netState = loginService.getState()
                            windowSub.value = when (netState) {
                                1 -> "您似乎没有网络连接"
                                3 -> loginService.loginOut()
                                4 -> loginService.loginOut()
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
                            val netState = loginService.getState()
                            windowSub.value = when (netState) {
                                1 -> "没有网络连接"
                                3 -> loginService.login(studentId.value, password.value, isp.value)
                                4 -> "网络已连接"
                                else -> "连接的似乎不是校园网"
                            }
                        } catch (e: Exception) {
                                windowSub.value = "登录失败，捕获到异常：$e"
                        }
                        loginIn.value = false
                    }
                }
                // 创建系统托盘
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
                            if (e?.button == MouseEvent.BUTTON1) { // 仅当点击左键时调用，打开主窗口
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
                            text = "系统通知",
                            checked = showNotification.value,
                            onCheckedChange = {
                                showNotification.value = it
                                settings.apply {
                                    putBoolean("notification", showNotification.value)
                                }
                            },
                        )
                        CheckboxItem(
                            text = "开机自启",
                            checked = autoStartUp.value,
                            onCheckedChange = {
                                autoStartUp.value = it
                                if (autoStartUp.value){
                                    AutoStartUp.makeAutoStartUp()
                                    windowSub.value = "已设置开机自启"
                                } else {
                                    AutoStartUp.removeAutoStartUp()
                                    windowSub.value = "已关闭开机自启"
                                }
                            },
                        )
                        Separator()
                        Item(text = "退出",
                            onClick = { exit() }
                        )
                    }
                )
            }
        }
    }
}
