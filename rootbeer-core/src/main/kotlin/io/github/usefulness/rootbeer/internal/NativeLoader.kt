package io.github.usefulness.rootbeer.internal

internal object NativeLoader {
    private var extracted = false

    @JvmStatic
    @Synchronized
    fun initialize(): Boolean {
        loadNativeLibrary()
        return extracted
    }

    fun isInitialized() = extracted

    private fun loadNativeLibrary() {
        if (extracted) {
            return
        }
        try {
            System.loadLibrary("toolChecker")
            extracted = true
        } catch (e: UnsatisfiedLinkError) {
            System.err.println(e)
        }
    }
}
