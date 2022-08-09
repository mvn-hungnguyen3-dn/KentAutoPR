package kent.api

import io.appium.java_client.android.Activity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("api/v6/activities?")
    fun getActivity(
        @Header("Authorization") token: String?,
        @Header("app-version") appVersion: String?,
        @Query("date") date: String?
    ): Call<Activity?>?
}
