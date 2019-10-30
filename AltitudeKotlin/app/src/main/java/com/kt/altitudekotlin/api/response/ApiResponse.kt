package com.kt.altitudekotlin.api.response

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

class ApiResponse<T> {
    var code: Int
    var body: T?
    var errorCode: String?
    var errorMessage: String?

    init {
        code = 500
        body = null
        errorCode = null
        errorMessage = null
    }

    constructor(code: Int, errMsg: String?) {
        this.code = code
        body = null
        errorCode = null
        errorMessage = errMsg
    }

    constructor(error: Throwable) {
        code = 500
        body = null
        errorCode = null
        errorMessage = error.message
    }

    constructor(response: Response<T>) {
        var localCode = -1
        var errorCode: String? = null
        var errorMessage: String? = null
        if (response.isSuccessful) {
            var responseObj: T? = response.body()
            if (responseObj is CommonResponse) {
                if ((responseObj as CommonResponse).isSuccessful()) {
                    localCode = (responseObj as CommonResponse).code
                    errorCode = null
                    errorMessage = null
                } else {
                    localCode = (responseObj as CommonResponse).code
                    errorCode = (responseObj as CommonResponse).error
                    errorMessage = (responseObj as CommonResponse).description
                }
            }
        } else {
            val responseBody = response.errorBody()
            if (responseBody != null) {
                try {
                    val commonResponse = Gson()
                        .fromJson(responseBody.string(), CommonResponse::class.java)
                    if (commonResponse != null) {
                        localCode = commonResponse.code
                        errorCode = commonResponse.error
                        errorMessage = commonResponse.description
                    }
                } catch (e: JsonSyntaxException) {
                    Timber.e(e, "parse error message error")
                } catch (e: IOException) {
                    Timber.e(e)
                }

            }
        }
    }

    fun isSuccessful(): Boolean {
        return code >= 200 && code < 300
    }

    override fun toString(): String {
        return "ApiResponse{" +
                "code=" + code +
                ", body=" + body +
                ", errorMessage='" + errorMessage + '\''.toString() +
                '}'.toString()
    }

}