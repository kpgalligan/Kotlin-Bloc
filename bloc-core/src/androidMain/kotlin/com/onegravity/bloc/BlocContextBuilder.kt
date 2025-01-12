/**
 * From https://github.com/arkivanov/Decompose
 */

@file:Suppress("SpellCheckingInspection")

package com.onegravity.bloc

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.arkivanov.essenty.backpressed.BackPressedHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.onegravity.bloc.context.BlocContext
import com.onegravity.bloc.context.DefaultBlocContext
import com.onegravity.bloc.utils.BlocDSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Use this from an Activity to get or create a "Component" without directly involving a ViewModel,
 * e.g.:
 * ```
 *   val component = blocContext { MyComponent(it) }      // it is the BlocContext
 * ```
 * or:
 * ```
 *   val bloc = blocContext { bloc<Int, Int>(it, 2) { ... } }
 * ```
 * Any class that needs a BlocContext to be instantiated is considered a "Component" in the context
 * of this function.
 *
 * The component will be tied to a ViewModel which is created "on the fly" and the component
 */
inline fun <A, reified Component : Any> A.getOrCreate(
    noinline createInstance: (context: BlocContext) -> Component
): Lazy<Component> where
        A : SavedStateRegistryOwner,
        A : OnBackPressedDispatcherOwner,
        A : ViewModelStoreOwner,
        A : LifecycleOwner =
    ComponentLazy(ActivityLazy { this }, Component::class, createInstance)

/**
 * The same from a fragment
 */
inline fun <reified Component: Any> Fragment.getOrCreate(
    noinline createInstance: (context: BlocContext) -> Component
): Lazy<Component> = ComponentLazy(ActivityLazy { requireActivity() }, Component::class, createInstance)

/**
 * We wrap a component into an InstanceWrapper so that components don't have to implement the
 * InstanceKeeper.Instance interface.
 */
class InstanceWrapper<C>(val component: C) : InstanceKeeper.Instance {
    override fun onDestroy() {}
}

/**
 * This creates the actual BlocContext.
 *
 * It creates a ViewModel and stores it in Android's ViewModelStore. The ViewModel is then used to
 * create the Lifecycle and the InstanceKeeper while the SavedStateRegistry and the
 * OnBackPressedDispatcher are "taken" from the Activity.
 */
fun <T> T.createBlocContext(): DefaultBlocContext where
        T : SavedStateRegistryOwner,
        T : OnBackPressedDispatcherOwner,
        T : ViewModelStoreOwner,
        T : LifecycleOwner {
    val viewModel = viewModelStore.blocViewModel()
    return DefaultBlocContext(
        lifecycle = viewModel.lifecycleRegistry,
        stateKeeper = savedStateRegistry.let(::StateKeeper),
        instanceKeeper = viewModel.instanceKeeperDispatcher,
        backPressedHandler = onBackPressedDispatcher.let(::BackPressedHandler)
    )
}

/**
 * Get or create the ViewModel.
 */
private fun ViewModelStore.blocViewModel(): BlocViewModel =
    ViewModelProvider(
        this,
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>) = BlocViewModel() as T
        }
    ).get()

internal class BlocViewModel : ViewModel() {
    val lifecycleRegistry = LifecycleRegistry()
    val instanceKeeperDispatcher = InstanceKeeperDispatcher()

    init {
        lifecycleRegistry.create()
    }

    override fun onCleared() {
        lifecycleRegistry.destroy()
        instanceKeeperDispatcher.destroy()
    }
}

/** --------------------------------------------------------------------------------------------- */

// TODO reconsider if we really need the ActivityBlocContext, it adds a lot of complexity

/**
 * If we're using a ViewModel, the BlocContext will be created in that ViewModel but the
 * SavedStateRegistry, ViewModelStore and OnBackPressedDispatcher are still "taken" from the
 * Activity, hence we need to pass those to the ViewModel (as ActivityBlocContext).
 */
data class ActivityBlocContext(
    val savedStateRegistry: SavedStateRegistry? = null,
    val viewModelStore: ViewModelStore? = null,
    val onBackPressedDispatcher: OnBackPressedDispatcher? = null
)

/**
 * Converts an ActivityBlocContext into a BlocContext.
 * The lifecycle will be the lifecycle of the ViewModel (onCreate() and onDestroy() only)
 */
fun ViewModel.blocContext(context: ActivityBlocContext): BlocContext =
    DefaultBlocContext(
        lifecycle = viewModelLifeCycle().asEssentyLifecycle(),
        stateKeeper = context.savedStateRegistry?.let(::StateKeeper),
        instanceKeeper = context.viewModelStore?.let(::InstanceKeeper),
        backPressedHandler = context.onBackPressedDispatcher?.let(::BackPressedHandler)
    )

/**
 * To create a ViewModel "lifecycle" we create a Coroutine.
 * Upon launch the lifecycle moves to CREATED. When the Coroutine is cancelled we take that as
 * the cue to move to DESTROYED.
 *
 * Why do we do all this? Because ViewModels don't have an observable lifecycle and we'd have to
 * have a "hook" into the ViewModel's onCleared() call to create that lifecycle. The ViewModel
 * would have to extend some BaseViewModel and we don't want that.
 */
private fun ViewModel.viewModelLifeCycle(): Lifecycle = object : LifecycleOwner {
    override fun getLifecycle() = lifecycleRegistry
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
            while (isActive) {
                delay(Long.MAX_VALUE)
            }
        }.invokeOnCompletion {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }
}.lifecycle

/**
 * From an Activity retrieve a ViewModel that takes the ActivityBlocContext as a parameter.
 */
@Suppress("UNCHECKED_CAST")
@BlocDSL
inline fun <reified VM : ViewModel> ComponentActivity.viewModel(
    crossinline createInstance: (context: ActivityBlocContext) -> VM
): Lazy<VM> {
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val context = ActivityBlocContext(
                savedStateRegistry = savedStateRegistry,
                viewModelStore = viewModelStore,
                onBackPressedDispatcher = onBackPressedDispatcher
            )
            return createInstance(context) as T
        }
    }
    return ViewModelLazy(VM::class, { viewModelStore }, { factory })
}

/**
 * Same for fragments.
 */
@Suppress("UNCHECKED_CAST")
@BlocDSL
inline fun <reified VM : ViewModel> Fragment.viewModel(
    crossinline createInstance: (context: ActivityBlocContext) -> VM
): Lazy<VM> {
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val context = ActivityBlocContext(
                savedStateRegistry = savedStateRegistry,
                viewModelStore = viewModelStore,
                onBackPressedDispatcher = activity?.onBackPressedDispatcher
            )
            return createInstance(context) as T
        }
    }
    return ViewModelLazy(VM::class, { viewModelStore }, { factory })
}
/** --------------------------------------------------------------------------------------------- */

/**
 * Creates DefaultBlocContext for compose previews with a Composable life cycle but without the
 * other parameters (StateKeeper, InstanceKeeper, BackPressedHandler)
 */
@Composable
fun previewBlocContext(): BlocContext = DefaultBlocContext(composableLifecycle())
