package com.martiandeveloper.muuvi.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R

class SplashActivity : AppCompatActivity() {

    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        window.setBackgroundDrawableResource(R.drawable.splash_background)
        setContentView(R.layout.activity_splash)
        go()
    }

    private fun go() {
        if (isInternetAvailable()) {
            if (auth.currentUser != null) {
                Handler().postDelayed({
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 2000)
            } else {
                Handler().postDelayed({
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 2000)
            }
        } else {
            openNoInternetDialog()
        }
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
        val dialogNoInternet = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.layout_no_internet, null)
        val layoutNoInternetDismissMTV =
            view.findViewById<MaterialTextView>(R.id.layout_no_internet_dismissMTV)
        setTextViewToggle(layoutNoInternetDismissMTV)
        layoutNoInternetDismissMTV.setOnClickListener {
            dialogNoInternet.dismiss()
        }
        dialogNoInternet.setView(view)
        dialogNoInternet.setCanceledOnTouchOutside(false)
        dialogNoInternet.show()
    }

    private fun setTextViewToggle(textView: MaterialTextView) {
        textView.setTextColor(
            ContextCompat.getColorStateList(
                this,
                R.color.facebook_log_in_text_selector
            )
        )
    }
}