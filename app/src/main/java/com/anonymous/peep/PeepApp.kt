package com.anonymous.peep

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PeepApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code here if needed
    }
}
