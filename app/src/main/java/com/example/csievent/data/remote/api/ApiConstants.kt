package com.example.csievent.data.remote.api

import com.example.csievent.BuildConfig

object ApiConstants {
    // Set in app/build.gradle.kts
    const val BASE_URL = BuildConfig.BASE_URL

    // Public pages required by Google Play
    const val PRIVACY_POLICY_URL = BuildConfig.BASE_URL + "privacy-policy"
    const val DELETE_ACCOUNT_URL = BuildConfig.BASE_URL + "delete-account"
}
