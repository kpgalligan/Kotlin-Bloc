package com.onegravity.bloc.posts

import androidx.lifecycle.ViewModel
import com.onegravity.bloc.*
import com.onegravity.bloc.sample.posts.bloc.Post
import com.onegravity.bloc.sample.posts.bloc.PostState
import com.onegravity.bloc.sample.posts.domain.repositories.PostOverview
import com.onegravity.bloc.utils.BlocObservableOwner
import com.onegravity.bloc.toObservable
import org.koin.core.component.KoinComponent

class PostDetailsViewModel(context: ActivityBlocContext, postOverview: PostOverview) :
    ViewModel(),
    BlocObservableOwner<PostState, Unit>,
    KoinComponent {

    override val observable = Post.bloc(blocContext(context), postOverview).toObservable()

}