package com.arkivanov.mvikotlin.sample.android.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.mvikotlin.core.store.MviDefaultStoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.android.attachTo
import com.arkivanov.mvikotlin.sample.android.app
import com.arkivanov.mvikotlin.sample.mpp.todolist.reaktive.ReaktiveTodoListBinder

class TodoListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ReaktiveTodoListBinder
            .bind(
                storeFactory = MviDefaultStoreFactory,
                queries = app.todoDabase.todoItemQueries,
                view = TodoListViewImpl()
            )
            .start()
//            .attachTo(lifecycle)
    }
}
