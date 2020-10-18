package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R

class SplashActivity : AppCompatActivity(), View.OnClickListener {

    private val auth = Firebase.auth

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        window.setBackgroundDrawableResource(R.drawable.splash_background)
        setContentView(R.layout.activity_splash)
    }

    private fun checkInternet() {
        Handler().postDelayed({
            if (isInternetAvailable()) {
                go()
            } else {
                openNoInternetDialog()
            }
        }, 500)
    }

    private fun go() {
        Handler().postDelayed({
            val intent: Intent = if (auth.currentUser != null) {
                Intent(this, FeedActivity::class.java)
            } else {
                Intent(this, AuthenticationActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }, 2000)
    }

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(): Boolean {
        var result = false

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }
        return result
    }

    private fun openNoInternetDialog() {
        dialog = AlertDialog.Builder(this).create()

        val view = layoutInflater.inflate(R.layout.layout_no_internet_2, null)

        val layoutNoInternet2TryAgainMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_2_tryAgainMTV)
        val layoutNoInternet2SettingsMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_2_settingsMTV)

        setTextViewToggle(layoutNoInternet2TryAgainMTV)
        setTextViewToggle(layoutNoInternet2SettingsMTV)

        layoutNoInternet2TryAgainMTV.setOnClickListener(this)
        layoutNoInternet2SettingsMTV.setOnClickListener(this)

        dialog.setView(view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            ContextCompat.getColorStateList(
                this,
                R.color.facebook_log_in_text_selector
            )
        )
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.layout_no_internet_2_tryAgainMTV -> tryAgain()
                R.id.layout_no_internet_2_settingsMTV -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
    }

    private fun tryAgain() {
        dialog.dismiss()
        checkInternet()
    }
}
