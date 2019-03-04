package com.arkoz.example.phone

import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import android.widget.Toast
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

object OngoingCall {


    val state: BehaviorSubject<Int> = BehaviorSubject.create()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            Timber.d(call.toString())
            state.onNext(newState)
        }

        fun Context.toast(message: CharSequence) = Toast.makeText(this, "Call is picked", Toast.LENGTH_SHORT).show()
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state.onNext(it.state)
            }
            field = value
        }

    fun answer() {
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call!!.disconnect()
    }
}
