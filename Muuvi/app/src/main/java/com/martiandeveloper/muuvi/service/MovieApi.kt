package com.martiandeveloper.muuvi.service

import com.martiandeveloper.muuvi.model.MovieResult
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {
    @GET("search/multi")
    fun getMovie(@Query("query") movie: String, @Query("page") page: Int): Single<MovieResult>
}