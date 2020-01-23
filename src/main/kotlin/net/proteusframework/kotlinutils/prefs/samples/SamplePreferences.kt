package net.proteusframework.kotlinutils.prefs.samples

import net.proteusframework.kotlinutils.prefs.UserPreferencesProperties

object SamplePreferences : UserPreferencesProperties(SamplePreferences::class) {
    var downloadDirectory by stringPref("\${HOME}/Download")
    val adminSiteId by longPrefN()
}
