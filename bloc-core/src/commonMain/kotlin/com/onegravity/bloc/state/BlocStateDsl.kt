package com.onegravity.bloc.state

import com.onegravity.bloc.utils.BlocDSL
import kotlin.jvm.JvmName

/**
 * Creates a [BlocState] instance using a [BlocStateBuilder]
 * ```
 *    blocState<State, Proposal> {
 *       initialState = SomeState
 *       accept { proposal, state ->
 *          // map proposal to State
 *       }
 *    }
 * ```
 */
@JvmName("blocState")
@BlocDSL
fun <State, Proposal> blocState(
    block: BlocStateBuilder<State, Proposal>.() -> Unit
): BlocState<State, Proposal> =
    BlocStateBuilderImpl<State, Proposal>()
        .also(block)
        .build()

/**
 * Creates a [BlocState] instance using a [SimpleBlocStateBuilder]
 * (Proposal == State -> no accept function needed):
 * ```
 *    blocState<State>(SomeState)
 * ```
 */
@JvmName("simpleBlocState")
@BlocDSL
fun <State> blocState(
    initialState: State
): BlocState<State, State> =
    SimpleBlocStateBuilderImpl<State>()
        .also { it.initialState = initialState }
        .build()
