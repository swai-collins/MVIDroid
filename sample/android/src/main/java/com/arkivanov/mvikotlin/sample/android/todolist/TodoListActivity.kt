package com.arkivanov.mvikotlin.sample.android.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.mvikotlin.base.bind.attachTo
import com.arkivanov.mvikotlin.sample.android.R
import com.arkivanov.mvikotlin.sample.android.app
import com.arkivanov.mvikotlin.sample.mpp.todolist.reaktive.ReaktiveTodoListBinder

class TodoListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.todo_list)

        ReaktiveTodoListBinder(
            storeFactory = app.storeFactory,
            queries = app.todoDatabase.todoItemQueries
        )
            .attachTo(lifecycle)
            .bind(TodoListViewImpl(findViewById(android.R.id.content)))
            .attachTo(lifecycle)
    }
}
