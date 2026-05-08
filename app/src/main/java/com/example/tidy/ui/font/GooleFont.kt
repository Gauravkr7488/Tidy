package com.example.tidy.ui.font

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.Ace777.tidy.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val Knewave = GoogleFont("Knewave")

val KnewaveFontFamily = FontFamily(
    Font(
        googleFont = Knewave,
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = Knewave,
        fontProvider = provider,
        weight = FontWeight.Bold
    )
)