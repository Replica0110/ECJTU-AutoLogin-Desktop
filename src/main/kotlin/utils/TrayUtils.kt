package com.lonx.utils

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.setContent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseListener
import java.util.*

private val iconSize = Size(512f, 512f)

internal val GlobalDensity get() =
    GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice
        .defaultConfiguration
        .density

private val GraphicsConfiguration.density: Density
    get() =
        Density(
            defaultTransform.scaleX.toFloat(),
            fontScale = 1f,
        )

internal val GlobalLayoutDirection get() = Locale.getDefault().layoutDirection

internal val Locale.layoutDirection: LayoutDirection
    get() = ComponentOrientation.getOrientation(this).layoutDirection

internal val ComponentOrientation.layoutDirection: LayoutDirection
    get() =
        when {
            isLeftToRight -> LayoutDirection.Ltr
            isHorizontal -> LayoutDirection.Rtl
            else -> LayoutDirection.Ltr
        }

val isTraySupported: Boolean get() = SystemTray.isSupported()




@Suppress("unused")
@Composable
fun MyTray(
    icon: Painter,
    state: MyTrayState = rememberMyTrayState(),
    tooltip: String? = null,
    onAction: (ActionEvent) -> Unit = {},
    mouseListener: MouseListener,
    menu: @Composable (MenuScope.() -> Unit) = {},
) {
    if (!isTraySupported) {
        DisposableEffect(Unit) {
            // We should notify developer, but shouldn't throw an exception.
            // If we would throw an exception, some application wouldn't work on some platforms at
            // all, if developer doesn't check that application crashes.
            //
            // We can do this because we don't return anything in Tray function, and following
            // code doesn't depend on something that is created/calculated in this function.
            System.err.println(
                "Tray is not supported on the current platform. " +
                        "Use the global property `isTraySupported` to check.",
            )
            onDispose {}
        }
        return
    }

    val currentOnAction by rememberUpdatedState(onAction)

    val awtIcon =
        remember(icon) {
            icon.toAwtImage(GlobalDensity, GlobalLayoutDirection, iconSize)
        }

    val tray =
        remember {
            TrayIcon(awtIcon).apply {
                isImageAutoSize = true

                addActionListener { e ->
                    currentOnAction(e)
                }

                addMouseListener(mouseListener)
            }
        }
    val popupMenu = remember { PopupMenu() }
    val currentMenu by rememberUpdatedState(menu)

    SideEffect {
        if (tray.image != awtIcon) tray.image = awtIcon
        if (tray.toolTip != tooltip) tray.toolTip = tooltip
    }

    val composition = rememberCompositionContext()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        tray.popupMenu = popupMenu

        val menuComposition =
            popupMenu.setContent(composition) {
                currentMenu()
            }

        SystemTray.getSystemTray().add(tray)

        state.notificationFlow
            .onEach(tray::displayMessage)
            .launchIn(coroutineScope)

        onDispose {
            menuComposition.dispose()
            SystemTray.getSystemTray().remove(tray)
        }
    }
}

@Composable
fun rememberMyTrayState() =
    remember {
        MyTrayState()
    }

private fun TrayIcon.displayMessage(notification: Notification) {
    val messageType =
        when (notification.type) {
            Notification.Type.None -> TrayIcon.MessageType.NONE
            Notification.Type.Info -> TrayIcon.MessageType.INFO
            Notification.Type.Warning -> TrayIcon.MessageType.WARNING
            Notification.Type.Error -> TrayIcon.MessageType.ERROR
        }

    displayMessage(notification.title, notification.message, messageType)
}
class MyTrayState {
    private val notificationChannel = Channel<Notification>(0)


    val notificationFlow: Flow<Notification>
        get() = notificationChannel.receiveAsFlow()


    fun sendNotification(notification: Notification) {
        notificationChannel.trySend(notification)
    }
}
