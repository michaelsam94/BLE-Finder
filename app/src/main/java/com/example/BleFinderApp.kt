package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.AppContainerImpl

class BleFinderApp : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainerImpl(this)
    }
}
