package com.lonx.utils

import com.lonx.AppName
import okio.Path
import okio.Path.Companion.toPath
import java.io.BufferedReader
import java.io.InputStreamReader

val Path.noOptionParent: Path
    get() {
        return this.parent?.parent ?: this
    }
class AutoStartUp {
    private val systemProperty = getSystemProperty()
    private fun getAppJarPath(): Path {
        systemProperty.getOption("compose.application.resources.dir")?.let {
            return it.toPath()
        }
        throw IllegalStateException("Could not find app path")
    }
    private fun getAppExePath(): Path {
        return getAppJarPath().noOptionParent.normalized()
    }
    private val path = getAppExePath().resolve("ECJTULoginTool.exe").toString()
    private fun isAutoStartUp():Boolean{
        val command = "reg query \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v \"$AppName\""
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("REG_SZ")) {
                    val registryValue = line!!.substringAfter("REG_SZ").trim()
                    println(registryValue)
                    return registryValue.equals("getRegValue()", ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    fun makeAutoStartUp(){
        try {
            if (!isAutoStartUp()) {
                println(path)
                val command = (
                        "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v " +
                                "\"$AppName\" /d \"$path --startup\" /f"
                        )
                println(command)
                val process = Runtime.getRuntime().exec(command)
                process.waitFor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun removeAutoStartUp(){
        try {
            if (isAutoStartUp()) {
                val command = (
                        "reg delete \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v " +
                                "\"$AppName\" /f"
                        )
                val process = Runtime.getRuntime().exec(command)
                process.waitFor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
