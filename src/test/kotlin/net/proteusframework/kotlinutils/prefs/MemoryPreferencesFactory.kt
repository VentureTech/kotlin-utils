package net.proteusframework.kotlinutils.prefs

import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

class MemoryPreferencesFactory : PreferencesFactory {
    val user = MemoryPreferences(null, "", true)
    val system = MemoryPreferences(null, "", false)

    override fun userRoot(): Preferences {
        return user
    }

    override fun systemRoot(): Preferences {
        return system
    }

    companion object {
        fun install() {
            System.setProperty("java.util.prefs.PreferencesFactory", MemoryPreferencesFactory::class.qualifiedName!!)
        }
    }
}