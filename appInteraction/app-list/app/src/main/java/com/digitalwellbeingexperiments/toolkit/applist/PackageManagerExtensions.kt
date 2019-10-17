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

package com.digitalwellbeingexperiments.toolkit.applist

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun PackageManager.getAllPackages(context: Context): List<ApplicationInfo> {
    return getInstalledApplications(0)
        .filter { it.packageName != context.packageName }
        .distinctBy { it.packageName }
        .sortedBy { getApplicationLabel(it.packageName) }
}

fun PackageManager.getLaunchablePackages(context: Context): List<ApplicationInfo> {
    return queryIntentActivities(Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        .filter { it.activityInfo.packageName != context.packageName}
        .distinctBy { it.activityInfo.packageName }
        .map { getApplicationInfo(it.activityInfo.packageName, 0) }
        .sortedBy { this.getApplicationLabel(it).toString() }
}

fun PackageManager.getApplicationLabel(packageName: String): String {
    val info = getApplicationInfo(packageName, 0)
    return getApplicationLabel(info).toString()
}