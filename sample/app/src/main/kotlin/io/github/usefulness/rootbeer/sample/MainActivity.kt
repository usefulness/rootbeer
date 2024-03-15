package io.github.usefulness.rootbeer.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.usefulness.rootbeer.sample.databinding.ActivityMainBinding
import io.github.usefulness.rootbeer.sample.ui.RootItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var infoDialog: AlertDialog? = null
    private val rootItemAdapter = RootItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.initView()
        binding.resetView()
    }

    private fun ActivityMainBinding.initView() {
        setSupportActionBar(toolbar)
        fab.setOnClickListener { checkForRoot() }
        rootResultsRecycler.layoutManager = LinearLayoutManager(this@MainActivity)
        rootResultsRecycler.adapter = rootItemAdapter
    }

    private fun ActivityMainBinding.resetView() {
        progressView.max = 100
        progressView.beerProgress = 0
        progressView.isVisible = true
        isRootedTextView.isVisible = false
        rootItemAdapter.submitList(null)
    }

    private fun ActivityMainBinding.checkForRoot() {
        fab.hide()
        resetView()

        lifecycleScope.launch {
            val results = CheckForRootWorker(context = this@MainActivity).invoke()
            animateResults(results)
        }
    }

    /**
     * There's probably a much easier way of doing this using View Property animators? :S
     */
    private fun ActivityMainBinding.animateResults(results: List<RootItemResult>) {
        val isRooted = results.any { it.result }
        // this allows us to increment the progress bar for x number of times for each of the results
        // all in the effort to smooth the animation
        val multiplier = 10
        progressView.max = results.size * multiplier

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                results.forEachIndexed { index, _ ->
                    for (i in 1..multiplier) {
                        // arbitrary delay, 50 millis seems to look ok when testing with 12 results
                        // post the UI updates in the UI thread
                        withContext(Dispatchers.Main) {
                            progressView.beerProgress += 1

                            // only add to the once we hit the multiplier
                            if (i == multiplier) {
                                rootItemAdapter.submitList(results.subList(fromIndex = 0, toIndex = index))
                            }
                            // is it the end of the results
                            if (index == results.size - 1) {
                                onRootCheckFinished(isRooted = isRooted)
                            }
                        }
                        delay(20)
                    }
                }
            }
            rootItemAdapter.submitList(results)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_github -> {
            openGithub()
            true
        }

        R.id.action_info -> {
            showInfoDialog()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun showInfoDialog() {
        // do nothing if already showing
        if (infoDialog?.isShowing != true) {
            infoDialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.info_details)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .setNegativeButton(R.string.action_more_info) { dialog, _ ->
                    dialog.dismiss()
                    openGithub()
                }
                .create()
            infoDialog?.show()
        }
    }

    private fun ActivityMainBinding.onRootCheckFinished(isRooted: Boolean) {
        fab.show()
        if (isRooted) {
            isRootedTextView.rooted()
        } else {
            isRootedTextView.notRooted()
        }
        isRootedTextView.isVisible = true
    }

    private fun openGithub() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK)))
    }

    private fun TextView.rooted() {
        setText(R.string.result_rooted)
        setTextColor(ContextCompat.getColor(context, R.color.fail))
    }

    private fun TextView.notRooted() {
        setText(R.string.result_not_rooted)
        setTextColor(ContextCompat.getColor(context, R.color.pass))
    }

    companion object {
        private const val GITHUB_LINK = "https://github.com/usefulness/rootbeer"
    }
}
