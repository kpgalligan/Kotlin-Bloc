package com.genaku.reduce.books

import com.onegravity.knot.Stream
import kotlinx.coroutines.CoroutineScope

interface IBooksUseCase {
    val state: Stream<BooksState>

    fun start(coroutineScope: CoroutineScope)
    fun load()
    fun clear()
}