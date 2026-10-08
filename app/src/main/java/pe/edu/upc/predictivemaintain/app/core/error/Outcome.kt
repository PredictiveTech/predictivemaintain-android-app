package pe.edu.upc.predictivemaintain.app.core.error

sealed class Outcome<out T> {
    data class Success<out T>(val data: T) : Outcome<T>()
    data class Failure(val error: AppError) : Outcome<Nothing>()

    inline fun <R> map(transform: (T) -> R): Outcome<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> Failure(error)
        }
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (AppError) -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Failure -> onFailure(error)
        }
    }

    inline fun onSuccess(action: (T) -> Unit): Outcome<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (AppError) -> Unit): Outcome<T> {
        if (this is Failure) action(error)
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }
}
