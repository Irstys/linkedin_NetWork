package ru.netology.linkedin_network.app

import android.app.Application
import ru.netology.linkedin_network.auth.AppAuth
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LinkedInNetworkApplication : Application(){

    @Inject
    lateinit var auth: AppAuth

    annotation class Inject

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("MAPS_API_KEY")
    }
}