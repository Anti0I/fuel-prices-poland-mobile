package com.example.fuel_prices.data

import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP client for the Fuel Price Aggregator API.
 * All methods perform synchronous network calls and must be called
 * from a background thread (e.g. inside a coroutine with Dispatchers.IO).
 */
class FuelApiService(
    baseUrl: String = DEFAULT_BASE_URL
) {
    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"
    }

    private val baseUrl: HttpUrl = baseUrl.toHttpUrl()
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches stations near the given coordinates.
     * Maps to: GET /stations/near?lat=...&lng=...&radius=...&limit=...
     */
    fun fetchStationsNear(
        lat: Double,
        lng: Double,
        radius: Double = 50.0,
        limit: Int = 15
    ): List<Station> {
        val url = baseUrl.newBuilder()
            .addPathSegment("stations")
            .addPathSegment("near")
            .addQueryParameter("lat", lat.toString())
            .addQueryParameter("lng", lng.toString())
            .addQueryParameter("radius", radius.toString())
            .addQueryParameter("limit", limit.toString())
            .build()

        val response = executeRequest<StationsResponse>(url, StationsResponse::class.java)
        return response.stations
    }

    /**
     * Executes a GET request and deserializes the JSON response body.
     * @throws IOException on network or parsing errors.
     */
    private fun <T> executeRequest(url: HttpUrl, type: java.lang.reflect.Type): T {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
            ?: throw IOException("Empty response body from $url")

        if (!response.isSuccessful) {
            throw IOException("API error ${response.code}: $body")
        }

        return gson.fromJson(body, type)
    }
}
