package com.pe.simswappingsimulator.module

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
class BasicAuthInterceptor(private val username: String, private val password: String) : Interceptor {

    private val credentials: String by lazy {
        okhttp3.Credentials.basic(username, password)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val requestBuilder: Request.Builder = originalRequest.newBuilder()
            .header("Authorization", credentials)

        val request: Request = requestBuilder.build()
        return chain.proceed(request)
    }
}