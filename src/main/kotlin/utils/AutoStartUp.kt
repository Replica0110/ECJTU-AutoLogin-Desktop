package com.lonx.utils

import com.lonx.AppName
import java.io.BufferedReader
import java.io.InputStreamReader

class AutoStartUp {
    fun isAutoStartUp():Boolean{
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
                val command = (
                        "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" /v " +
                                "\"$AppName\" /d \"C:\\Program Files\\ecjtulogin\\ecjtulogin.exe --startup\" /f"
                        )
                val process = Runtime.getRuntime().exec(command)
                process.waitFor()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}