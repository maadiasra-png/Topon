package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// TOPON Brand Palette - Rescue & AI Biometric Network
val ToponNavyDark = Color(0xFF080F1E)
val ToponNavyCardDark = Color(0xFF111E38)
val ToponNavyCardElevatedDark = Color(0xFF182849)

val ToponNavyLight = Color(0xFFF1F5F9)
val ToponNavyCardLight = Color(0xFFFFFFFF)
val ToponNavyCardElevatedLight = Color(0xFFE2E8F0)

// Accent 1: Rescue Azure Blue
val ToponBluePrimary = Color(0xFF0284C7)
val ToponBluePrimaryDark = Color(0xFF38BDF8)

// Accent 2: Emergency Alert Coral / Amber
val ToponAlertCoral = Color(0xFFEF4444)
val ToponAlertAmber = Color(0xFFF59E0B)

// Accent 3: Verified Emerald / Mint
val ToponVerifiedGreen = Color(0xFF10B981)
val ToponCyanScanner = Color(0xFF06B6D4)

// Neutral text & border colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val BorderDark = Color(0xFF1E293B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val BorderLight = Color(0xFFCBD5E1)

enum class ToponAccentTheme(val label: String, val primary: Color, val primaryDark: Color) {
    RESCUE_BLUE("آبی نجات", Color(0xFF0284C7), Color(0xFF38BDF8)),
    EMERGENCY_AMBER("کهربایی هشدار", Color(0xFFD97706), Color(0xFFFBBF24)),
    TACTICAL_EMERALD("زمردی امداد", Color(0xFF059669), Color(0xFF34D399))
}
