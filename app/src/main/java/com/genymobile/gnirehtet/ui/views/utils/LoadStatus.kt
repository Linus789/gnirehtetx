package com.genymobile.gnirehtet.ui.views.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

sealed interface LoadStatus<out T> {
    object Loading : LoadStatus<Nothing>
    class Data<out T>(val data: T) : LoadStatus<T>

    fun <R> map(f: (T) -> R): LoadStatus<R> {
        return when (this) {
            is Loading -> Loading
            is Data -> Data(f(this.data))
        }
    }

    @Composable
    fun <R> mapComposable(f: @Composable (T) -> R): LoadStatus<R> {
        return when (this) {
            is Loading -> Loading
            is Data -> Data(f(this.data))
        }
    }
}

@Composable
fun <T> LoadData(
    loadStatus: LoadStatus<T>,
    content: @Composable (T) -> Unit,
) {
    when (loadStatus) {
        is LoadStatus.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadStatus.Data -> {
            content(loadStatus.data)
        }
    }
}
