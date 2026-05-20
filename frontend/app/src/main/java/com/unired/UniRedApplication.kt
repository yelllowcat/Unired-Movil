package com.unired

import android.app.Application
import com.unired.util.SessionManager

class  UniRedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.initialize(this)
    }
}
