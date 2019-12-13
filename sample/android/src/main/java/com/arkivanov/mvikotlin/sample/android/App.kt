package com.arkivanov.mvikotlin.sample.android

import android.app.Application
import android.util.Log
import com.arkivanov.mvikotlin.base.store.MviStoreFactory
import com.arkivanov.mvikotlin.core.debug.logging.MviLoggingMode
import com.arkivanov.mvikotlin.core.debug.logging.MviLoggingStoreFactory
import com.arkivanov.mvikotlin.core.debug.timetravel.MviTimeTravelStoreFactory
import com.arkivanov.mvikotlin.core.store.MviDefaultStoreFactory
import com.arkivanov.mvikotlin.sample.mpp.common.TodoDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

class App : Application() {

    val todoDatabase: TodoDatabase by lazy {
        TodoDatabase(AndroidSqliteDriver(TodoDatabase.Schema, this, "TodoDatabase.db"))
    }

    val storeFactory: MviStoreFactory =
        if (BuildConfig.DEBUG) {
            MviLoggingStoreFactory(
                delegate = MviTimeTravelStoreFactory,
                logger = { Log.v("MviKotlinApp", it) },
                mode = MviLoggingMode.MEDIUM
            )
        } else {
            MviDefaultStoreFactory
        }
}
