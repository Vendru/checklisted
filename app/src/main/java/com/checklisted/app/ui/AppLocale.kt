package com.checklisted.app.ui

import java.util.Locale

/**
 * The language the app's copy is written in.
 *
 * Every string in `res/values` is Brazilian Portuguese and there is no second
 * translation, so the interface is pt-BR on any device. Dates and weekday initials
 * have to follow it: formatting them with the device locale put "M T W T F S S" over
 * a grid captioned "Últimos 3 meses" for anyone whose phone is set to English.
 *
 * When a translation is added, this becomes the resolved resource locale rather than
 * a constant.
 */
val AppLocale: Locale = Locale.forLanguageTag("pt-BR")
