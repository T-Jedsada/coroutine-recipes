@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.dmytrodanylyk.launch.sequentially

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import com.dmytrodanylyk.R
import com.dmytrodanylyk.getThreadMessage
import com.dmytrodanylyk.logd
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MainPresenter(this, DataProvider())
        presenter.startPresenting()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopPresenting()
    }

    override fun showData(data: String?) {
        logd("showData $data" + getThreadMessage())
    }

    override fun showLoading() {
        logd("showLoading" + getThreadMessage())
    }
}

class DataProvider : DataProviderAPI {

    override fun loadData(params: String): String {
        SystemClock.sleep(5000)
        return "data for $params"
    }
}

interface DataProviderAPI {

    fun loadData(params: String): String
}

interface MainView {
    fun showData(data: String?)
    fun showLoading()
}

/**
 * launch + async + async (execute two tasks sequentially)
 */
class MainPresenter(private val view: MainView,
                    private val dataProvider: DataProviderAPI,
                    private val uiContext: CoroutineContext = UI,
                    private val bgContext: CoroutineContext = CommonPool) {

    fun startPresenting() {
        loadData()
    }

    fun stopPresenting() {
        // not used
    }

    private fun loadData() = launch(uiContext) {
        view.showLoading() // ui thread

        // non ui thread, suspend until task is finished
        val result1 = async(bgContext) { dataProvider.loadData("Task 1") }.await()

        // non ui thread, suspend until task is finished
        val result2 = async(bgContext) { dataProvider.loadData("Task 2") }.await()

        val result = "$result1 $result2" // ui thread

        view.showData(result) // ui thread
    }

}