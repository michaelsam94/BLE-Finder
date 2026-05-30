package com.michael.blefinder

import android.app.Application
import com.michael.blefinder.di.AppContainer
import com.michael.blefinder.di.AppContainerImpl

class BleFinderApp : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainerImpl(this)
    }
}
