package com.onegravity.bloc.utils

import com.onegravity.bloc.fsm.Matcher

data class MatcherReducer<State, Action: Any, Proposal>(
    val matcher: Matcher<Action, Action>?,
    val reducer: Reducer<State, Action, Proposal>,
    val expectsProposal: Boolean
)
