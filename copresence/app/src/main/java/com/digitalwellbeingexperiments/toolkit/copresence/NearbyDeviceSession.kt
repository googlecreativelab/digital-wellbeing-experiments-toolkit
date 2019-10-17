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

package com.digitalwellbeingexperiments.toolkit.copresence

data class NearbyDeviceSession(val deviceId: String, val deviceName: String) {
    companion object {
        const val MAX_PING_STRIKES = 3
    }

    private var pingReceived = false
    private var pingStrikes = 0

    fun onPingReceived() {
        pingReceived = true
    }

    fun hasExpired(): Boolean {
        if (!pingReceived) {
            pingStrikes++
        } else {
            if (pingStrikes > 0) {
                pingStrikes = 0
            }
        }
        pingReceived = false
        return pingStrikes >= MAX_PING_STRIKES
    }
}