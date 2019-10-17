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

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.google.android.gms.tasks.Task

class NearbyManager(context: Context) {
    companion object {
        //  Timeout stuff
        const val MESSAGE_TTL_SECS = 2
        const val  POLL_TIME_MS = MESSAGE_TTL_SECS * 1000L * 10 //10 secs
    }

    private var client: MessagesClient = Nearby.getMessagesClient(context)
    //receive
    private var message: Message? = null
    private var messageListener: MessageListener? = null
    private lateinit var subscribeStrategy: Strategy
    private lateinit var subscribeOptions: SubscribeOptions

    //send
    private lateinit var broadcastStrategy: Strategy
    private lateinit var broadcastOptions: PublishOptions

    private lateinit var sessionExpiryTimer: CountDownTimer

    fun start() {
        messageListener = object : MessageListener() {
            override fun onFound(receivedMessage: Message?) {
                receivedMessage?.let {
                    CoPresenceMessageUtil.parseMessageToSession(it)?.let { session ->
                        getDataStore().apply {
                            if(sessionExists(session.deviceId)){
                                session.onPingReceived()
                            }
                            addSession(session)
                        }
                    }
                }
            }
        }

        val broadcastCallback = object : PublishCallback() {
            override fun onExpired() {
                super.onExpired()
                broadcastMessage()
            }
        }

        broadcastStrategy = Strategy.Builder()
            .setDiscoveryMode(Strategy.DISCOVERY_MODE_DEFAULT)
            .setDistanceType(Strategy.DISTANCE_TYPE_EARSHOT)
            .setTtlSeconds(MESSAGE_TTL_SECS)
            .build()

        broadcastOptions = PublishOptions.Builder()
            .setStrategy(broadcastStrategy)
            .setCallback(broadcastCallback)
            .build()


        //subscribe
        subscribeStrategy = Strategy.DEFAULT
        subscribeOptions = SubscribeOptions.Builder()
            .setStrategy(subscribeStrategy)
            .build()

        messageListener?.let {
            logClientTaskResult(
                "SUBSCRIBE",
                client.subscribe(it, subscribeOptions)
            )
        }

        //publish message
        broadcastMessage()

        sessionExpiryTimer = object: CountDownTimer(Long.MAX_VALUE, POLL_TIME_MS){
            override fun onFinish() {}

            override fun onTick(millisUntilFinished: Long) {
                getDataStore().clearExpiredSessions()
            }
        }
        sessionExpiryTimer.start()
    }

    fun stop() {
        sessionExpiryTimer.cancel()

        //unpublish
        logClientTaskResult(
            "UNPUBLISH",
            client.unpublish(message ?: return)
        )

        messageListener?.let {
            //unsubscribe
            logClientTaskResult(
                "UNSUBSCRIBE",
                client.unsubscribe(it)
            )
        }
    }

    private fun broadcastMessage() {

        message?.let {
            client.unpublish(it)
        }

        message = CoPresenceMessageUtil.createMessage(
            getDataStore().getLocalDeviceId(),
            getDataStore().getName())

        message?.let {
            client.publish(it, broadcastOptions)
        }
    }


    private fun logNearbyFailure(e: Exception, action: String) {
        var errorDetails = e.message
        if (e is ApiException) {
            errorDetails =
                "ApiException status (${e.statusCode}) ${NearbyMessagesStatusCodes.getStatusCodeString(
                    e.statusCode
                )}"
        }
        Log.e(
            TAG(),
            "$action, Exception details $errorDetails"
        )
    }

    private fun logClientTaskResult(taskName: String, task: Task<Void>) {
        task
            .addOnSuccessListener {
                Log.w(TAG(), "SUCCESS >> ${taskName.toUpperCase()}")
            }
            .addOnFailureListener { e ->
                logNearbyFailure(e, "FAIL >>> ${taskName.toUpperCase()}")
            }
    }
}