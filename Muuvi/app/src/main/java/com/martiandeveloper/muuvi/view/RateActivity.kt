package com.martiandeveloper.muuvi.view

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.databinding.ActivityRateBinding
import com.martiandeveloper.muuvi.viewmodel.RateViewModel
import kotlinx.android.synthetic.main.activity_rate.*

class RateActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var vm: RateViewModel

    private lateinit var binding: ActivityRateBinding

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var currentTimeStamp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rate)
        vm = getViewModel()
        binding.rateViewModel = vm
        setToolbar()
        setPoster()
        setTitle()
        setListeners()
        setRating()
        observe()
        val tsLong = System.currentTimeMillis() / 1000
        currentTimeStamp = tsLong.toString()
    }

    private fun getViewModel(): RateViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RateViewModel() as T
            }
        })[RateViewModel::class.java]
    }

    private fun setToolbar() {
        setSupportActionBar(activity_rate_mainMT)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        supportActionBar?.title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setPoster() {
        val posterPath = intent.getStringExtra("posterPath")
        if (posterPath != null) {
            Glide.with(this)
                .load(posterPath)
                .placeholder(R.drawable.logo1)
                .centerCrop()
                .into(activity_rate_toolbarIV)
        }
    }

    private fun setTitle() {
        val title = intent.getStringExtra("title")
        binding.title = title
        if (intent.getStringExtra("mediaType") == "movie") {
            binding.subTitle = resources.getString(R.string.rate_this_movie)
        } else {
            binding.subTitle = resources.getString(R.string.rate_this_series)
        }
    }

    private fun setListeners() {
        activity_rate_gotItMTV.setOnClickListener(this)
        activity_rate_postMTV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.activity_rate_gotItMTV -> activity_rate_noteCV.visibility = View.GONE
                R.id.activity_rate_postMTV -> post()
            }
        }
    }

    private fun setRating() {
        val rating = intent.getStringExtra("rating")
        Log.d("Murad", "" + rating)
        if (rating != null) {
            activity_rate_mainRB.rating = rating.toFloat()
        }
    }

    private fun observe() {
        vm.rateETContent.observe(this, {
            binding.ratingLength = "${it.length}/500"
            if (it.length > 500) {
                activity_rate_ratingLengthMTV.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorTen
                    )
                )
                activity_rate_rateET.setBackgroundResource(R.drawable.edit_text_background_3)
                activity_rate_postMTV.setTextColor(ContextCompat.getColor(this, R.color.colorThree))
                activity_rate_postMTV.isEnabled = false
            } else {
                activity_rate_ratingLengthMTV.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorThree
                    )
                )
                activity_rate_rateET.setBackgroundResource(R.drawable.edit_text_background_2)
                activity_rate_postMTV.setTextColor(ContextCompat.getColor(this, R.color.colorOne))
                activity_rate_postMTV.isEnabled = true
            }
        })
    }

    private fun post() {
        if (auth.currentUser != null) {
            val movieId = intent.getStringExtra("id")
            if (movieId != null) {
                showProgress()

                val postMap = hashMapOf(
                    "movieId" to movieId,
                    "userId" to auth.currentUser!!.uid,
                    "time" to currentTimeStamp,
                    "review" to vm.rateETContent.value
                )

                db.collection("posts").document("$movieId (${auth.currentUser!!.uid})").set(postMap)
                    .addOnCompleteListener {
                        hideProgress()
                        if (it.isSuccessful) {
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                resources.getString(R.string.something_went_wrong_please_try_again_later),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun showProgress() {
        activity_rate_noteLL.alpha = .5F
        activity_rate_mainRB.alpha = .5F
        activity_rate_rateET.alpha = .5F
        activity_rate_rateET.isEnabled = false
        activity_rate_gotItMTV.isEnabled = false
        activity_rate_postMTV.visibility = View.GONE
        activity_rate_mainPB.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        activity_rate_noteLL.alpha = 1F
        activity_rate_mainRB.alpha = 1F
        activity_rate_rateET.alpha = 1F
        activity_rate_rateET.isEnabled = true
        activity_rate_gotItMTV.isEnabled = true
        activity_rate_postMTV.visibility = View.VISIBLE
        activity_rate_mainPB.visibility = View.GONE
    }
}