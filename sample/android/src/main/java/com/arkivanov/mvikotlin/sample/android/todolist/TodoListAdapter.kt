package com.arkivanov.mvikotlin.sample.android.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkivanov.mvikotlin.sample.android.R
import com.arkivanov.mvikotlin.sample.mpp.todolist.TodoItem

class TodoListAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    private var items: List<TodoItem> = emptyList()

    fun setItems(items: List<TodoItem>) {
        val oldItems = items
        this.items = items

        DiffUtil
            .calculateDiff(
                object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int = oldItems.size

                    override fun getNewListSize(): Int = items.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldItems[oldItemPosition].id == items[newItemPosition].id

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldItems[oldItemPosition] == items[newItemPosition]
                }
            )
            .dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false), listener)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface Listener {
        fun onItemClick(id: Long)

        fun onItemDoneClick(id: Long)

        fun onItemDeleteClick(id: Long)
    }

    class ViewHolder(view: View, listener: Listener) : RecyclerView.ViewHolder(view) {
        private lateinit var boundItem: TodoItem

        init {
            itemView.setOnClickListener {
                listener.onItemClick(boundItem.id)
            }
        }

        private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            listener.onItemDoneClick(boundItem.id)
        }
        private val checkBox = itemView.findViewById<CheckBox>(R.id.check_box).apply {
            setOnCheckedChangeListener(onCheckedChangeListener)
        }

        private val textView = itemView.findViewById<TextView>(R.id.text)

        init {
            itemView.findViewById<View>(R.id.delete_button).apply {
                setOnClickListener {
                    listener.onItemDeleteClick(boundItem.id)
                }
            }
        }

        fun bind(item: TodoItem) {
            boundItem = item
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isDone
            checkBox.setOnCheckedChangeListener(onCheckedChangeListener)
            textView.text = item.text
        }
    }
}
