package com.lonx.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class LoginService {

    fun login(studentID: String, passwordECJTU: String, theISP: Int): String {
        if (studentID.isEmpty()) {
            return "未填写学号！"
        }
        if (passwordECJTU.isEmpty()) {
            return "未填写密码！"
        }
        val strTheISP = when (theISP) {
            1 -> "cmcc"
            2 -> "telecom"
            else -> "unicom"
        }

        val client = OkHttpClient.Builder()
            .followRedirects(false)
            .build()

        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val postBody = "DDDDD=%2C0%2C$studentID@$strTheISP&upass=$passwordECJTU"
        val request = Request.Builder()
            .url("http://172.16.2.100:801/eportal/?c=ACSetting&a=Login&protocol=http:&hostname=172.16.2.100&iTermType=1&wlanacip=null&wlanacname=null&mac=00-00-00-00-00-00&enAdvert=0&queryACIP=0&loginMethod=1")
            .post(postBody.toRequestBody(mediaType))
            .build()

        val call = client.newCall(request)
        return try {
            val response = call.execute()
            val headers = response.headers
            val location = headers["Location"]
            if (location != null) {
                if (!location.contains("RetCode=")) {
                    return "登录成功！"
                }
                val startIndex = location.indexOf("RetCode=") + 8
                val endIndex = location.indexOf("&", startIndex)
                if (startIndex >= 0 && endIndex >= 0) {
                    return when (location.substring(startIndex, endIndex)) {
                        "userid error1" -> "账号不存在(未绑定宽带账号或运营商选择有误)"
                        "userid error2" -> "密码错误"
                        "512" -> "AC认证失败(重复登录)"
                        "Rad:Oppp error: Limit Users Err" -> "超出校园网设备数量限制"
                        else -> {
                            "登录失败，未知错误"
                        }
                    }
                }
                "无法解析回包数据：$headers"
            } else {
                "无法解析回包数据：$headers"
            }
        } catch (e: IOException) {
            "发送登录请求失败，捕获到异常：$e"
        }
    }

    fun getState(): Int {
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("http://172.16.2.100")
            .get()
            .build()

        val call = client.newCall(request)
        return try {
            val response = call.execute()
            if (response.code == 200) {
                val responseBody = response.body?.string() ?: ""
                if (responseBody.contains("<title>注销页</title>")) {
                    4
                } else {
                    3
                }
            } else {
                2
            }
        } catch (e: IOException) {
            when (e) {
                is SocketTimeoutException -> 2
                is ConnectException -> 1
                else -> {
                    2
                }
            }
        }
    }
}