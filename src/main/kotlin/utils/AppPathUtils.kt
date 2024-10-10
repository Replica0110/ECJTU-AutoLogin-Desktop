package com.lonx.utils

import com.lonx.AppName
import okio.Path
import okio.Path.Companion.toPath

val Path.noOptionParent: Path
    get() {
        return this.parent?.parent ?: this
    }
object AppPathUtils {
    private val systemProperty = getSystemProperty()
    fun getAppResPath(): Path {
        systemProperty.getOption("compose.application.resources.dir")?.let {
            return it.toPath()
        }
        throw IllegalStateException("Could not find app path")
    }
    fun getAppExePath(): Path {
        return getAppResPath().noOptionParent.normalized().resolve("$AppName.exe")
    }

}