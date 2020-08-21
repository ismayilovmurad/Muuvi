package com.martiandeveloper.muuvi.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.databinding.FragmentAddTvDetailBinding
import com.martiandeveloper.muuvi.service.POSTER_BASE_URL
import kotlinx.android.synthetic.main.fragment_add_tv_detail.*


class AddTvDetailFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentAddTvDetailBinding

    private lateinit var fragmentAddMovieDetailMainMT: MaterialToolbar

    private lateinit var title: String
    private lateinit var id: String
    private lateinit var posterPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_tv_detail, container, false)
        fragmentAddMovieDetailMainMT =
            binding.root.findViewById(R.id.fragment_add_tv_detail_mainMT)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        getMovie()
        setToolbar()
        setListeners()
    }

    private fun getMovie() {
        if (arguments != null) {
            if (requireArguments().getString("id") != "" || requireArguments().getString("id") != "none") {
                id = requireArguments().getString("id")!!
            }
            if (requireArguments().getString("title") != "" || requireArguments().getString("title") != "none") {
                title = requireArguments().getString("title")!!
            }
            if (requireArguments().getString("genreIds") != "" || requireArguments().getString("genreIds") != "none") {
                binding.genre = requireArguments().getString("genreIds")
                binding.isGenreMTVGone = false
            } else {
                binding.isGenreMTVGone = true
            }
            if (requireArguments().getString("releaseDate") != "" || requireArguments().getString("releaseDate") != "none") {
                binding.releaseDate = requireArguments().getString("releaseDate")
                binding.isReleaseDateMTVGone = false
            } else {
                binding.isReleaseDateMTVGone = true
            }
            if (requireArguments().getString("originCountry") != "" || requireArguments().getString("originCountry") != "none") {
                binding.country = requireArguments().getString("originCountry")
                binding.isCountryMTVGone = false
            } else {
                binding.isCountryMTVGone = true
            }
            if (requireArguments().getString("originalLanguage") != "" || requireArguments().getString(
                    "originalLanguage"
                ) != "none"
            ) {
                binding.language = requireArguments().getString("originalLanguage")
                binding.isLanguageMTVGone = false
            } else {
                binding.isLanguageMTVGone = true
            }
            if (requireArguments().getString("voteAverage") != "" || requireArguments().getString("voteAverage") != "none") {
                binding.voteAverage = requireArguments().getString("voteAverage")
                binding.isVoteAverageMTVGone = false
            } else {
                binding.isVoteAverageMTVGone = true
            }
            if (requireArguments().getString("overview") != "" || requireArguments().getString("overview") != "none") {
                binding.overview = requireArguments().getString("overview")
                binding.isOverviewMTVGone = false
            } else {
                binding.isOverviewMTVGone = true
            }
            if (requireArguments().getString("posterPath") != "" || requireArguments().getString("posterPath") != "none") {
                posterPath = "$POSTER_BASE_URL${requireArguments().getString("posterPath")}"
                Glide.with(requireContext())
                    .load(posterPath)
                    .placeholder(R.drawable.logo1)
                    .centerCrop()
                    .into(fragment_add_tv_detail_posterIV)
            }
        }
    }

    private fun setToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(fragmentAddMovieDetailMainMT)
        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)!!.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
        fragmentAddMovieDetailMainMT.setNavigationOnClickListener {
            (activity as AppCompatActivity?)!!.onBackPressed()
        }
    }

    private fun setListeners() {
        fragment_add_mainRB.setOnRatingBarChangeListener { _, rating, _ ->
            if(rating > 0){
                go(rating)
            }
        }
        fragment_add_tv_detail_writeReviewMTV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if(v != null){
            when(v.id){
                R.id.fragment_add_tv_detail_writeReviewMTV -> go(0.0F)
            }
        }
    }

    private fun go(rating: Float){
        val intent = Intent(activity,RateActivity::class.java)
        intent.putExtra("rating",rating.toString())
        intent.putExtra("id",id)
        intent.putExtra("title",title)
        intent.putExtra("mediaType","tv")
        intent.putExtra("posterPath",posterPath)
        startActivity(intent)
    }

}