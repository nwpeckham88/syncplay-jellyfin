package com.yuroyami.syncplay.navigation

import android.content.Context
import android.content.Intent
import com.yuroyami.syncplay.JellyfinActivity

class AndroidJellyfinNavigation(private val context: Context) : JellyfinNavigation {
    override fun openJellyfinBrowser() {
        val intent = Intent(context, JellyfinActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
