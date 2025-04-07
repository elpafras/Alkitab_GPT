package org.sabda.gpt

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import org.sabda.gpt.adapter.CustomExpandableListAdapter
import org.sabda.gpt.data.local.ExamplePrompt
import org.sabda.gpt.data.local.ExpandableListData
import org.sabda.gpt.databinding.ActivityMainBinding
import org.sabda.gpt.fragment.AIFragment
import org.sabda.gpt.fragment.HomeFragment
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.showToast

class MainActivity : AppCompatActivity(), NetworkChangeCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var examplePrompt: ExamplePrompt
    private var isDrawer: Boolean = false

    private lateinit var randomPrompt: String


    private var customExpandableListAdapter: CustomExpandableListAdapter? = null
    private var listDataHeader: MutableList<String>? = null
    private var listDataChild: HashMap<String, MutableList<String>>? = null
    private var isConnected: Boolean = false

    private val inactivityTimeout = 3000L // 3 detik
    private val handler = Handler(Looper.getMainLooper())
    private val hideFloatingButtonRunnable = Runnable {
        binding.floatingChatButton.animate()
            .alpha(0f)
            .setDuration(500)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { binding.floatingChatButton.visibility = View.GONE }
            .start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentFrame, HomeFragment())
                    .commit()
                supportFragmentManager.executePendingTransactions()
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentFrame, HomeFragment())
                .commit()
        }

        initializeUI()
        initializeExpandableListView()
        initializeListeners()
        initializeNetwork()
        setInitialInput()
        setupNavBottom()
        handleIntent(intent)
        setupButtons()

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

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        resetInactivityTimer() // Setiap ada sentuhan, reset timer
        return super.dispatchTouchEvent(event)
    }

    private fun resetInactivityTimer() {
        handler.removeCallbacks(hideFloatingButtonRunnable)
        binding.floatingChatButton.visibility = View.VISIBLE
        binding.floatingChatButton.animate()
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
        handler.postDelayed(hideFloatingButtonRunnable, inactivityTimeout)
    }

    private fun setupButtons() {
        binding.floatingChatButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ChatActivity::class.java).apply {
                putExtra("START_NEW_CHAT", true)
            }
            startActivity(intent)
        }
    }

    private fun handleIntent(intent: Intent) {
        intent.getStringExtra("inputPedia")?.let {
            TODO()
        } ?: intent.getStringExtra("topic")?.let {
            val topic = it
            val lastBookName = intent.getStringExtra("lastBookName") ?: ""
            val lastChapter = intent.getIntExtra("lastChapter", 1)

            val textToSet = buildString {
                append("$topic ")
                append("$lastBookName $lastChapter".trim())
            }.trim()
            TODO()
        }

        intent.extras?.let {
            if (it.containsKey("YOUTUBE_ID") || it.containsKey("PDF_URL") || it.containsKey("TITLE")) {
                startActivity(Intent(this, AlkitabGPT::class.java).apply {
                    putExtras(it)
                })
            }
        }
    }

    private fun initializeExpandableListView() {
        prepareListData()
        customExpandableListAdapter = listDataHeader?.let { headers ->
            listDataChild?.let { children ->
                CustomExpandableListAdapter(this, headers, children).also {
                    binding.expandableListView.setAdapter(it)
                }
            }
        }

        binding.expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            listDataChild?.get(listDataHeader?.get(groupPosition))?.get(childPosition)?.let {
                Log.d("MainActivity", "Selected: $it")
                handleChildClick(it)
            }
            true
        }

        repeat(listDataHeader?.size ?: 0) {
            binding.expandableListView.expandGroup(it)
        }
    }

    private fun initializeListeners() {
        binding.apply {
            dots.setOnClickListener { showPopUp(it) }
            drawer.setOnClickListener { toggleDrawer() }
        }
    }

    private fun setupNavBottom() {
        binding.navBottom.setOnItemSelectedListener { item: MenuItem ->
            val fragment: Fragment = when (item.itemId) {
                R.id.home -> HomeFragment()
                R.id.ai -> AIFragment()
                else -> return@setOnItemSelectedListener false
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentFrame, fragment)
                .commit()

            true
        }

        // binding.navBottom?.itemIconTintList = ColorStateList.valueOf(Color.WHITE)
        // binding.navBottom?.itemTextColor = ColorStateList.valueOf(Color.WHITE)

        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val activeColor = ContextCompat.getColor(
            this, if (isNightMode) R.color.nav_active else R.color.nav_active
        )
        binding.navBottom.itemActiveIndicatorColor = ColorStateList.valueOf(activeColor)

        val iconTextColor = ContextCompat.getColor(
            this, if (isNightMode) R.color.black else R.color.white
        )
        binding.navBottom.itemIconTintList = ColorStateList.valueOf(iconTextColor)
        binding.navBottom.itemTextColor = ColorStateList.valueOf(iconTextColor)
    }

    private fun initializeNetwork() {
        isConnected = NetworkUtil.isNetworkAvailable(this)
        updateConnectionStatus(isConnected)
        if (!isConnected) {
            applicationContext.showToast(getString(R.string.toast_offline))
        }
        NetworkUtil.registerNetworkChangeReceiver(
            this
        ) { isConnected: Boolean -> this.updateConnectionStatus(isConnected) }
    }

    private fun setInitialInput() {
        examplePrompt = ExamplePrompt()
        randomPrompt = examplePrompt.getRandomPrompt()
    }

    private fun handleChildClick(selectedChild: String) {
        when (selectedChild) {
            "Studi Alkitab" -> { openUrl("https://alkitab.sabda.org/bible.php?book=Matius&chapter=1", "Situs: Studi Alkitab") }
            "Media Alkitab" -> { openUrl("https://sabda.id/badeno/", "Situs: Media Alkitab") }
            "Alkitab" -> { openApps("org.sabda.alkitab.action.VIEW", "org.sabda.alkitab") }
            "AlkiPEDIA" -> { openApps("org.sabda.pedia.action.VIEW", "org.sabda.pedia") }
            "Tafsiran" -> { openApps("org.sabda.tafsiran.action.VIEW", "org.sabda.tafsiran") }
            "Kamus Alkitab" -> { openApps("org.sabda.kamus.action.VIEW", "org.sabda.kamus") }
            "SABDA Bot" -> { openUrl("https://t.me/sabdabot") }
            "Lain-lain" -> { openUrl("https://play.google.com/store/apps/dev?id=4791022353258811724&hl=id") }
            "AI-4-Church & AI-4-GOD" -> { openUrl("https://chat.whatsapp.com/EkBAirjrEKK4wa31yqQb6b") }
        }
    }

    private fun toggleDrawer() {
        if (isDrawer) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun prepareListData() {
        listDataHeader = ArrayList()
        listDataChild = HashMap()

        // Header List
        val headers = listOf(R.string.situs2, R.string.aplikasi_5, R.string.komunitas)
        val listDataHeader = headers.map { getString(it) }.also { (listDataHeader as ArrayList<String>).addAll(it) }

        // Child Lists
        val childItems = listOf(
            listOf(R.string.studi_alkitab, R.string.alkitab_media),
            listOf(R.string.alkitab, R.string.alkipedia, R.string.tafsiran, R.string.kamus_alkitab, R.string.bot, R.string.lain),
            listOf(R.string.ai4cg)
        ).map { group -> group.map { getString(it) } }

        // Mapping Headers to Child Lists
        listDataHeader.forEachIndexed { index, header ->
            listDataChild!![header] = childItems[index].toMutableList()
        }
    }

    private fun showPopUp(v: View) {
        val menu = PopupMenu(this, v)
        val inflater = menu.menuInflater
        inflater.inflate(R.menu.drawer_item_dots, menu.menu)
        menu.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            when (itemId) {
                R.id.AlkitabGPT -> {
                    openUrl("https://chatgpt.com/g/g-QjHkF2IEk-alkitab-gpt-bible-man")
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

    private fun openUrl(url: String, title: String? = null) {
        if (isConnected) {
            title?.let { NetworkUtil.openWebView(this, url, it) } ?: NetworkUtil.openUrl(this, url)
        } else {
            applicationContext.showToast(getString(R.string.toast_offline))
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (!isConnected) {
            applicationContext.showToast(getString(R.string.toast_offline))
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
            } catch (_: ActivityNotFoundException) {
                redirectToPlayStore(packageName)
            }
        } else {
            Log.d("TAG", "$packageName not installed")
            redirectToPlayStore(packageName)
        }
    }

    private fun redirectToPlayStore(packageName: String) {
        startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
        handler.removeCallbacks(hideFloatingButtonRunnable)
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }
}