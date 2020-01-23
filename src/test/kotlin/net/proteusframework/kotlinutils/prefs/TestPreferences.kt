package net.proteusframework.kotlinutils.prefs

private val PATH = arrayOf("one", "two", "three")

object PathPreferences : UserPreferencesProperties(PathPreferencesNode(PATH)) {
    var hello by stringPref("goodbye")
    var age by intPref(77)
    var agree by booleanPref()

    val path = PATH
}

object PackagePreferences : UserPreferencesProperties(PackagePreferencesNode(PackagePreferences::class)) {
    var hello by stringPref("goodbye")
    var age by intPref(77)
    var agree by booleanPref()
    var age2 by intPref(77, SUB[0])
}

private val SUB = arrayOf("sub")

object PackageSubPreferences : UserPreferencesProperties(PackagePreferencesNode(PackageSubPreferences::class, SUB)) {
    var hello by stringPref("goodbye")
    var age by intPref(77)
    var agree by booleanPref()
    var age2 by intPref(77)
}

object ClassPreferences : UserPreferencesProperties(ClassPreferencesNode(ClassPreferences::class)) {
    var hello by stringPref("goodbye")
    var age by intPref(77)
    var agree by booleanPref()
}

object ClassSubPreferences : UserPreferencesProperties(ClassPreferencesNode(ClassSubPreferences::class, SUB)) {
    var hello by stringPref("goodbye")
    var age by intPref(77)
    var agree by booleanPref()
}