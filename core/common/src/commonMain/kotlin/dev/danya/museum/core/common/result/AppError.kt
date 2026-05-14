package dev.danya.museum.core.common.result

sealed class AppError {
    data object NoInternetError : AppError()
    data class NetworkError(val code: Int?, val message: String) : AppError()
    data class DatabaseError(val cause: Throwable) : AppError()
    data class UnknownError(val cause: Throwable) : AppError()
}
