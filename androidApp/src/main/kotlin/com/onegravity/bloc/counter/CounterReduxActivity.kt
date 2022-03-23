package com.onegravity.bloc.counter

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.onegravity.bloc.R
import com.onegravity.bloc.bind
import com.onegravity.bloc.databinding.ActivityCounterReduxBinding
import com.onegravity.bloc.factory

class CounterReduxActivity : AppCompatActivity() {

    private val viewModel by viewModels<CounterReduxViewModel> { factory { CounterReduxViewModel(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind<ActivityCounterReduxBinding>(R.layout.activity_counter_redux) { it.viewmodel = viewModel }
    }

}