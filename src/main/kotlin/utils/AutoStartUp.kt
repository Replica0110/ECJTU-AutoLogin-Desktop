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
                    val registryValue = line!!.substringAfter("REG_SZ").substringBefore("--startup").trim()
                    return registryValue.equals(path, ignoreCase = true)
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
                    "/d", "\"$path --startup\"",
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
