package com.anonymous.peep.service

/**
 * Maps Android package names to friendly display names with emojis.
 * Direct port of StatusBroadcaster.ts APP_NAME_MAP.
 */
object AppNameMapper {

    private val PEEP_MESSAGES = listOf(
        "Also stalking someone 🕵️",
        "Being nosy too 👃",
        "Spying on friends rn 👀",
        "Caught peeping 😳",
        "Snooping around 🔍",
        "Up to no good 🤫",
        "Also being a creep 🫣",
        "Peeping right back 👁️",
        "Being nosy af 💀",
        "In stalker mode 🥷",
    )

    private val APP_MAP = mapOf(
        "com.android.chrome" to "Browsing Chrome 🌐",
        "com.google.android.youtube" to "Watching YouTube 📺",
        "com.spotify.music" to "Listening to Spotify 🎵",
        "com.instagram.android" to "Scrolling Instagram 📸",
        "com.whatsapp" to "Chatting on WhatsApp 💬",
        "com.netflix.mediaclient" to "Watching Netflix 🎬",
        "com.twitter.android" to "Scrolling X 𝕏",
        "com.google.android.apps.maps" to "Navigating Maps 🗺️",
        "com.google.android.gm" to "Checking Gmail 📧",
        "com.facebook.katana" to "On Facebook 👥",
        "com.zhiliaoapp.musically" to "Watching TikTok 🎵",
        "com.snapchat.android" to "Using Snapchat 👻",
        "com.discord" to "Chatting on Discord 💬",
        "com.linkedin.android" to "Networking on LinkedIn 💼",
        "com.reddit.frontpage" to "Browsing Reddit 🔥",
        "com.amazon.mShop.android.shopping" to "Shopping on Amazon 🛒",
        "com.google.android.apps.messaging" to "Texting on Messages 💬",
        "com.android.settings" to "In Settings ⚙️",
        "com.android.camera2" to "Taking a Photo 📸",
        "com.google.android.GoogleCamera" to "Taking a Photo 📸",
        "com.google.android.dialer" to "On a Phone Call 📞",
        "com.android.vending" to "Browsing Play Store 🛍️",
        "com.google.android.apps.photos" to "Looking at Photos 🖼️",
        "com.telegram.messenger" to "On Telegram ✈️",
        "com.pinterest" to "On Pinterest 📌",
        "com.google.android.apps.docs" to "On Google Docs 📄",
    )

    fun getFriendlyAppName(packageName: String): String {
        if (packageName.isEmpty()) return "Idle 😴"
        if (packageName == "com.anonymous.peep") {
            return PEEP_MESSAGES.random()
        }
        return APP_MAP[packageName] ?: "Using ${packageName.split(".").last()} 📱"
    }
}
