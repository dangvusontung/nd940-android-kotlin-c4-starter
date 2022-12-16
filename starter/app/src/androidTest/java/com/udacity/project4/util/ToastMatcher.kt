package com.udacity.project4.util

import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description?) {
        description?.appendText("is Toast")
    }

    override fun matchesSafely(item: Root?): Boolean {
        val type = item?.windowLayoutParams?.get()?.type
        type?.let {
            if (it == WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW) {
                val windowToken = item.decorView.windowToken
                val appToken = item.decorView.applicationWindowToken
                if (windowToken == appToken)
                    return true
            }
        }
        return false
    }
}