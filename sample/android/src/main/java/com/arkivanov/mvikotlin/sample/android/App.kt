package com.arkivanov.mvikotlin.sample.android

import android.app.Application
import com.arkivanov.mvikotlin.sample.mpp.common.TodoDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

class App : Application() {

    val todoDabase: TodoDatabase by lazy {
        TodoDatabase(AndroidSqliteDriver(TodoDatabase.Schema, this, "TodoDatabase.db"))
    }
}
