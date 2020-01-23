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
    abstract val rootNode: Preferences
    /** Transformation function to modify the key used for the preference. Source string is the property name. */
    abstract val prefKeyTransform: (String) -> String

    protected fun stringPref(defaultValue: String = ""): ReadWriteProperty<PreferencesProperties, String> =
        ReadWritePreferencesProperty<String>(defaultValue, prefKeyTransform)

    protected fun stringPrefN(defaultValue: String? = null): ReadWriteProperty<PreferencesProperties, String?> =
        ReadWritePreferencesProperty<String?>(defaultValue, prefKeyTransform)

    protected fun intPref(defaultValue: Int = 0): ReadWriteProperty<PreferencesProperties, Int> =
        ReadWritePreferencesProperty<Int>(defaultValue, prefKeyTransform)

    protected fun intPrefN(defaultValue: Int? = null): ReadWriteProperty<PreferencesProperties, Int?> =
        ReadWritePreferencesProperty<Int?>(defaultValue, prefKeyTransform)

    protected fun longPref(defaultValue: Long = 0): ReadWriteProperty<PreferencesProperties, Long> =
        ReadWritePreferencesProperty<Long>(defaultValue, prefKeyTransform)

    protected fun longPrefN(defaultValue: Long? = null): ReadWriteProperty<PreferencesProperties, Long?> =
        ReadWritePreferencesProperty<Long?>(defaultValue, prefKeyTransform)

    protected fun floatPref(defaultValue: Float = 0f): ReadWriteProperty<PreferencesProperties, Float> =
        ReadWritePreferencesProperty<Float>(defaultValue, prefKeyTransform)

    protected fun floatPrefN(defaultValue: Float? = null): ReadWriteProperty<PreferencesProperties, Float?> =
        ReadWritePreferencesProperty<Float?>(defaultValue, prefKeyTransform)

    protected fun doublePref(defaultValue: Double = 0.0): ReadWriteProperty<PreferencesProperties, Double> =
        ReadWritePreferencesProperty<Double>(defaultValue, prefKeyTransform)

    protected fun doublePrefN(defaultValue: Double? = null): ReadWriteProperty<PreferencesProperties, Double?> =
        ReadWritePreferencesProperty<Double?>(defaultValue, prefKeyTransform)

    protected fun booleanPref(defaultValue: Boolean = false): ReadWriteProperty<PreferencesProperties, Boolean> =
        ReadWritePreferencesProperty<Boolean>(defaultValue, prefKeyTransform)

    protected fun booleanPrefN(defaultValue: Boolean? = null): ReadWriteProperty<PreferencesProperties, Boolean?> =
        ReadWritePreferencesProperty<Boolean?>(defaultValue, prefKeyTransform)

    protected fun byteArrayPref(defaultValue: ByteArray = byteArrayOf()): ReadWriteProperty<PreferencesProperties, ByteArray> =
        ReadWritePreferencesProperty<ByteArray>(defaultValue, prefKeyTransform)

    protected fun byteArrayPrefN(defaultValue: ByteArray? = null): ReadWriteProperty<PreferencesProperties, ByteArray?> =
        ReadWritePreferencesProperty<ByteArray?>(defaultValue, prefKeyTransform)

    private class ReadWritePreferencesProperty<T>(
        private val defaultValue: T,
        private val prefKeyTransform: (String) -> String
    ) : ReadWriteProperty<PreferencesProperties, T> {

        @Suppress("UNCHECKED_CAST")
        override operator fun getValue(thisRef: PreferencesProperties, property: KProperty<*>): T {
            val preferenceProperty = getPrefAccessors(property) as PreferenceProperty<T>
            return preferenceProperty.getter(thisRef.rootNode, getPreferenceKey(thisRef, property), this.defaultValue)
        }

        @Suppress("UNCHECKED_CAST")
        override operator fun setValue(thisRef: PreferencesProperties, property: KProperty<*>, value: T) {
            val preferenceProperty = getPrefAccessors(property) as PreferenceProperty<T>
            preferenceProperty.setter(thisRef.rootNode, getPreferenceKey(thisRef, property), value)
            thisRef.rootNode.flush()
        }

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
            val keyMapKey = "/${thisRef.rootNode.absolutePath()}/${property.name}"
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


