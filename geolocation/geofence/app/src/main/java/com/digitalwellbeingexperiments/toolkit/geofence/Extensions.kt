// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.digitalwellbeingexperiments.toolkit.geofence

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*


fun Any.TAG() = this.javaClass.simpleName

fun createSnackbar(view: View, message: String, length: Int): Snackbar {
    val snackbar = Snackbar.make(view, message, length)
    //https://materialdoc.com/components/snackbars-and-toasts/
    snackbar.view.apply {
        findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            setTextColor(resources.getColor(R.color.snackbar_text_color))
        }
        setBackgroundColor(resources.getColor(R.color.snackbar_bg_color))
    }
    return snackbar
}

fun formatTimestamp(timestamp: Long, pattern: String = "dd MMM yyyy, HH:mm"): String {
    val sdf = SimpleDateFormat(pattern)
    val resultdate = Date(timestamp)
    return sdf.format(resultdate)
}
