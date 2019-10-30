package com.kt.altitudekotlin.api.calladapter

import androidx.lifecycle.LiveData
import com.kt.altitudekotlin.api.response.ApiResponse
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != LiveData::class.java) return null
        var observableType: Type = getParameterUpperBound(0, returnType as ParameterizedType)
        var rawObservableType: Class<*> = getRawType(observableType)
        require(rawObservableType == ApiResponse::class.java) { "type must be a resource" }
        require(observableType is ParameterizedType) { "resource must be parameterized" }
        var bodyType: Type = getParameterUpperBound(0, observableType)
        return LiveDataCallAdapter<Any>(bodyType) as CallAdapter<*, *>
    }
}