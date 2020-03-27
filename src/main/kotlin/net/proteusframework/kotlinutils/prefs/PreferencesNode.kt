package net.proteusframework.kotlinutils.prefs

import net.proteusframework.kotlinutils.prefs.PreferencesNodeType.SYSTEM
import net.proteusframework.kotlinutils.prefs.PreferencesNodeType.USER
import java.util.prefs.Preferences
import kotlin.reflect.KClass

private fun nodeName(klazz: KClass<*>): String {
    require(!klazz.java.isArray) { "Arrays have no associated preferences node." }
    val qualifiedName = klazz.qualifiedName!!
    return "/${qualifiedName.replace('.', '/')}"
}

enum class PreferencesNodeType {
    SYSTEM,
    USER
}

interface PreferencesNode {
    fun getNode(type: PreferencesNodeType): Preferences
}

class PackagePreferencesNode(
    /** We will use the qualified name of this class's package as the basis of the node path. */
    private val nodePackageKClass: KClass<*>,
    /** Optional sub node path relative to [#nodePackageKClass] package name. */
    private val subNodes: Array<String> = emptyArray<String>()
) : PreferencesNode {
    override fun getNode(type: PreferencesNodeType): Preferences = when (type) {
        SYSTEM -> Preferences.systemNodeForPackage(nodePackageKClass.java)
        USER   -> Preferences.userNodeForPackage(nodePackageKClass.java)
    }.node(subNodes.joinToString("/"))
}

class PathPreferencesNode(
    /** Node path for the preferences' node. */
    private val nodePath: Array<String> = emptyArray<String>()
) : PreferencesNode {
    override fun getNode(type: PreferencesNodeType): Preferences = when (type) {
        SYSTEM -> Preferences.systemRoot()
        USER   -> Preferences.userRoot()
    }.node(nodePath.joinToString("/"))
}

class ClassPreferencesNode(
    /** We will use the qualified name of this class as the basis of the node path. */
    private val nodeKClass: KClass<*>,
    /** Optional sub node path relative to [#nodeKClass] class name. */
    private val subNodes: Array<String> = emptyArray<String>()
) : PreferencesNode {
    override fun getNode(type: PreferencesNodeType): Preferences = when (type) {
        SYSTEM -> Preferences.systemRoot()
        USER   -> Preferences.userRoot()
    }.node(nodeName(nodeKClass)).node(subNodes.joinToString("/"))
}

/**
 * Implement to provide a property specific sub node.
 */
abstract class EnvironmentPreferencesNode(
    private val delegate: PreferencesNode,
    /** Property Name. */
    private val propertyName: String,
    /** Default property value if not found. Set to null if required. */
    private val propertyValueDefault: String? = null
) : PreferencesNode {

    abstract fun getProperty(propertyName: String): String?

    override fun getNode(type: PreferencesNodeType): Preferences {
        val nodePath = getProperty(propertyName) ?: let {
            if (propertyValueDefault == null)
                throw IllegalStateException("No value for property: $propertyName")
            else
                propertyValueDefault
        }
        return delegate.getNode(type).node(nodePath)
    }
}
