package com.martiandeveloper.muuvi.service

import com.martiandeveloper.muuvi.model.MovieResult
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MovieApi {
    @GET("search/multi")
    suspend fun getMovie(@Query("query") movie: String, @Query("page") page: Int): Response<MovieResult>

}