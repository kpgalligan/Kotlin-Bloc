package com.onegravity.bloc.utils

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A StateStream and a Sink at the same time.
 *
 * It's a wrapper around MutableStateFlow with the extra Sink functionality.
 */
class MutableStateStream<Value>(initialValue: Value) :
    StateStream<Value>,
    Sink<Value> {

    private val state = MutableStateFlow(initialValue)

    override val value: Value
        get() = state.value

    override suspend fun collect(collector: FlowCollector<Value>) {
        state.collect(collector)
    }

    override fun send(value: Value) {
        state.tryEmit(value)
    }

}
