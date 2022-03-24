package com.onegravity.bloc.sample.posts.data

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.binding.binding
import kotlinx.coroutines.coroutineScope
import com.onegravity.bloc.sample.posts.data.posts.network.AvatarUrlGenerator
import com.onegravity.bloc.sample.posts.data.posts.network.PostNetworkDataSource
import com.onegravity.bloc.sample.posts.domain.repositories.PostComment
import com.onegravity.bloc.sample.posts.domain.repositories.PostDetail
import com.onegravity.bloc.sample.posts.domain.repositories.PostOverview
import com.onegravity.bloc.sample.posts.domain.repositories.PostRepository

class PostDataRepository(
    private val networkDataSource: PostNetworkDataSource,
    private val avatarUrlGenerator: AvatarUrlGenerator
) : PostRepository {
    override suspend fun getOverviews(): Result<List<PostOverview>, Exception> =
        coroutineScope {
            binding {
                val users = networkDataSource.getUsers().bind()
                networkDataSource.getPosts().bind().map { post ->
                    val user = users.first { it.id == post.userId }
                    PostOverview(
                        post.id,
                        avatarUrlGenerator.generateUrl(user.email),
                        post.title,
                        user.name
                    )
                }
            }
        }

    override suspend fun getDetail(id: Int): Result<PostDetail, Exception> =
        coroutineScope {
            binding {
                val postData = networkDataSource.getPost(id).bind()
                val comments = networkDataSource.getComments().bind()
                    .filter { comment -> comment.postId == postData.id }
                PostDetail(
                    postData.id,
                    postData.body,
                    comments.map { PostComment(it.id, it.name, it.email, it.body) }
                )
            }
        }

}