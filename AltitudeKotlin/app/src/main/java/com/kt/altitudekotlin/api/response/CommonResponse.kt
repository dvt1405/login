package com.kt.altitudekotlin.api.response

import java.io.Serializable

open class CommonResponse:Serializable {
    var code: Int = 0
    var description: String? = null
    var error: String? = null

//  public CommonResponse(int status, String message) {
//    this.status = status;
//    this.message = message;
//  }

    fun isSuccessful(): Boolean {
        return code >= 200 && code < 300
    }

    override fun toString(): String {
        return "CommonResponse{" +
                "status=" + code +
                ", message='" + description + '\''.toString() +
                '}'.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as CommonResponse?

        if (code != that!!.code) {
            return false
        }
        return if (description != null) description == that!!.description else that!!.description == null
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + if (description != null) description!!.hashCode() else 0
        return result
    }
}