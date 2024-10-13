package com.lonx

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.lonx.ui.app
import com.lonx.utils.AppLaunchManager
import com.lonx.utils.LoginService
import com.lonx.utils.MyTray
import com.lonx.utils.rememberMyTrayState
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
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
        if (!appLock()) {
            if (isAppRunning()) {
                exitProcess(0)
            }
        }
    }

    private fun appLock(): Boolean {  // 创建应用锁
        return try {
            Files.write(lockFile, "${ProcessHandle.current().pid()}\n".toByteArray(), StandardOpenOption.CREATE_NEW)
            true
        } catch (e: Exception) {
            false
        }
    }
    private fun isAppRunning(): Boolean { // 检查是否存在应用锁
        return try {
            val pid = Files.readAllLines(lockFile).first().toInt()
            ProcessHandle.of(pid.toLong()).isPresent && ProcessHandle.of(pid.toLong()).get().isAlive
        } catch (e: Exception) {
            false
        }
    }
    fun releaseAppLock() { // 释放应用锁
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
        val autoStartUp = mutableStateOf(AppLaunchManager.isAutoStartUp())
        private var showWindow = mutableStateOf(true)
        private var windowSub = mutableStateOf("")
        private var studentId = mutableStateOf("")
        private var password = mutableStateOf("")
        private var getIP = mutableStateOf(false)
        private var isp = mutableStateOf(1)
        private var showNotification = mutableStateOf(true)
        private val autoExit = mutableStateOf(false)
        private val loginService = LoginService()
        private var ip = mutableStateOf("unknown")
        private fun initialize(settings: Settings) {
            studentId.value = settings.getString("id", "")
            password.value = settings.getString("pwd", "")
            isp.value = settings.getInt("isp", 1)
            getIP.value = settings.getBoolean("getIP", false)
            showNotification.value = settings.getBoolean("notification", true)
        }
        // 退出并释放锁
        fun exit() {
            AppSingleton.releaseAppLock()
            exitProcess(0)
        }
        @JvmStatic
        fun main(args: Array<String>) {
            // 确保只有一个实例在运行
            AppSingleton

            // 创建应用
            application {
                val scrollState = remember { ScrollState(0) }
                val coroutineScope = rememberCoroutineScope {Dispatchers.Default}
                val trayState = rememberMyTrayState()
                val settings: Settings = PreferencesSettings(Preferences.userNodeForPackage(ECJTUAutoLogin::class.java))
                val appIcon = painterResource("icon.svg")
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
                    coroutineScope.launch {
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
                    coroutineScope.launch {
                        try {
                            val netState = loginService.getState()
                            windowSub.value = when (netState) {
                                1 -> "没有网络连接"
                                3 -> loginService.login(studentId.value, password.value, isp.value)
                                4 -> "网络已连接"
                                else -> "连接的似乎不是校园网"
                            }
                            if ((netState==4 || netState==3) && getIP.value) {
                                ip.value = loginService.getIp()
                                loginService.copyToClipboard(ip.value)
                            }
                        } catch (e: Exception) {
                                windowSub.value = "登录失败，捕获到异常：$e"
                        }
                        loginIn.value = false
                    }
                }
                // 创建系统托盘
                MyTray(
                    icon = appIcon,
                    state = trayState,
                    tooltip = "系统通知：${showNotification.value}\n开机自启：${autoStartUp.value}\nIP地址：${ip.value}",
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
                                    AppLaunchManager.makeAutoStartUp()
                                    windowSub.value = "已设置开机自启"
                                } else {
                                    AppLaunchManager.removeAutoStartUp()
                                    windowSub.value = "已关闭开机自启"
                                }
                            },
                        )
                        Menu(
                            text = "调试选项",
                            content = {
                                Item(
                                    text = "清空缓存",
                                    onClick = {
                                        coroutineScope.launch {
                                            settings.clear()
                                            windowSub.value = "已清空缓存"
                                        }
                                    }
                                )
//                                Item( // TODO 功能未实现
//                                    text = "重启应用",
//                                    onClick = {
//                                        coroutineScope.launch {
//                                            AppLaunchManager.restart { exitApplication() }
//                                        }
//                                    }
//                                )
                                CheckboxItem(
                                    text = "自动获取IP并复制",
                                    checked = getIP.value,
                                    onCheckedChange = {
                                        getIP.value = it
                                        settings.apply {
                                            putBoolean("getIP", getIP.value)
                                        }
                                    },
                                )
                            }
                        )
                        Separator()
                        Item(
                            text = "退出",
                            onClick = { exit() }
                        )
                    }
                )
            }
        }
    }
}
