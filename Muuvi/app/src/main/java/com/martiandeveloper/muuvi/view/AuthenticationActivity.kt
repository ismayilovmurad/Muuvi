package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.adapter.RecyclerViewLanguageAdapter
import com.martiandeveloper.muuvi.model.Language
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.*


class AuthenticationActivity : AppCompatActivity(), View.OnClickListener,
    RecyclerViewLanguageAdapter.ItemClickListener, NavController.OnDestinationChangedListener {

    private lateinit var adapter: RecyclerViewLanguageAdapter
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        checkLanguage()
        setContentView(R.layout.activity_authentication)
        setListeners()
        initVariables()
        window.setBackgroundDrawableResource(R.drawable.authentication_background)
    }

    private fun checkLanguage() {
        val sharedPreference = getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
        val languageCode = sharedPreference.getString("languageCode", "nope")

        if (languageCode != "nope") {
            if (languageCode != null) {
                changeLanguage(languageCode)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun changeLanguage(language: String) {
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        configuration.setLocale(Locale(language))
        resources.updateConfiguration(configuration, displayMetrics)
    }

    private fun setListeners() {
        activity_authentication_localizationLL.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.activity_authentication_localizationLL) {
            openLocalizationDialog(v)
        }
    }

    private fun openLocalizationDialog(v: View?) {
        val dialogLocalization = AlertDialog.Builder(v?.context).create()
        val view = layoutInflater.inflate(R.layout.layout_localization, null)

        val layoutLocalizationMainRV =
            view.findViewById<RecyclerView>(R.id.layout_localization_mainRV)
        val primaryLanguageNameList: ArrayList<String> =
            ArrayList(listOf(*resources.getStringArray(R.array.primaryLanguageName)))
        val secondaryLanguageNameList: ArrayList<String> =
            ArrayList(listOf(*resources.getStringArray(R.array.secondaryLanguageName)))

        val languageList = ArrayList<Language>()

        val sharedPreference = getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
        val language = sharedPreference.getString("language", "English")

        for (i in 0 until primaryLanguageNameList.size) {
            val checkedLanguage = primaryLanguageNameList.indexOf(language)
            if (i == checkedLanguage) {
                languageList.add(
                    Language(
                        primaryLanguageNameList[i],
                        secondaryLanguageNameList[i],
                        true
                    )
                )
            } else {
                languageList.add(
                    Language(
                        primaryLanguageNameList[i],
                        secondaryLanguageNameList[i],
                        false
                    )
                )
            }
        }

        layoutLocalizationMainRV.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerViewLanguageAdapter(languageList, this, this)
        layoutLocalizationMainRV.adapter = adapter

        val layoutLocalizationMainSV =
            view.findViewById<SearchView>(R.id.layout_localization_mainSV)
        layoutLocalizationMainSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

        })

        dialogLocalization.setView(view)
        dialogLocalization.show()
    }

    override fun onItemClick(language: Language) {
        if (!language.isChecked) {
            when (language.primaryName) {
                "English" -> setLanguage("en", "English")
                "Afrikaans" -> setLanguage("af", "Afrikaans")
                "Bahasa Indonesia" -> setLanguage("in", "Bahasa Indonesia")
                "Bahasa Melayu" -> setLanguage("ms", "Bahasa Melayu")
                "Dansk" -> setLanguage("da", "Dansk")
                "Deutsch" -> setLanguage("de", "Deutsch")
                "English (UK)" -> setLanguage("en", "English (UK)")
                "Español" -> setLanguage("es", "Español")
                "Español (España)" -> setLanguage("es", "Español (España)")
                "Filipino" -> setLanguage("fil", "Filipino")
                "Français (Canada)" -> setLanguage("fr", "Français (Canada)")
                "Français (France)" -> setLanguage("fr", "Français (France)")
                "Hrvatski" -> setLanguage("hr", "Hrvatski")
                "Italiano" -> setLanguage("it", "Italiano")
                "Magyar" -> setLanguage("hu", "Magyar")
                "Nederlands" -> setLanguage("nl", "Nederlands")
                "Norsk (bokmål)" -> setLanguage("no", "Norsk (bokmål)")
                "Polski" -> setLanguage("pl", "Polski")
                "Português (Brasil)" -> setLanguage("pt", "Português (Brasil)")
                "Português (Portugal)" -> setLanguage("pt", "Português (Portugal)")
                "Română" -> setLanguage("ro", "Română")
                "Slovenčina" -> setLanguage("sk", "Slovenčina")
                "Suomi" -> setLanguage("fi", "Suomi")
                "Svenska" -> setLanguage("sv", "Svenska")
                "Tiếng Việt" -> setLanguage("vi", "Tiếng Việt")
                "Türkçe" -> setLanguage("tr", "Türkçe")
                "Čeština" -> setLanguage("cs", "Čeština")
                "Ελληνικά" -> setLanguage("el", "Ελληνικά")
                "Български" -> setLanguage("bg", "Български")
                "Pусский" -> setLanguage("ru", "Pусский")
                "Українська" -> setLanguage("uk", "Українська")
                "српски" -> setLanguage("sr", "српски")
                "עברית" -> setLanguage("iw", "עברית")
                "اَلْعَرَبِيَّةُ" -> setLanguage("ar", "اَلْعَرَبِيَّةُ")
                "فارسی" -> setLanguage("fa", "فارسی")
                "हिन्दी" -> setLanguage("hi", "हिन्दी")
                "ภาษาไทย" -> setLanguage("th", "ภาษาไทย")
                "中文(简体)" -> setLanguage("zh", "中文(简体)")
                "中文(台灣)" -> setLanguage("zh", "中文(台灣)")
                "中文(香港)" -> setLanguage("zh", "中文(香港)")
                "日本語" -> setLanguage("ja", "日本語")
                "한국어" -> setLanguage("ko", "한국어")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setLanguage(languageCode: String, language: String) {
        saveLanguage(languageCode, language)
    }

    private fun saveLanguage(languageCode: String, language: String) {
        val sharedPreference = getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
        val editor = sharedPreference!!.edit()
        editor.putString("languageCode", languageCode)
        editor.putString("language", language)
        editor.apply()
        restart()
    }

    private fun restart() {
        startActivity(Intent(this, AuthenticationActivity::class.java))
        finish()
    }

    private fun initVariables() {
        navController = Navigation.findNavController(
            this,
            R.id.activity_authentication_authentication_navigationF
        )
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        /*when {
            destination.label.toString() == "LogInHelpFragment" -> {
                activity_authentication_localizationLL.visibility = View.GONE
            }
            destination.label.toString() == "SignUpUsernameFragment" -> {
                activity_authentication_localizationLL.visibility = View.GONE
            }
            destination.label.toString() == "SignUpUserImageFragment" -> {
                activity_authentication_localizationLL.visibility = View.GONE
            }
            destination.label.toString() == "SignUpWithPhoneOrEmailFragment" -> {
                activity_authentication_localizationLL.visibility = View.GONE
            }
            destination.label.toString() == "PasswordFragment" -> {
                activity_authentication_localizationLL.visibility = View.GONE
            }
            else -> {
                activity_authentication_localizationLL.visibility = View.VISIBLE
            }
        }*/
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(this)
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(this)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageData: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageData)
        for (fragment in supportFragmentManager.primaryNavigationFragment
            ?.childFragmentManager?.fragments!!) {
            fragment.onActivityResult(requestCode, resultCode, imageData)
        }
    }
}