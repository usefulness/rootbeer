package io.github.usefulness.rootbeer.sample

import android.content.Context
import io.github.usefulness.rootbeer.RootBeer

class CheckForRootWorker(context: Context) {

    private val rootBeer = RootBeer(context).apply {
        setLogging(logging = true)
    }

    operator fun invoke() = getRootResults()

    private fun getRootResults() = listOf(
        RootItemResult(R.string.root_checks_1, rootBeer.detectRootManagementApps()),
        RootItemResult(R.string.root_checks_2, rootBeer.detectPotentiallyDangerousApps()),
        RootItemResult(R.string.root_checks_3, rootBeer.detectTestKeys()),
        RootItemResult(R.string.root_checks_4, rootBeer.checkForBusyBoxBinary()),
        RootItemResult(R.string.root_checks_5, rootBeer.checkForSuBinary()),
        RootItemResult(R.string.root_checks_6, rootBeer.checkSuExists()),
        RootItemResult(R.string.root_checks_7, rootBeer.checkForRWPaths()),
        RootItemResult(R.string.root_checks_8, rootBeer.checkForDangerousProps()),
        RootItemResult(R.string.root_checks_9, rootBeer.checkForRootNative()),
        RootItemResult(R.string.root_checks_10, rootBeer.detectRootCloakingApps()),
        RootItemResult(R.string.root_checks_11, rootBeer.checkForMagiskBinary()),
        RootItemResult(R.string.root_checks_12, rootBeer.checkForMagiskUdsNative()),
    )
}
