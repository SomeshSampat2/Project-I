package com.innovatelabs3.projectI2

import android.app.Application

class ProjectIApplication : Application() {
    companion object {
        private lateinit var instance: ProjectIApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
} 