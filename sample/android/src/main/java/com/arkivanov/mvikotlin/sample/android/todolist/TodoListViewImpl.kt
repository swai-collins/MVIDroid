package com.arkivanov.mvikotlin.sample.android.todolist

import android.util.Log
import android.view.View
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView

class TodoListViewImpl(
    private val root: View
) : TodoListView() {



    override fun bind(model: Model) {
        super.bind(model)

        Log.v("MyTest", model.toString())
    }
}
