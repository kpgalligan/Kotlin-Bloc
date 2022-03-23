/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package com.onegravity.bloc.posts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onegravity.bloc.R
import com.onegravity.bloc.databinding.PostListFragmentBinding
import com.onegravity.bloc.factory
import com.onegravity.bloc.sample.posts.bloc.PostList
import com.onegravity.bloc.subscribe
import com.onegravity.bloc.utils.viewBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.onegravity.bloc.sample.posts.bloc.PostListState

class PostListFragment : Fragment(R.layout.post_list_fragment) {

    private val viewModel: PostListViewModel by viewModels { factory { PostListViewModel(it) } }

    private val binding by viewBinding<PostListFragmentBinding>()

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setTitle(R.string.app_name)
            setLogo(R.drawable.ic_bloc_toolbar)
        }

        binding.content.layoutManager = LinearLayoutManager(activity)

        binding.content.addItemDecoration(
            SeparatorDecoration(requireActivity(), R.dimen.separator_margin_start_icon, R.dimen.separator_margin_end)
        )

        binding.content.adapter = adapter

        viewModel.subscribe(state = ::render, sideEffect = ::sideEffect)
    }

    private fun render(state: PostListState) {
        val posts = state.overviews
            .sortedBy { it.title }
            .map { PostListItem(it, viewModel) }
        adapter.update(posts)
    }

    private fun sideEffect(sideEffect: PostList.OpenPost) {
        findNavController().navigate(
            PostListFragmentDirections.actionListFragmentToDetailFragment(
                sideEffect.postOverview
            )
        )
    }
}
