package io.github.usefulness.rootbeer.internal

import io.github.usefulness.rootbeer.RootBeerNative

internal object RootBeerWrapper {

    init {
        loadNativeLibrary()
    }

    @JvmStatic
    @Synchronized
    fun loadNativeLibrary() {
        runCatching { NativeLoader.initialize() }
            .onFailure(Throwable::printStackTrace)
    }

    fun isInitialized() = NativeLoader.isInitialized()

    fun setLogDebugMessages(logDebugMessages: Boolean) = RootBeerNative().setLogDebugMessages(logDebugMessages)
    fun checkForRoot(pathArray: Array<String>) = RootBeerNative().checkForRoot(pathArray)
    fun checkForMagiskUDS() = RootBeerNative().checkForMagiskUDS()
}
