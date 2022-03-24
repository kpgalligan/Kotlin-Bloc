package com.onegravity.bloc.sample.books

import com.github.michaelbull.result.mapBoth
import com.onegravity.bloc.bloc
import com.onegravity.bloc.context.BlocContext
import com.onegravity.bloc.state.ReduxBlocState
import com.onegravity.bloc.utils.toBlocState
import com.onegravity.bloc.utils.toObservable

/**
 * Implements the BooksUseCase with a single Bloc and a [ReduxBlocState].
 */
class BooksUseCaseImplRedux(
    context: BlocContext,
    private val repository: BooksRepository,
) : BooksUseCase {
    private fun BookResult.toState() =
        mapBoth(
            { if (it.isEmpty()) BookState.Empty else BookState.Loaded(it) },
            {
                val message = when (it) {
                    is BooksRepository.Failure.Network -> "Network error. Check Internet connection and try again."
                    is BooksRepository.Failure.Generic -> "Generic error, please try again."
                }
                BookState.Failure(message)
            }
        )

    private val blocState = reduxStore.toBlocState<BookState, Any, ReduxModel>(
        context = context,
        initialState = BookState.Empty,
        selector = { reduxModel ->
            when {
                reduxModel.isLoading -> BookState.Loading
                else -> reduxModel.books.toState()
            }
        }
    )

    sealed class BookAction {
        object Load : BookAction()
        object Clear : BookAction()
    }

    private val bloc = bloc<BookState, BookAction, Nothing, Any>(context, blocState) {
        reduce<BookAction.Load> { ReduxProposal.Load(blocState.coroutineScope, repository) }
        reduce<BookAction.Clear> { ReduxProposal.Clear }
    }

    override val observable = bloc.toObservable()

    override fun load() {
        bloc.send(BookAction.Load)
    }

    override fun clear() {
        bloc.send(BookAction.Clear)
    }

}