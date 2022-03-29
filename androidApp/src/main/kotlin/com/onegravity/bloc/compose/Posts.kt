package com.onegravity.bloc.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onegravity.bloc.sample.posts.domain.repositories.PostOverview
import com.skydoves.landscapist.glide.GlideImage

@Composable
internal fun PostsList(
    model: List<PostOverview>,
    modifier: Modifier = Modifier,
    onClicked: (post: PostOverview) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(model) { post ->
            val isSelected = false // todo article.id == model.selectedArticleId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onClicked(post) }
                    )
                    .run { if (isSelected) background(color = selectionColor()) else this }
                    .padding(16.dp)

            ) {
                Box(
                    modifier = Modifier.height(80.dp).width(80.dp).padding(16.dp)
                ) {
                    GlideImage(
                        imageModel = post.avatarUrl,
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(modifier = Modifier.matchParentSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = post.username,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)
                    )
                    Text(
                        text = post.title,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)
                    )
                }
            }

            Divider()
        }
    }
}

/*
Glide.with(viewBinding.root.context).load(post.avatarUrl)
.apply(RequestOptions.circleCropTransform()).into(viewBinding.postAvatar)

viewBinding.postTitle.text = post.title
viewBinding.postUsername.text = post.username


 */
@Composable
private fun selectionColor(): Color =
    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
