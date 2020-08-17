package com.martiandeveloper.muuvi.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.martiandeveloper.muuvi.R
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setContentView(R.layout.activity_feed)
        window.setBackgroundDrawableResource(R.drawable.splash_background)
        setNavigation()
    }

    private fun setNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.activity_feed_feed_navigationF) as NavHostFragment?
        NavigationUI.setupWithNavController(
            activity_feed_mainBNV,
            navHostFragment!!.navController
        )
    }
}