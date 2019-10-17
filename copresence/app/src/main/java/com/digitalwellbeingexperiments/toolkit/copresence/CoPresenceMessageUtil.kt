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

import com.google.android.gms.nearby.messages.Message

object CoPresenceMessageUtil {
    private const val MSG_PREFIX = "CoPresence"
    private const val MSG_SEPARATOR = "|"

    fun isValidMessage(content: String): Boolean {
        return (content.contains(MSG_PREFIX) && content.split(MSG_SEPARATOR).size == 3)//is it one of our messages?
    }

    fun createMessage(deviceId: String, name: String): Message {
        return Message(
            (MSG_PREFIX + MSG_SEPARATOR + deviceId + MSG_SEPARATOR + name).toByteArray()
        )
    }

    fun parseMessageToSession(message: Message): NearbyDeviceSession? {
        val messageContent = String(message.content)
        if (isValidMessage(messageContent)) {
            val messageParts = messageContent.split(MSG_SEPARATOR)
            return NearbyDeviceSession(messageParts[1], messageParts[2])
        }
        return null
    }
}