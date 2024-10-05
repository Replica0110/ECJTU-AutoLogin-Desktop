package com.lonx.utils

fun getSystemProperty(): SystemProperty {
    return DesktopSystemProperty
}

object DesktopSystemProperty : SystemProperty {

    override fun getOption(key: String): String? = System.getProperty(key)

    override fun get(key: String): String = System.getProperty(key)

    override fun get(
        key: String,
        default: String,
    ): String = System.getProperty(key, default)

    override fun set(
        key: String,
        value: String,
    ) {
        System.setProperty(key, value)
    }
}

interface SystemProperty {

    fun getOption(key: String): String?

    fun get(key: String): String

    fun get(
        key: String,
        default: String,
    ): String

    fun set(
        key: String,
        value: String,
    )
}