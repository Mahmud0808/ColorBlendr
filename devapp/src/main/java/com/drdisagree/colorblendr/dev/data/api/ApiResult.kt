package com.drdisagree.colorblendr.dev.data.api

sealed class ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>()

    data class Failure(val code: Int?, val error: String?) : ApiResult<Nothing>()
}