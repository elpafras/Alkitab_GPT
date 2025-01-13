package org.sabda.gpt

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ExpandableListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import org.sabda.gpt.adapter.CustomExpandableListAdapter
import org.sabda.gpt.data.ExamplePrompt
import org.sabda.gpt.data.ExpandableListData
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.databinding.ActivityMainBinding
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.ToastUtil

class MainActivity : AppCompatActivity(), NetworkChangeCallback {

    private lateinit var binding: ActivityMainBinding
    private var examplePrompt: ExamplePrompt? = null
    private var isDrawer: Boolean = false
    private var isConnected: Boolean = false
    private var randomPrompt: String? = null
    var maxCharCount: Int = 250

    private var customExpandableListAdapter: CustomExpandableListAdapter? = null
    private var listDataHeader: MutableList<String>? = null
    private var listDataChild: HashMap<String, MutableList<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUI()
        initializeExpandableListView()
        initializeListeners()
        initializeNetwork()
        setInitialInput()
        handleIntent(intent)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initializeUI() {
        val (headers, children) = ExpandableListData.prepareListData(this)
        val adapter = CustomExpandableListAdapter(this, headers, children)
        binding.expandableListView.setAdapter(adapter)

        binding.expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val selectedChild = children[headers[groupPosition]]?.get(childPosition)
            Log.d("MainActivity", "Selected: $selectedChild")
            if (selectedChild != null) {
                handleChildClick(selectedChild)
            }
            true
        }
    }

    private fun handleIntent(intent: Intent) {
        val inputPedia = intent.getStringExtra("inputPedia")
        val topic = intent.getStringExtra("topic")
        val lastBookName = intent.getStringExtra("lastBookName")
        val lastChapter = intent.getIntExtra("lastChapter", 1)

        if (inputPedia != null || topic != null) {
            binding.input.setText("")

            if (inputPedia != null) {
                binding.input.setText(inputPedia)
            } else if (topic != null) {
                val formattedText = String.format("%s %s %s", topic, lastBookName, lastChapter)
                binding.input.setText(formattedText.trim { it <= ' ' })
            }

            val alkitabGPTIntent = Intent(this, AlkitabGPT::class.java)
            alkitabGPTIntent.putExtras(intent.extras!!)
            startActivity(alkitabGPTIntent)
        }
    }

    private fun initializeExpandableListView() {
        prepareListData()
        customExpandableListAdapter =
            listDataChild?.let { listDataHeader?.let { it1 ->
                CustomExpandableListAdapter(this,
                    it1, it)
            } }
        binding.expandableListView.setAdapter(customExpandableListAdapter)

        binding.expandableListView.setOnChildClickListener { _: ExpandableListView?, _: View?, groupPosition: Int, childPosition: Int, _: Long ->
            val selectedChild =
                listDataChild!![listDataHeader!![groupPosition]]!![childPosition]
            Log.d("MainActChild", "initializeExpandableListView: $selectedChild")
            handleChildClick(selectedChild)
            true
        }

        binding.expandableListView.expandGroup(0)
        binding.expandableListView.expandGroup(1)
        binding.expandableListView.expandGroup(2)
    }

    private fun initializeListeners() {
        binding.apply {
            dots.setOnClickListener { showPopUp(it) }
            drawer.setOnClickListener { toggleDrawer() }
            input.addTextChangedListener(InputTextWatcher())
            input.setOnKeyListener { _: View, keyCode: Int, event: KeyEvent -> handleEnterKey(keyCode, event) }
            input.setOnEditorActionListener { _: TextView, actionId: Int, _: KeyEvent -> handleEditorAction(actionId) }
            send.setOnClickListener { handleSendButtonClick() }
            alkitabGPT.setOnClickListener { handleClick("https://chatgpt.com/g/g-QjHkF2IEk-alkitab-gpt-bible-man") }
            situsAlkitabGpt.setText(R.string.situs)
            situsAlkitabGpt.setOnClickListener { handleClick2("https://gpt.sabda.org/","Situs: Alkitab GPT") }
            situsAi.setOnClickListener { handleClick2("https://ai.sabda.org/","Situs: AI-Sabda") }
            lebihlanjut.setOnClickListener { startActivity(Intent(this@MainActivity, Selengkapnya::class.java)) }
        }
    }

    private fun initializeNetwork() {
        isConnected = NetworkUtil.isNetworkAvailable(this)
        updateConnectionStatus(isConnected)
        if (!isConnected) {
            ToastUtil.showToast(this)
        }
        NetworkUtil.registerNetworkChangeReceiver(
            this
        ) { isConnected: Boolean -> this.updateConnectionStatus(isConnected) }
    }

    private fun setInitialInput() {
        examplePrompt = ExamplePrompt()
        randomPrompt = examplePrompt!!.getRandomPrompt()
        binding.input.setText(randomPrompt)
        updateCharCount(binding.input.length())
    }

    private fun handleChildClick(selectedChild: String) {
        when (selectedChild) {
            "Studi Alkitab" -> {
                Log.d("TAG", "handleChildClicked")
                handleClick2("https://alkitab.sabda.org", "Situs: Studi Alkitab")
            }

            "Media Alkitab" -> {
                Log.d("TAG", "handleChildClicked")
                handleClick2("https://sabda.id/badeno/", "Situs: Media Alkitab")
            }

            "Alkitab" -> {
                Log.d("TAG", "handleChildClicked")
                openApps("org.sabda.alkitab.action.VIEW", "org.sabda.alkitab")
            }

            "AlkiPEDIA" -> {
                Log.d("TAG", "handleChildClicked")
                openApps("org.sabda.pedia.action.VIEW", "org.sabda.pedia")
            }

            "Tafsiran" -> {
                Log.d("TAG", "handleChildClicked")
                openApps("org.sabda.tafsiran.action.VIEW", "org.sabda.tafsiran")
            }

            "Kamus Alkitab" -> {
                Log.d("TAG", "handleChildClicked")
                openApps("org.sabda.kamus.action.VIEW", "org.sabda.kamus")
            }

            "Sabda Bot" -> {
                Log.d("TAG", "handleChildClicked")
                handleClick("https://t.me/sabdabot")
            }

            "Lain-lain" -> {
                Log.d("TAG", "handleChildClicked")
                handleClick("https://play.google.com/store/apps/dev?id=4791022353258811724&hl=id")
            }

            "AI-4-Church & AI-4-GOD" -> {
                Log.d("TAG", "handleChildClicked")
                handleClick("https://chat.whatsapp.com/EkBAirjrEKK4wa31yqQb6b")
            }
        }
    }

    private fun toggleDrawer() {
        if (isDrawer) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun handleEnterKey(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            handleSendInput()
            return true
        }
        return false
    }

    private fun handleEditorAction(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            handleSendInput()
            return true
        }
        return false
    }

    private fun handleSendButtonClick() {
        val inputText = binding.input.text.toString()
        val length = inputText.length

        if (length < 5) {
            Toast.makeText(
                this@MainActivity,
                "Masukkan prompt dengan panjang minimal 5 karakter",
                Toast.LENGTH_SHORT
            ).show()
        } else if (length > maxCharCount) {
            Toast.makeText(
                this@MainActivity,
                "Masukkan prompt dengan panjang maksimal 250 karakter",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (isConnected) {
                val intent = Intent(this@MainActivity, AlkitabGPT::class.java)
                intent.putExtra("inputtext", inputText)
                startActivity(intent)
                binding.input.setText("")
            } else {
                ToastUtil.showToast(this)
            }
        }
    }

    private fun handleSendInput() {
        val inputText = binding.input.text.toString()
        handleClick("https://gpt.sabda.org/chat.php?t=$inputText")
        binding.input.setText("")
    }

    private fun prepareListData() {
        listDataHeader = ArrayList()
        listDataChild = HashMap()

        (listDataHeader as ArrayList<String>).add(getString(R.string.situs2))
        (listDataHeader as ArrayList<String>).add(getString(R.string.aplikasi_5))
        (listDataHeader as ArrayList<String>).add(getString(R.string.komunitas))

        val listGroup1 = listOf(
            getString(R.string.studi_alkitab),
            getString(R.string.alkitab_media)
        )

        val listGroup2 = listOf(
            getString(R.string.alkitab),
            getString(R.string.alkipedia),
            getString(R.string.tafsiran),
            getString(R.string.kamus_alkitab),
            getString(R.string.bot),
            getString(R.string.lain)
        )

        val listGroup3 = listOf(
            getString(R.string.ai4cg)
        )


        listDataChild!![(listDataHeader as ArrayList<String>)[0]] = listGroup1.toMutableList()
        listDataChild!![(listDataHeader as ArrayList<String>)[1]] = listGroup2.toMutableList()
        listDataChild!![(listDataHeader as ArrayList<String>)[2]] = listGroup3.toMutableList()

    }

    private fun showPopUp(v: View) {
        val menu = PopupMenu(this, v)
        val inflater = menu.menuInflater
        inflater.inflate(R.menu.drawer_item_dots, menu.menu)
        menu.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            when (itemId) {
                R.id.AlkitabGPT -> {
                    handleClick("https://chatgpt.com/g/g-QjHkF2IEk-alkitab-gpt-bible-man")
                }
                R.id.pelajari -> {
                    startActivity(Intent(this@MainActivity, Selengkapnya::class.java))
                }
                R.id.tentang -> {
                    startActivity(Intent(this@MainActivity, Tentang::class.java))
                }
                R.id.question -> {
                    startActivity(Intent(this@MainActivity, QuestionAnswer::class.java))
                }
                R.id.realese -> {
                    showReleaseDialog()
                }
            }
            true
        }
        menu.show()
    }

    private fun showReleaseDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.release_note))
        builder.setMessage(getString(R.string.message_release_notes))
        builder.setPositiveButton(
            "OK"
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        val dialog = builder.create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(
                this, R.color.white
            )
        )
    }

    private fun handleClick(url: String) {
        if (isConnected) {
            NetworkUtil.openUrl(this, url)
        } else {
            ToastUtil.showToast(this)
        }
    }

    private fun handleClick2(url: String, title: String) {
        if (isConnected) {
            NetworkUtil.openWebView(this, url, title)
        } else {
            ToastUtil.showToast(this)
        }
    }

    private fun updateCharCount(length: Int) {
        val remaining = maxCharCount - length
        binding.characterCount.text = getString(R.string.cc, remaining)
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected
        binding.send.isEnabled = isConnected
        binding.alkitabGPT.isEnabled = isConnected
        binding.situsAlkitabGpt.isEnabled = isConnected

        if (!isConnected) {
            ToastUtil.showToast(this)
        }
    }

    private fun openApps(destination: String, packageName: String) {
        Log.d("TAG", "openApps:$packageName")
        if (isAppInstalled(packageName)) {
            Log.d("TAG", packageName + "installed")
            val intent = Intent(destination)
            //intent.setPackage(packageName);
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                redirectToPlayStore(packageName)
            }
        } else {
            Log.d("TAG", "$packageName not installed")
            redirectToPlayStore(packageName)
        }
    }

    private fun redirectToPlayStore(packageName: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        binding.input.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.input, InputMethodManager.SHOW_IMPLICIT)

        //clearDrawerSelection();
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
    }

    private inner class InputTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val length = s.length
            updateCharCount(length)

            if (length > maxCharCount) {
                binding.input.setText(s.subSequence(0, maxCharCount))
                binding.input.setSelection(maxCharCount)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }
}