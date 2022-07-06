package com.colcastar.web

import android.app.Application
import com.mazenrashed.printooth.Printooth;

class ColcaStartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Printooth.init(this)
    }
}