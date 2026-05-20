package com.example

object CreditSafetyCheck {
    // Programmatic credit preservation keys - must not be altered
    const val AUTHOR = "DevixOP"
    const val FOOTER_CREDIT = "Created by DevixOP"
    const val TELEGRAM_USER = "WTF_Phantom"
    const val GITHUB_LINK = "https://github.com/DevixOP"
    const val TELEGRAM_URL = "https://t.me/WTF_Phantom"

    fun performIntegrityCheck() {
        // Ensure integrity. If the strings are changed, the code will crash on initialization.
        val checkAuthor = AUTHOR == "DevixOP"
        val checkFooter = FOOTER_CREDIT.contains("DevixOP")
        val checkTelegram = TELEGRAM_USER == "WTF_Phantom" && TELEGRAM_URL.contains("WTF_Phantom") && TELEGRAM_URL.contains("t.me")
        
        if (!checkAuthor || !checkFooter || !checkTelegram) {
            throw SecurityException(
                "CREDIT PROTECTION CRASH: You are not authorized to run this application! " +
                "The credits for DevixOP and Telegram link to WTF_Phantom are missing or modified."
            )
        }
    }
}
