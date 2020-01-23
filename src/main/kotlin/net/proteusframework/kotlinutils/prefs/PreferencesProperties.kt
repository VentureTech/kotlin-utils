package net.proteusframework.kotlinutils.prefs

import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction3
import kotlin.reflect.KProperty

/**
 * Preferences properties class that provides preferences variables or values
 * as a delegated property.
 *
 * See [UserPreferencesProperties] and [SystemPreferencesProperties]
 * @author Russ Tennant (russ@proteus.co)
 */
abstract class PreferencesProperties {
    abstract val preferences: Preferences
    /** Transformation function to modify the key used for the preference. Source string is the property name. */
    abstract val prefKeyTransform: (String) -> String

    protected fun stringPref(
        defaultValue: String = "",
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, String> {
        return ReadWritePreferencesProperty<String>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun stringPrefN(
        defaultValue: String? = null,
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, String?> {
        return ReadWritePreferencesProperty<String?>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun intPref(
        defaultValue: Int = 0,
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, Int> {
        return ReadWritePreferencesProperty<Int>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun longPref(
        defaultValue: Long = 0,
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, Long> {
        return ReadWritePreferencesProperty<Long>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun floatPref(
        defaultValue: Float = 0f,
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, Float> {
        return ReadWritePreferencesProperty<Float>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun doublePref(
        defaultValue: Double = 0.0,
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, Double> {
        return ReadWritePreferencesProperty<Double>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun booleanPref(
        defaultValue: Boolean = false,
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, Boolean> {
        return ReadWritePreferencesProperty<Boolean>(defaultValue, prefKeyTransform, subNode)
    }

    protected fun byteArrayPref(
        defaultValue: ByteArray = byteArrayOf(),
        vararg subNode: String
    ): ReadWriteProperty<PreferencesProperties, ByteArray> {
        return ReadWritePreferencesProperty<ByteArray>(defaultValue, prefKeyTransform, subNode)
    }

    private class ReadWritePreferencesProperty<T>(
        private val defaultValue: T,
        private val prefKeyTransform: (String) -> String,
        private val subNode: Array<out String>
    ) : ReadWriteProperty<PreferencesProperties, T> {

        @Suppress("UNCHECKED_CAST")
        override operator fun getValue(thisRef: PreferencesProperties, property: KProperty<*>): T {
            val preferenceProperty = getPrefAccessors(property) as PreferenceProperty<T>
            return preferenceProperty.getter(getPreferences(thisRef), getPreferenceKey(thisRef, property), this
                .defaultValue)
        }

        @Suppress("UNCHECKED_CAST")
        override operator fun setValue(thisRef: PreferencesProperties, property: KProperty<*>, value: T) {
            val preferenceProperty = getPrefAccessors(property) as PreferenceProperty<T>
            preferenceProperty.setter(getPreferences(thisRef), getPreferenceKey(thisRef, property), value)
            thisRef.preferences.flush()
        }

        private fun getPreferences(thisRef: PreferencesProperties): Preferences = if (subNode.isEmpty()) thisRef.preferences
        else thisRef.preferences.node(subNode.joinToString("/"))

        @Suppress("ConvertReferenceToLambda")
        private fun getPrefAccessors(property: KProperty<*>): PreferenceProperty<Any> {
            val typeName: String = property.returnType.classifier?.toString()
                ?: throw IllegalStateException("Unsupported type for Preferences: ${property.returnType.classifier}")
            return PREF_PROPERTY.computeIfAbsent(typeName) {
                val classifier: KClassifier? = property.returnType.classifier
                val (getter, setter) = when (classifier) {
                    String::class    -> Preferences::get to Preferences::put
                    Int::class       -> Preferences::getInt to Preferences::putInt
                    Long::class      -> Preferences::getLong to Preferences::putLong
                    Float::class     -> Preferences::getFloat to Preferences::putFloat
                    Double::class    -> Preferences::getDouble to Preferences::putDouble
                    Boolean::class   -> Preferences::getBoolean to Preferences::putBoolean
                    ByteArray::class -> Preferences::getByteArray to Preferences::putByteArray
                    else             -> throw IllegalStateException("Unsupported type for Preferences: $classifier")
                }
                @Suppress("UNCHECKED_CAST")
                PreferenceProperty(
                    getter as KFunction3<Preferences, String, Any, Any>,
                    setter as KFunction3<Preferences, String, Any, Unit>
                )
            }
        }

        private fun getPreferenceKey(thisRef: PreferencesProperties, property: KProperty<*>): String {
            val keyMapKey = "/${thisRef.preferences.absolutePath()}/${property.name}"
            return KEY_MAP.computeIfAbsent(keyMapKey) { prefKeyTransform(property.name) }
        }

        companion object {
            /** Absolute path of key to relative path of transformed preference key. */
            private val KEY_MAP = mutableMapOf<String, String>()
            /** Kotlin type name to PreferenceProperty */
            private val PREF_PROPERTY = mutableMapOf<String, PreferenceProperty<Any>>()
        }
    }

    /**
     * Getter and setter pair for the preference data type.
     */
    private class PreferenceProperty<T>(
        val getter: KFunction3<Preferences, String, T, T>,
        val setter: KFunction3<Preferences, String, T, Unit>
    )
}


