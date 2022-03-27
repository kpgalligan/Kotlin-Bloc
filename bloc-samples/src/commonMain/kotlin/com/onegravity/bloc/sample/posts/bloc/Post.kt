package com.onegravity.bloc.sample.posts.bloc

import com.github.michaelbull.result.Result
import com.onegravity.bloc.bloc
import com.onegravity.bloc.context.BlocContext
import com.onegravity.bloc.util.getKoinInstance
import com.onegravity.bloc.sample.posts.domain.repositories.PostDetail
import com.onegravity.bloc.sample.posts.domain.repositories.PostOverview
import com.onegravity.bloc.sample.posts.domain.repositories.PostRepository

object Post {
    sealed class Action {
        object Load : Action()
        object Loading : Action()
        data class Loaded(val post: Result<PostDetail, Exception>) : Action()
    }

    fun bloc(context: BlocContext, overview: PostOverview) = bloc<PostState, Action>(
        context,
        PostState(overview)
    ) {
        val repository = getKoinInstance<PostRepository>()

        onCreate {
            dispatch(Action.Load)
        }

        thunk<Action.Load> {
            dispatch(Action.Loading)
            val postOverview = getState().postOverview
            val result = repository.getDetail(postOverview.id)
            dispatch(Action.Loaded(result))
        }

        reduce<Action.Loading> { state.copy(loading = true) }

        reduce<Action.Loaded> {
            state.copy(loading = false, post = action.post)
        }
    }
}