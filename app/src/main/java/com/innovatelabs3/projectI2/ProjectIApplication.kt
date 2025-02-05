package com.innovatelabs3.projectI2

import android.app.Application
import android.content.Context

class ProjectIApplication : Application() {
    companion object {
        private lateinit var instance: ProjectIApplication
        
        fun getContext(): Context = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
} 