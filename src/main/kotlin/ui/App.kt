package com.lonx.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.lonx.AppSingleton
import com.lonx.utils.AutoStartUp
import com.moriafly.salt.ui.*
import com.moriafly.salt.ui.popup.PopupState
import com.russhwolf.settings.Settings
import kotlin.system.exitProcess

@OptIn(UnstableSaltApi::class)
@Composable
@Preview
fun app(
    showWindow: MutableState<Boolean>,
    settings: Settings,
    scrollState: ScrollState = remember { ScrollState(0) },
    isp: MutableState<Int>,
    windowState: WindowState,
    popupState: PopupState,
    id: MutableState<String>,
    pwd: MutableState<String>,
    windowSub: MutableState<String>,
    login: MutableState<Boolean>
) {
    val popSub = when (isp.value) {
        1 -> remember { mutableStateOf("中国移动") }
        2 -> remember { mutableStateOf("中国联通") }
        else -> remember { mutableStateOf("中国电信") }
    }
    val autoStartUp = AutoStartUp()

    if (showWindow.value) {
        Window(
            icon = painterResource("icon.svg"),
            state = windowState,
            onCloseRequest = { showWindow.value = false },
            title = "华交校园网工具"
        ) {
            SaltTheme(configs = SaltConfigs(false)) {
                Scaffold(
                    bottomBar = {
                        BottomAppBar(modifier = Modifier.height(20.dp), backgroundColor = SaltTheme.colors.background) {
                            Text(
                                text = windowSub.value,
                                modifier = Modifier.statusBarsPadding(),
                                style = SaltTheme.textStyles.sub
                            )
                        }
                    }
                    ,
                    content = { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SaltTheme.colors.background)
                                .padding(innerPadding)
                                .statusBarsPadding()
                                .verticalScroll(scrollState)
                        ) {
                            RoundedColumn {
                            ItemEdit(
                                text = id.value,
                                onChange = { id.value = it },
                                hint = "请输入账号"
                            )
                            ItemDivider()
                            ItemEditPassword(
                                text = pwd.value,
                                onChange = { pwd.value = it },
                                hint = "请输入密码"
                            )
                            ItemDivider()
                            ItemPopup(
                                state = popupState, text = "切换运营商", sub = popSub.value
                            ) {
                                Item(text = "中国移动", onClick = {
                                    isp.value = 1
                                    popSub.value = "中国移动"
                                    popupState.dismiss()
                                }, arrowType = ItemArrowType.None)
                                Item(text = "中国联通", onClick = {
                                    isp.value = 2
                                    popSub.value = "中国联通"
                                    popupState.dismiss()
                                }, arrowType = ItemArrowType.None)
                                Item(text = "中国电信", onClick = {
                                    isp.value = 3
                                    popSub.value = "中国电信"
                                    popupState.dismiss()
                                }, arrowType = ItemArrowType.None)
                            }}
                            RoundedColumn {
                            SaltButton(
                                onClick = {
                                    login.value = true
                                },
                                text = "登录"
                            )
                            ItemDivider()
                            SaltButton(
                                text = "保存账号",
                                onClick = {
                                    settings.apply {
                                        clear()
                                        putString("id", id.value)
                                        putString("pwd", pwd.value)
                                        putInt("isp", isp.value)
                                    }
                                    windowSub.value = "账号已保存"
                                }
                            )
                            ItemDivider()
                            SaltButton(
                                text = "开机自启",
                                onClick = {
                                    if (autoStartUp.isAutoStartUp()) {
                                        autoStartUp.removeAutoStartUp()
                                        windowSub.value = "已取消开机自启"
                                    } else {
                                        autoStartUp.makeAutoStartUp()
                                        windowSub.value = "已设置开机自启"
                                    }
                                }
                            )
                            ItemDivider()
                            SaltButton(
                                text = "退出",
                                onClick = {
                                    AppSingleton.releaseLock()
                                    exitProcess(0)
                                }
                            )}

                        }

                    }
                )
            }
        }
    }
}

@UnstableSaltApi
@Composable
fun SaltButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    primary: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = text
            ) {
                onClick()
            }
            .padding(horizontal = SaltTheme.dimens.padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = if (enabled && primary) SaltTheme.colors.highlight else SaltTheme.colors.subText,
            fontWeight = FontWeight.Bold
        )
    }
}
