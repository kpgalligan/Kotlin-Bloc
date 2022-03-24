package com.onegravity.bloc.sample.counter

import com.onegravity.bloc.bloc
import com.onegravity.bloc.context.BlocContext
import com.onegravity.bloc.utils.toBlocState

object ReduxCounter {
    sealed class Action(val value: Int) {
        data class Increment(private val _value: Int = 1): Action(_value)
        data class Decrement(private val _value: Int = 1): Action(_value)
    }

    fun bloc(context: BlocContext) = bloc<Int, Action, ReduxAction>(
        context,
        reduxStore.toBlocState(context = context, initialState = 1)
    ) {
        state<Action.Increment> { ReduxAction.Increment(action.value) }
        state<Action.Decrement> { ReduxAction.Decrement(action.value) }
    }
}

// TODO implement some samples with Compose
// TODO think about the initial value, who/what provides it?
// TODO iOS
