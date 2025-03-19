package org.sabda.gpt

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.adapter.CustomExpandableListAdapter
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.ToastUtil

class QuestionAnswer : AppCompatActivity() {
    private lateinit var exp: ExpandableListView
    private lateinit var title: TextView
    private lateinit var out: ImageView

    private lateinit var customExpandableListAdapter: CustomExpandableListAdapter

    private var listDataHeader: List<String> = emptyList()
    private var listDataChild: HashMap<String, MutableList<String>> = HashMap()
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_answer)

        initViews()
        setupTitle()
        initializeExpandListView()
        initializeNetwork()

        out.setOnClickListener { finish() }
    }

    private fun initViews() {
        exp = findViewById(R.id.questionListView)
        title = findViewById(R.id.title)
        out = findViewById(R.id.out)
    }

    private fun setupTitle() {
        title.setText(R.string.question_and_answer)
    }

    private fun initializeExpandListView() {
        listDataHeader = createHeaders()
        listDataChild = HashMap(createChildren(listDataHeader))

        customExpandableListAdapter = CustomExpandableListAdapter(this, listDataHeader, listDataChild)
        exp.setAdapter(customExpandableListAdapter)

        for (i in 0 until customExpandableListAdapter.groupCount) {
            exp.expandGroup(i)
        }

        exp.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val selectedChild = listDataChild[listDataHeader[groupPosition]]!![childPosition]
            Log.d(ContentValues.TAG, "SelectedChild: $selectedChild")
            true
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (!isConnected) {
            ToastUtil.showToast(this,"")
        }
    }

    private fun createHeaders(): List<String> {
        return createStringResourcesList("question", 15)
    }

    private fun createChildren(headers: List<String>): Map<String, MutableList<String>> {
        val answers = createStringResourcesList("answer", 15)

        return headers.mapIndexed { index, header ->
            header to createChildList(answers[index])
        }.toMap(HashMap())
    }

    private fun createChildList(answerResId: String): MutableList<String> {
        return mutableListOf(answerResId)
    }

    private fun createStringResourcesList(prefix: String, count: Int): List<String> {
        return (1..count).map { index -> getString(resources.getIdentifier("$prefix$index", "string", packageName)) }
    }

    private fun initializeNetwork() {
        isConnected = NetworkUtil.isNetworkAvailable(this)
        updateConnectionStatus(isConnected)

        NetworkUtil.registerNetworkChangeReceiver(this) { isConnected ->
            this.isConnected = isConnected
            updateConnectionStatus(isConnected)
        }
    }
}