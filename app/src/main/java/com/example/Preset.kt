package com.example

data class PassportPreset(
    val name: String,
    val country: String,
    val flag: String,
    val widthMm: Float,
    val heightMm: Float,
    val description: String = ""
)

object Presets {
    val list = listOf(
        // India Sizes
        PassportPreset("Passport (Default)", "India 🇮🇳", "🇮🇳", 35f, 45f, "Standard Indian Passport (35x45mm)"),
        PassportPreset("Visa (OCI)", "India 🇮🇳", "🇮🇳", 50f, 50f, "OCI / Indian Visa (50x50mm)"),
        PassportPreset("Mini Size", "India 🇮🇳", "🇮🇳", 25f, 25f, "Indian Mini ID (25x25mm)"),
        PassportPreset("Small Size", "India 🇮🇳", "🇮🇳", 20f, 25f, "Indian Small size (20x25mm)"),
        PassportPreset("Card Size", "India 🇮🇳", "🇮🇳", 25f, 32f, "Indian Card size (25x32mm)"),
        PassportPreset("Medium Size", "India 🇮🇳", "🇮🇳", 30f, 40f, "Indian Medium size (30x40mm)"),
        PassportPreset("35mm Square", "India 🇮🇳", "🇮🇳", 35f, 35f, "Indian Square size (35x35mm)"),
        PassportPreset("Large Size", "India 🇮🇳", "🇮🇳", 40f, 50f, "Indian Large size (40x50mm)"),
        PassportPreset("Square 51x51mm", "India 🇮🇳", "🇮🇳", 51f, 51f, "Indian OCI/Visa Square (51x51mm)"),

        // US Sizes
        PassportPreset("Passport", "United States 🇺🇸", "🇺🇸", 33f, 48f, "US Passport (33x48mm)"),
        PassportPreset("Visa", "United States 🇺🇸", "🇺🇸", 51f, 51f, "US Visa / DS-160 (2x2 in / 51x51mm)"),

        // UK
        PassportPreset("Passport", "United Kingdom 🇬🇧", "🇬🇧", 35f, 45f, "UK Passport Office (35x45mm)"),

        // EU / Schengen
        PassportPreset("Passport", "EU / Schengen 🇪🇺", "🇪🇺", 35f, 45f, "Schengen Visa or EU Passport (35x45mm)"),

        // China
        PassportPreset("Passport/Visa", "China 🇨🇳", "🇨🇳", 33f, 48f, "China Passport & Visa (33x48mm)"),

        // Asia Pacific
        PassportPreset("Passport", "Japan 🇯🇵", "🇯🇵", 35f, 45f, "Japan Passport (35x45mm)"),
        PassportPreset("Passport", "Korea 🇰🇷", "🇰🇷", 35f, 45f, "South Korea National Passport (35x45mm)"),
        PassportPreset("Passport", "Singapore 🇸🇬", "🇸🇬", 35f, 45f, "Singapore ICA Passport (35x45mm)"),
        PassportPreset("Passport", "Australia 🇦🇺", "🇦🇺", 35f, 45f, "Australia Passport (35x45mm)"),

        // Other Countries
        PassportPreset("Passport", "Canada 🇨🇦", "🇨🇦", 50f, 70f, "Canada Passport Office (50x70mm)"),
        PassportPreset("Passport", "Brazil 🇧🇷", "🇧🇷", 35f, 45f, "Brazil Passport (35x45mm)"),
        PassportPreset("Photo ID", "General 🌍", "🌍", 35f, 45f, "Global Identity Card (35x45mm)"),
        PassportPreset("Mini ID", "General 🌍", "🌍", 25f, 35f, "Compact Mini ID profile (25x35mm)")
    )
}
