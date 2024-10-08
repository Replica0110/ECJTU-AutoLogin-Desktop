package com.lonx.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.lonx.ECJTUAutoLogin.Companion.autoStartUp
import com.lonx.ECJTUAutoLogin.Companion.exit
import com.lonx.ECJTUAutoLogin.Companion.loginIn
import com.lonx.ECJTUAutoLogin.Companion.loginOut
import com.lonx.utils.AutoStartUp
import com.moriafly.salt.ui.*
import com.russhwolf.settings.Settings


@OptIn(UnstableSaltApi::class)
@Composable
@Preview
fun app(
    showWindow: MutableState<Boolean>,
    settings: Settings,
    scrollState: ScrollState = remember { ScrollState(0) },
    isp: MutableState<Int>,
    windowState: WindowState,
    studentId: MutableState<String>,
    password: MutableState<String>,
    showNotification: MutableState<Boolean>,
    windowSub: MutableState<String>
) {
    if (showWindow.value) {
        Window(
            icon = painterResource("icon.svg"),
            state = windowState,
            onCloseRequest = { showWindow.value = false },
            title = "华交校园网工具"
        ) {
            var showInfoDialog by remember { mutableStateOf(false) }
            if (showInfoDialog) {
                val tmpStudentId= remember { mutableStateOf(studentId.value) }
                val tmpPassword= remember { mutableStateOf(password.value) }
                val tmpISP= remember { mutableStateOf(isp.value) }
                SaltInputDialog(
                    title = "账号信息",
                    hint = "请输入账号",
                    secondHint = "请输入密码",
                    onConfirm = {
                        studentId.value = tmpStudentId.value
                        password.value = tmpPassword.value
                        isp.value=tmpISP.value
                        settings.apply {
                            putString("id", studentId.value)
                            putString("pwd", password.value)
                            putInt("isp", isp.value)
                        }
                        showInfoDialog = false },
                    popupMenuText = "选择运营商",
                    popupMenuItems = listOf("中国移动", "中国联通", "中国电信"),
                    popupMenuItemIndex = isp.value-1,
                    onDismissRequest = {showInfoDialog=false},
                    firstEditText = tmpStudentId.value,
                    secondEditText = tmpPassword.value,
                    onChange = {id,pwd,ispIndex->
                        tmpStudentId.value=id
                        tmpPassword.value=pwd
                        tmpISP.value=ispIndex+1 }
                    )
            }
            SaltTheme(configs = SaltConfigs(false)) {
                Scaffold(
                    bottomBar = {
                        BottomAppBar(modifier = Modifier.height(25.dp), backgroundColor = SaltTheme.colors.background) {
                            Text(
                                text = windowSub.value,
                                modifier = Modifier.padding(start = SaltTheme.dimens.subPadding),
                                style = SaltTheme.textStyles.sub
                            )
                        } },
                    content = { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SaltTheme.colors.background)
                                .padding(innerPadding)
                                .statusBarsPadding()
                                .verticalScroll(scrollState)) {
                            RoundedColumn {
                                ItemTip(text = "账号信息")
                                Item(
                                    text = "账号设置",
                                    sub = "当前账号：${studentId.value}",
                                    onClick = { showInfoDialog=true }
                                )
                            }
                            RoundedColumn {
                                ItemTip(text = "登录")
                                SaltButton(
                                    onClick = { loginIn.value = true },
                                    text = "登录"
                                )
                                ItemDivider()
                                SaltButton(
                                    onClick = { loginOut.value = true },
                                    text = "注销"
                                )
                            }
                            RoundedColumn {
                                ItemTip(text = "设置")
                                SaltItemSwitcher(
                                    text = "系统通知",
                                    state = showNotification.value,
                                    onChange ={
                                        showNotification.value = it
                                        settings.apply {
                                            putBoolean("notification", it)
                                        }
                                    })
                                ItemDivider()
                                SaltItemSwitcher(
                                    text = "开机自启",
                                    state = autoStartUp.value,
                                    onChange = {
                                        autoStartUp.value = it
                                        if (autoStartUp.value) {
                                            AutoStartUp.makeAutoStartUp()
                                            windowSub.value = "已设置开机自启"
                                        } else {
                                            AutoStartUp.removeAutoStartUp()
                                            windowSub.value = "已关闭开机自启"
                                        } }
                                )
                                ItemDivider()
                                SaltButton(
                                    text = "退出",
                                    onClick = { exit() }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}


