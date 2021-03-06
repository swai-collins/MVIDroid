package com.arkivanov.mvidroid.store.defaultstore

import com.arkivanov.mvidroid.store.component.MviBootstrapper
import com.arkivanov.mvidroid.store.component.MviExecutor
import com.arkivanov.mvidroid.store.component.MviReducer
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.disposables.Disposable
import io.reactivex.observers.TestObserver
import org.junit.Assert.*
import org.junit.Test

internal class MviDefaultStoreTest {

    private var executorHolder = ExecutorHolder()
    private var store = createStore()

    @Test
    fun `executor invoked WHEN bootstrapper dispatched action`() {
        createStore(
            bootstrapper = mockBootstrapper { dispatch ->
                dispatch("action")
                null
            }
        )
        verify(executorHolder.executor).execute("action")
    }

    @Test
    fun `disposable returned from bootstrapper disposed WHEN store is disposed`() {
        val disposable = mock<Disposable>()
        createStore(bootstrapper = mockBootstrapper { disposable }).dispose()
        verify(disposable).dispose()
    }

    @Test
    fun `executor initialized WHEN store created`() {
        verify(executorHolder.executor).init(any(), any(), any())
    }

    @Test
    fun `executor can read store's state`() {
        assertEquals("state", executorHolder.stateSupplier())
    }

    @Test
    fun `state updated WHEN executor dispatched result`() {
        executorHolder.resultConsumer("result")
        assertEquals("result", store.state)
    }

    @Test
    fun `state emitted WHEN executor dispatched result`() {
        lateinit var state: String
        store.states.subscribe { state = it }
        executorHolder.resultConsumer("result")
        assertEquals("result", state)
    }

    @Test
    fun `label emitted WHEN executor published label`() {
        lateinit var label: String
        store.labels.subscribe { label = it }
        executorHolder.labelConsumer("label")
        assertEquals("label", label)
    }

    @Test
    fun `last label not emitted WHEN resubscribed`() {
        var label: String? = null
        executorHolder.labelConsumer("label")
        store.labels.subscribe { label = it }
        assertNull(label)
    }

    @Test
    fun `executor invoked WHEN intent send`() {
        val store = createStore(executorHolder = ExecutorHolder())
        store.accept("intent")
        verify(executorHolder.executor).execute("intent")
    }

    @Test
    fun `disposable returned from executor disposed WHEN store is disposed`() {
        val disposable = mock<Disposable>()
        createStore(executorHolder = ExecutorHolder { disposable })
        store.accept("intent")
        store.dispose()
        verify(disposable).dispose()
    }

    @Test
    fun `isDisposed=true WHEN store disposed`() {
        store.dispose()
        assertTrue(store.isDisposed)
    }

    @Test
    fun `states observable completed WHEN store disposed`() {
        val observer = TestObserver<String>()
        store.states.subscribe(observer)
        store.dispose()
        observer.assertComplete()
    }

    @Test
    fun `labels observable completed WHEN store disposed`() {
        val observer = TestObserver<String>()
        store.labels.subscribe(observer)
        store.dispose()
        observer.assertComplete()
    }

    @Test
    fun `valid state read for last intent WHEN two intents for label`() {
        lateinit var lastState: String
        createStore(
            executorHolder = ExecutorHolder {
                if (it == "intent2") {
                    lastState = stateSupplier()
                }
                resultConsumer(it)
                null
            }
        )
        store.labels.subscribe {
            store.accept("intent1")
            store.accept("intent2")
        }
        executorHolder.labelConsumer("label")
        assertEquals("intent1", lastState)
    }

    private fun createStore(
        bootstrapper: MviBootstrapper<String>? = null,
        executorHolder: ExecutorHolder = this.executorHolder
    ): MviDefaultStore<String, String, String, String, String> {
        this.executorHolder = executorHolder
        store = MviDefaultStore(
            "state",
            bootstrapper,
            { it },
            executorHolder.executor,
            object : MviReducer<String, String> {
                override fun String.reduce(result: String): String = result
            }
        )

        return store
    }

    private fun mockBootstrapper(onBootstrap: (dispatch: (String) -> Unit) -> Disposable?): MviBootstrapper<String> =
        mock { _ ->
            on { bootstrap(any()) }.thenAnswer { onBootstrap(it.getArgument(0)) }
        }

    private class ExecutorHolder(onExecute: (ExecutorHolder.(label: String) -> Disposable?) = { null }) {
        var isInitialized: Boolean = false
        lateinit var stateSupplier: () -> String
        lateinit var resultConsumer: (String) -> Unit
        lateinit var labelConsumer: (String) -> Unit

        val executor = mock<MviExecutor<String, String, String, String>> {
            on { init(any(), any(), any()) }.thenAnswer { invocation ->
                isInitialized = true
                stateSupplier = invocation.getArgument(0)
                resultConsumer = invocation.getArgument(1)
                labelConsumer = invocation.getArgument(2)
                Unit
            }

            on { execute(any()) }.thenAnswer { onExecute(it.getArgument(0)) }
        }
    }
}
