package com.rein.android.requeststest

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


class MainActivity : AppCompatActivity() {
    private lateinit var btn :Button

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.send_button)

        btn.setOnClickListener {

            var body = JSONObject()
            body.put("Extra Body", "Some body")

            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    println("checkClientTrusted")
                }

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    println("checkServerTrusted")
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    println("getAcceptedIssuers")
                    return arrayOf()
                }
            }
            val sslSocketFactory = with(SSLContext.getInstance("SSL")) {
                init(null, arrayOf(trustManager), SecureRandom())
                socketFactory
            }


            Retrofit.Builder()
                .baseUrl("https://webhook.site/")
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                        .hostnameVerifier { host, ssl -> true }
                        .sslSocketFactory(sslSocketFactory, trustManager)
                        .build()
                )
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Net::class.java).get().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    {
                        println(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        }

    }
}

interface Net {
    @Headers("x-authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJYLUZpbmdlcnByaW50IjoiOWM4YmE4MTNhZjIzNzIzNGRmMjZiOTVhNjg5M2ZkYTYiLCJ1c2VyX25hbWUiOiJkLnJleW5AZXZvdG9yLnJ1IiwieF91c2VyX2lkIjoiNDBkMjQ4MjYtMzk1ZS00ZDUyLWE5NDAtZmViMjMzOTgzZWM4Iiwic2NvcGUiOlsicmVhZCIsIndyaXRlIiwicHVyY2hhc2UiLCIyZmE6ZGlzYWJsZSJdLCJleHAiOjE2MzIyOTQ2ODQsImlhdCI6MTYzMjI5Mjg4NDk2MSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9QVUJMSVNIRVIiXSwianRpIjoiZDA5NmM4OTQtNDhlZi00NGM4LWIwNTctOTdhZDliODNhZjQzIiwieF91aWQiOm51bGwsImNsaWVudF9pZCI6IkV2by1VSSIsInhfbG9uZ2xpdmVkIjpmYWxzZX0.BsIHJZYu2JIPi6nYd74o9otm3erdRnYVASAAynoKrzQ")
    @GET("72514704-fddc-47ac-bca9-0eb78e9f39fb")
    //fun get(@Body body: String?): Single<String>
    fun get(): Single<String>
}
