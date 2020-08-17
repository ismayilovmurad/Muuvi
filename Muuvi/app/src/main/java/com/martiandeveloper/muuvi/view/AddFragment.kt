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
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.adapter.RecyclerViewMovieAdapter2
import com.martiandeveloper.muuvi.databinding.FragmentAddBinding
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.repository.MovieDataSourceFactory
import com.martiandeveloper.muuvi.repository.MoviePagedListRepository
import com.martiandeveloper.muuvi.service.MovieApi
import com.martiandeveloper.muuvi.service.MovieService
import com.martiandeveloper.muuvi.service.POST_PER_PAGE
import com.martiandeveloper.muuvi.viewmodel.AddViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_add.*


class AddFragment : Fragment(), View.OnClickListener {

    private lateinit var vm: AddViewModel

    private lateinit var binding: FragmentAddBinding

    private lateinit var layoutManager: LinearLayoutManager

    lateinit var movieRepository: MoviePagedListRepository

    lateinit var adapter: RecyclerViewMovieAdapter2

    lateinit var apiService:MovieApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = MovieService.getClient()

        movieRepository = MoviePagedListRepository(apiService)

        vm = getViewModel()
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

    private fun getViewModel(): AddViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddViewModel(movieRepository) as T
            }
        })[AddViewModel::class.java]
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
    }

    private fun setRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        adapter = RecyclerViewMovieAdapter2(requireContext())
        fragment_add_mainRV.layoutManager = layoutManager
        fragment_add_mainRV.adapter = adapter
    }

    private val compositeDisposable = CompositeDisposable()
    private fun observe() {
        /*vm.movieList.observe(viewLifecycleOwner, Observer { movieList ->
            movieList?.let {
                adapter.updateMovieList(it)
            }
        })*/

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
                fetchLiveMoviePagedList(it,compositeDisposable)
                binding.isClearIVGone = false
            } else {
                binding.isClearIVGone = true
            }
        })

        /*vm.moviePagedList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })*/

    }

    lateinit var moviePagedList: LiveData<PagedList<Movie>>
    lateinit var moviesDataSourceFactory: MovieDataSourceFactory

    fun fetchLiveMoviePagedList (movie:String,compositeDisposable: CompositeDisposable) {
        moviesDataSourceFactory = MovieDataSourceFactory(movie,apiService, compositeDisposable)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        moviePagedList = LivePagedListBuilder(moviesDataSourceFactory, config).build()

        moviePagedList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

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
}