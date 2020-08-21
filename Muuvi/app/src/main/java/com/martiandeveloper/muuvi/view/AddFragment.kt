package com.martiandeveloper.muuvi.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.adapter.RecyclerViewMovieAdapter
import com.martiandeveloper.muuvi.databinding.FragmentAddBinding
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.repository.MovieDataSource
import com.martiandeveloper.muuvi.service.MovieApi
import com.martiandeveloper.muuvi.service.MovieService
import com.martiandeveloper.muuvi.viewmodel.AddViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AddFragment : Fragment(), View.OnClickListener, RecyclerViewMovieAdapter.ItemClickListener {

    private lateinit var vm: AddViewModel

    private lateinit var binding: FragmentAddBinding

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var adapter: RecyclerViewMovieAdapter

    lateinit var api: MovieApi

    private val compositeDisposable = CompositeDisposable()
    private lateinit var moviePagedList: LiveData<PagedList<Movie>>
//    private lateinit var moviesDataSourceFactory: MovieDataSourceFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = getViewModel()
    }

    private fun getViewModel(): AddViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddViewModel() as T
            }
        })[AddViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add, container, false)
        binding.addViewModel = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        setRecyclerView()
        observe()
        setProgress(isRecyclerViewGone = false, isProgressLLViewGone = true)
        binding.isClearIVGone = true
        setListeners()
        fragment_add_movieSeriesET.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(fragment_add_movieSeriesET, InputMethodManager.SHOW_IMPLICIT)
        api = MovieService.getClient()

    }

    private fun setupView(movie:String) {

        val listData = Pager(PagingConfig(pageSize = 20)) {
            MovieDataSource(movie, api)
        }.flow.cachedIn(lifecycleScope)

        lifecycleScope.launch {
            listData.collect {
                adapter.submitData(it)
            }
        }

        setProgress(
            isRecyclerViewGone = false,
            isProgressLLViewGone = true
        )

        /*val listData = Pager(PagingConfig(pageSize = 20)) {
            MovieDataSource("i", APIService.getApiService())
        }.flow.cachedIn(lifecycleScope)

        lifecycleScope.launch {
            listData.collect {
                adapter.submitData(it)
            }
        }*/
    }

    private fun setRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        adapter = RecyclerViewMovieAdapter(requireContext(), this)
        fragment_add_mainRV.layoutManager = layoutManager
        fragment_add_mainRV.adapter = adapter
    }

    private fun observe() {
        vm.isError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                setProgress(isRecyclerViewGone = false, isProgressLLViewGone = true)
                if (it) {
                    setToast(resources.getString(R.string.something_went_wrong_please_try_again_later))
                }
            }
        })

        vm.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                if (it) {
                    setProgress(isRecyclerViewGone = true, isProgressLLViewGone = false)
                } else {
                    setProgress(isRecyclerViewGone = false, isProgressLLViewGone = true)

                }
            }
        })

        vm.movieSeriesETContent.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                setProgress(isRecyclerViewGone = true, isProgressLLViewGone = false)
                binding.isClearIVGone = false
                if (vm.movieSeriesETContent.value != null) {
                    if (vm.movieSeriesETContent.value != "") {
                        setupView(vm.movieSeriesETContent.value!!)
                        /*fetchLiveMoviePagedList(
                            vm.movieSeriesETContent.value!!
                        )*/
                    }
                }
            } else {
                setProgress(isRecyclerViewGone = true, isProgressLLViewGone = true)
                binding.isClearIVGone = true
            }
        })

    }

    /*private fun fetchLiveMoviePagedList(movie: String) {
        moviesDataSourceFactory = MovieDataSourceFactory(movie, api, compositeDisposable)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        moviePagedList = LivePagedListBuilder(moviesDataSourceFactory, config).build()

        moviePagedList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it) {
                setProgress(
                    isRecyclerViewGone = false,
                    isProgressLLViewGone = true
                )
            }
        })
    }*/

    private fun setProgress(isRecyclerViewGone: Boolean, isProgressLLViewGone: Boolean) {
        if (vm.movieSeriesETContent.value != null) {
            val text =
                "${resources.getString(R.string.searching_for)} \"${vm.movieSeriesETContent.value}\"..."
            binding.searchingFor = text
        }

        binding.isMainRVGone = isRecyclerViewGone
        binding.isProgressLLGone = isProgressLLViewGone
    }

    private fun setToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun setListeners() {
        fragment_add_clearIV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.fragment_add_clearIV -> fragment_add_movieSeriesET.text.clear()
            }
        }
    }

    override fun onItemClick(movie: Movie) {
        var id = ""
        var title = ""
        var genreIds = ""
        var originCountry = ""
        var releaseDate = ""
        var originalLanguage = ""
        var voteAverage = ""
        var overview = ""
        var posterPath = ""

        if (movie.mediaType != null) {
            if (movie.mediaType != "null") {
                if (movie.mediaType != "") {
                    if (movie.mediaType == "movie") {
                        if (movie.id != null) {
                            if (movie.id != 0) {
                                id = movie.id.toString()
                            }
                        }
                        if (movie.title != null) {
                            if (movie.title != "null") {
                                title = movie.title
                            }
                        }
                        if (movie.genreIds != null) {
                            if (movie.genreIds.isNotEmpty()) {
                                for (i in movie.genreIds.indices) {
                                    when {
                                        movie.genreIds[i] == 28 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Action"
                                            } else {
                                                "Action, "
                                            }
                                        }
                                        movie.genreIds[i] == 12 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Adventure"
                                            } else {
                                                "Adventure, "
                                            }
                                        }
                                        movie.genreIds[i] == 16 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Animation"
                                            } else {
                                                "Animation, "
                                            }
                                        }
                                        movie.genreIds[i] == 35 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Comedy"
                                            } else {
                                                "Comedy, "
                                            }
                                        }
                                        movie.genreIds[i] == 80 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Crime"
                                            } else {
                                                "Crime, "
                                            }
                                        }
                                        movie.genreIds[i] == 99 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Documentary"
                                            } else {
                                                "Documentary, "
                                            }
                                        }
                                        movie.genreIds[i] == 18 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Drama"
                                            } else {
                                                "Drama, "
                                            }
                                        }
                                        movie.genreIds[i] == 10751 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Family"
                                            } else {
                                                "Family, "
                                            }
                                        }
                                        movie.genreIds[i] == 14 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Fantasy"
                                            } else {
                                                "Fantasy, "
                                            }
                                        }
                                        movie.genreIds[i] == 36 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "History"
                                            } else {
                                                "History, "
                                            }
                                        }
                                        movie.genreIds[i] == 27 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Horror"
                                            } else {
                                                "Horror, "
                                            }
                                        }
                                        movie.genreIds[i] == 10402 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Music"
                                            } else {
                                                "Music, "
                                            }
                                        }
                                        movie.genreIds[i] == 9648 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Mystery"
                                            } else {
                                                "Mystery, "
                                            }
                                        }
                                        movie.genreIds[i] == 10749 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Romance"
                                            } else {
                                                "Romance, "
                                            }
                                        }
                                        movie.genreIds[i] == 878 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Science Fiction"
                                            } else {
                                                "Science Fiction, "
                                            }
                                        }
                                        movie.genreIds[i] == 10770 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "TV Movie"
                                            } else {
                                                "TV Movie, "
                                            }
                                        }
                                        movie.genreIds[i] == 53 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Thriller"
                                            } else {
                                                "Thriller, "
                                            }
                                        }
                                        movie.genreIds[i] == 10752 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "War"
                                            } else {
                                                "War, "
                                            }
                                        }
                                        movie.genreIds[i] == 37 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Western"
                                            } else {
                                                "Western, "
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (movie.releaseDate != null) {
                            if (movie.releaseDate != "null") {
                                releaseDate = movie.releaseDate
                            }
                        }
                        if (movie.originalLanguage != null) {
                            if (movie.originalLanguage != "null") {
                                originalLanguage = movie.originalLanguage
                            }
                        }
                        if (movie.voteAverage != null) {
                            voteAverage = movie.voteAverage.toString()
                        }
                        if (movie.overview != null) {
                            if (movie.overview != "null") {
                                overview = movie.overview
                            }
                        }
                        if (movie.posterPath != null) {
                            if (movie.posterPath != "null") {
                                posterPath = movie.posterPath
                            }
                        }
                        navigate(
                            AddFragmentDirections.actionAddFragmentToAddMovieDetailFragment(
                                id,
                                title,
                                genreIds,
                                releaseDate,
                                originalLanguage,
                                voteAverage,
                                overview,
                                posterPath
                            )
                        )
                    }
                    if (movie.mediaType == "tv") {
                        if (movie.id != null) {
                            if (movie.id != 0) {
                                id = movie.id.toString()
                            }
                        }
                        if (movie.name != null) {
                            if (movie.name != "null") {
                                title = movie.name
                            }
                        }
                        if (movie.genreIds != null) {
                            if (movie.genreIds.isNotEmpty()) {
                                for (i in movie.genreIds.indices) {
                                    when {
                                        movie.genreIds[i] == 10759 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Action & Adventure"
                                            } else {
                                                "Action & Adventure, "
                                            }
                                        }
                                        movie.genreIds[i] == 16 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Animation"
                                            } else {
                                                "Animation, "
                                            }
                                        }
                                        movie.genreIds[i] == 35 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Comedy"
                                            } else {
                                                "Comedy, "
                                            }
                                        }
                                        movie.genreIds[i] == 80 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Crime"
                                            } else {
                                                "Crime, "
                                            }
                                        }
                                        movie.genreIds[i] == 99 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Documentary"
                                            } else {
                                                "Documentary, "
                                            }
                                        }
                                        movie.genreIds[i] == 18 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Drama"
                                            } else {
                                                "Drama, "
                                            }
                                        }
                                        movie.genreIds[i] == 10751 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Family"
                                            } else {
                                                "Family, "
                                            }
                                        }
                                        movie.genreIds[i] == 10762 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Kids"
                                            } else {
                                                "Kids, "
                                            }
                                        }
                                        movie.genreIds[i] == 9648 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Mystery"
                                            } else {
                                                "Mystery, "
                                            }
                                        }
                                        movie.genreIds[i] == 10763 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "News"
                                            } else {
                                                "News, "
                                            }
                                        }
                                        movie.genreIds[i] == 10764 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Reality"
                                            } else {
                                                "Reality, "
                                            }
                                        }
                                        movie.genreIds[i] == 10765 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Sci-Fi & Fantasy"
                                            } else {
                                                "Sci-Fi & Fantasy, "
                                            }
                                        }
                                        movie.genreIds[i] == 10766 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Soap"
                                            } else {
                                                "Soap, "
                                            }
                                        }
                                        movie.genreIds[i] == 10767 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Talk"
                                            } else {
                                                "Talk, "
                                            }
                                        }
                                        movie.genreIds[i] == 10768 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "War & Politics"
                                            } else {
                                                "War & Politics, "
                                            }
                                        }
                                        movie.genreIds[i] == 37 -> {
                                            genreIds += if (movie.genreIds.lastIndex == i) {
                                                "Western"
                                            } else {
                                                "Western, "
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (movie.originCountry != null) {
                            if (movie.originCountry.isNotEmpty()) {
                                for (i in movie.originCountry.indices) {
                                    originCountry += if (movie.originCountry.lastIndex == i) {
                                        movie.originCountry[i]
                                    } else {
                                        movie.originCountry[i] + ", "
                                    }
                                }
                            }
                        }
                        if (movie.firstAirDate != null) {
                            if (movie.firstAirDate != "null") {
                                releaseDate = movie.firstAirDate
                            }
                        }
                        if (movie.originalLanguage != null) {
                            if (movie.originalLanguage != "null") {
                                originalLanguage = movie.originalLanguage
                            }
                        }
                        if (movie.voteAverage != null) {
                            voteAverage = movie.voteAverage.toString()
                        }
                        if (movie.overview != null) {
                            if (movie.overview != "null") {
                                overview = movie.overview
                            }
                        }
                        if (movie.posterPath != null) {
                            if (movie.posterPath != "null") {
                                posterPath = movie.posterPath
                            }
                        }
                        navigate(
                            AddFragmentDirections.actionAddFragmentToAddTvDetailFragment(
                                id,
                                title,
                                genreIds,
                                releaseDate,
                                originCountry,
                                originalLanguage,
                                voteAverage,
                                overview,
                                posterPath
                            )
                        )
                    }
                }
            }
        }
    }

    private fun navigate(action: NavDirections) {
        val imm =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(fragment_add_movieSeriesET.windowToken, 0)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }
}