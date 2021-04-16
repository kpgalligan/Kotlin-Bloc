package com.genaku.reduce.station

import com.genaku.reduce.Intent
import com.genaku.reduce.State

sealed class StationState : State {
    object Empty : StationState()
    class Train(val name: String) : StationState()
    class Bus(val name: String) : StationState()
    class Lorry(val name: String) : StationState()

    fun getValue() = when (this) {
        Empty -> ""
        is Bus -> this.name
        is Lorry -> this.name
        is Train -> this.name
    }
}

sealed class TrainIntent : Intent {
    class Arrive(val name: String) : TrainIntent()
    class Leave(val name: String) : TrainIntent()
}

sealed class BusIntent : Intent {
    class Arrive(val name: String) : BusIntent()
    class Leave(val name: String) : BusIntent()
}

sealed class LorryIntent : Intent {
    class Arrive(val name: String) : LorryIntent()
    class Leave(val name: String) : LorryIntent()
}