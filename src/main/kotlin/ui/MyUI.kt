package com.lonx.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.*
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.popup.PopupState

@Composable
fun SaltItemSwitcher(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    text: String,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(SaltTheme.dimens.item)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(horizontal = SaltTheme.dimens.padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = SaltTheme.dimens.subPadding)
        ) {
            Text(
                text = text,
                color = if (enabled) SaltTheme.colors.text else SaltTheme.colors.subText
            )
        }
        Spacer(modifier = Modifier.width(SaltTheme.dimens.subPadding))
        val backgroundColor by animateColorAsState(
            targetValue = if (state) SaltTheme.colors.highlight else SaltTheme.colors.subText.copy(alpha = 0.1f),
            animationSpec = spring(),
            label = "backgroundColor"
        )
        Box(
            modifier = Modifier
                .size(46.dp, 26.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(color = backgroundColor)
                }
                .padding(5.dp)
        ) {
            val layoutDirection = LocalLayoutDirection.current
            val translationX by animateDpAsState(
                targetValue = if (state) {
                    when (layoutDirection) {
                        LayoutDirection.Ltr -> 20.dp
                        LayoutDirection.Rtl -> (-20).dp
                    }
                } else {
                    0.dp
                },
                animationSpec = spring(),
                label = "startPadding"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = translationX.toPx()
                    }
                    .size(16.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
            )
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (enabled && primary) SaltTheme.colors.text else SaltTheme.colors.subText
        )
    }
}
@UnstableSaltApi
@Composable
fun SaltInputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String? = null,
    firstEditText: String,
    secondEditText: String,
    popupMenuText: String = "",
    popupMenuItems: List<String>,
    popupMenuItemIndex: Int = 0,
    onChange: (String, String, Int) -> Unit,
    hint: String? = null,
    secondHint: String? = null
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        title?.let { DialogTitle(text = it) }
        var popupMenuSub by remember { mutableStateOf(popupMenuItems[popupMenuItemIndex])  }
        val popupState = remember { PopupState(
            initialExpend = false
        ) }
        LaunchedEffect(popupMenuItemIndex) {
            popupMenuSub = popupMenuItems[popupMenuItemIndex]
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            RoundedColumn {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "账号",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = SaltTheme.dimens.padding, end = 0.dp)
                    )
                    ItemEdit(
                        text = firstEditText,
                        onChange = { firstEditText ->
                            onChange(firstEditText, secondEditText, popupMenuItemIndex)
                        },
                        hint = hint
                    )
                }
                Spacer(modifier = Modifier.height(SaltTheme.dimens.subPadding))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "密码",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = SaltTheme.dimens.padding, end = 0.dp)
                    )
                    ItemEditPassword(
                        text = secondEditText,
                        onChange = { secondEditText ->
                            onChange(firstEditText, secondEditText, popupMenuItemIndex)
                        },
                        hint = secondHint
                    )
                }

                Spacer(modifier = Modifier.height(SaltTheme.dimens.subPadding))
                ItemPopup(
                    state = popupState,
                    text = popupMenuText,
                    sub = popupMenuSub,
                ) {
                    popupMenuItems.forEachIndexed { index, item ->
                        Item(
                            text = item,
                            onClick = {
                                popupMenuSub = item
                                onChange(firstEditText, secondEditText, index)
                                popupState.dismiss()
                            },
                            arrowType = ItemArrowType.None
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.outerPadding()
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = "取消",
                textColor = SaltTheme.colors.subText,
                backgroundColor = SaltTheme.colors.subBackground
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding))
            TextButton(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier
                    .weight(1f),
                text = "确定"
            )
        }
    }
}
