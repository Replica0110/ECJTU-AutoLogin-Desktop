package com.lonx.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

internal val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

internal val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default

internal val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

object GlobalCoroutineScopeImpl  {

     val mainCoroutineDispatcher = CoroutineScope(mainDispatcher)

     val ioCoroutineDispatcher = CoroutineScope(ioDispatcher)

     val cpuCoroutineDispatcher = CoroutineScope(cpuDispatcher)
}
