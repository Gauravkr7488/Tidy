package com.example.tidy

import android.app.Application
import io.objectbox.BoxStore

class App: Application() {
    lateinit var boxStore: BoxStore
        private set
    override fun onCreate() {
        super.onCreate()
        // initialize ObjectBox
        boxStore = MyObjectBox.builder().androidContext(this).build()
    }
}

