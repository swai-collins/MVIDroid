package com.arkivanov.mvikotlin.sample.android.todolist

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.arkivanov.mvikotlin.base.observable.diff
import com.arkivanov.mvikotlin.sample.android.R
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoListView

class TodoListViewImpl(root: View) : TodoListView() {

    private val adapter =
        TodoListAdapter(
            object : TodoListAdapter.Listener {
                override fun onItemClick(id: Long) {
                    dispatch(Event.ItemClicked(id))
                }

                override fun onItemDoneClick(id: Long) {
                    dispatch(Event.ItemDoneClicked(id))
                }

                override fun onItemDeleteClick(id: Long) {
                    dispatch(Event.ItemDeleteClicked(id))
                }
            }
        )

    init {
        requireNotNull(root.findViewById<RecyclerView>(R.id.recycler_view)).adapter = adapter
        models.diff(Model::items, { a, b -> a === b }, adapter::setItems)
    }
}
