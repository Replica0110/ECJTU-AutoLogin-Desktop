package com.lonx.utils

import com.lonx.AppName
import java.io.BufferedReader
import java.io.InputStreamReader



object AppLaunchManager {

    private const val SCRIPT = "start.bat"
    private val pid = ProcessHandle.current().pid()
    private val exePath = AppPathUtils.getAppExePath()
    private val scriptPath = AppPathUtils.getAppResPath().resolve("script").resolve(SCRIPT)
    fun restart(exitApplication: () -> Unit) {
        val command =
            listOf(
                "cmd",
                "/c",
                scriptPath.toString(),
                pid.toString(),
            )
        try {
            ProcessBuilder(*command.toTypedArray())
                .redirectErrorStream(true)
                .start()

            exitApplication()
        } catch (e: Exception) {
            println(e)
        }
    }

    fun isAutoStartUp(): Boolean {
        val command = listOf(
            "reg",
            "query",
            "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
            "/v", AppName
        )

        try {
            val processBuilder = ProcessBuilder(*command.toTypedArray())
                .redirectErrorStream(true) // 合并错误输出流

            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("REG_SZ")) {
                    val registryValue = line!!.substringBefore("REG_SZ").trim()
                    return registryValue.equals(AppName, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun makeAutoStartUp() {
        try {
            if (!isAutoStartUp()) {
                val command = listOf(
                    "reg",
                    "add",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                    "/v", AppName,
                    "/d", "\"$exePath --startup\"",
                    "/f"
                )
                val processBuilder = ProcessBuilder(*command.toTypedArray())
                    .redirectErrorStream(true) // 合并错误输出流

                val process = processBuilder.start()
                process.waitFor()
                if (process.exitValue() != 0) {
                    // 如果命令执行失败，获取错误信息
                    val error = process.inputStream.bufferedReader().use { it.readText() }
                    throw RuntimeException("注册表添加失败: $error")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeAutoStartUp() {
        try {
            if (isAutoStartUp()) {
                val command = listOf(
                    "reg",
                    "delete",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                    "/v", AppName,
                    "/f"
                )
                val processBuilder = ProcessBuilder(*command.toTypedArray())
                    .redirectErrorStream(true) // 合并错误输出流

                val process = processBuilder.start()
                process.waitFor()
                if (process.exitValue() != 0) {
                    // 如果命令执行失败，获取错误信息
                    val error = process.inputStream.bufferedReader().use { it.readText() }
                    throw RuntimeException("删除注册表项失败: $error")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
