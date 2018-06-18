# Simple Phone

This project shows the absolute minimum an Android Phone app needs to implement to replace the native phone app and provide the UI when making calls.

## Becoming a default Phone app

To have your app listed as a Phone app, you must have an activity with at least those intent filters (to handle both cases mentioned in documentation of [ACTION_DIAL][1], also mentioned in [DefaultDialerManager hidden class][2]):

```xml
<intent-filter>
    <action android:name="android.intent.action.DIAL" />
    <data android:scheme="tel" />
</intent-filter>
<intent-filter>
    <action android:name="android.intent.action.DIAL" />
</intent-filter>
```

And to be honest, that's a bit counterintuitive, because setting the default Phone app is separate from setting a default Dialer – the former controls only the ongoing call UI, while the latter controls only the dialing UI.

Filters in the [AndroidManifest](app/src/main/AndroidManifest.xml) improve a bit over that minimum, to allow *setting the app as the default Dialer*, and launching dialer from web browser. The [Dialer app in AOSP][3] has even more filters declared.

You can make it easier for the user to set your app as the default Phone app with the help from `TelecomManager`:

```kotlin
if (getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
    Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            .let(::startActivity)
}
```

This will show a dialog similar to this:

[![change default dialer dialog][4]][4]

  [1]: https://developer.android.com/reference/android/content/Intent.html#ACTION_DIAL
  [2]: https://android.googlesource.com/platform/frameworks/base/+/master/telecomm/java/android/telecom/DefaultDialerManager.java#144
  [3]: https://android.googlesource.com/platform/packages/apps/Dialer/+/nougat-release/AndroidManifest.xml#79
  [4]: https://i.stack.imgur.com/awS6u.png

## Handling calls

You need to define an [`InCallService`][5] implementation the system will bind to and notify you about the call:

```xml
<service
    android:name=".CallService"
    android:permission="android.permission.BIND_INCALL_SERVICE">
    <meta-data
        android:name="android.telecom.IN_CALL_SERVICE_UI"
        android:value="true" />
    <intent-filter>
        <action android:name="android.telecom.InCallService" />
    </intent-filter>
</service>
```

There you should handle at least [`onCallAdded`][6] (set up listeners on `Call`, start your UI - activity - for the call) and [`onCallRemoved`][7] (remove listeners), like [`CallService`](app/src/main/java/com/github/arekolek/phone/CallService.kt) does in a simplified way.

If the user wants to answer the call, you need to invoke the method [`Call#answer(int)`][8] (with [`VideoProfile.STATE_AUDIO_ONLY`][9] for example). In this example [`CallActivity`](app/src/main/java/com/github/arekolek/phone/CallActivity.kt) reacts to user input by calling those methods on `Call` object shared through the [`OngoingCall`](app/src/main/java/com/github/arekolek/phone/OngoingCall.kt).

Check out [Call.Callback][10] for events that can happen with a single call. This sample uses just the `onStateChanged` callback, to update the UI and finish the activity when the remote party hangs upl

  [5]: https://developer.android.com/reference/android/telecom/InCallService.html
  [6]: https://developer.android.com/reference/android/telecom/InCallService.html#onCallAdded(android.telecom.Call)
  [7]: https://developer.android.com/reference/android/telecom/InCallService.html#onCallRemoved(android.telecom.Call)
  [8]: https://developer.android.com/reference/android/telecom/Call.html#answer(int)
  [9]: https://developer.android.com/reference/android/telecom/VideoProfile.html#STATE_AUDIO_ONLY
  [10]: https://developer.android.com/reference/android/telecom/Call.Callback.html