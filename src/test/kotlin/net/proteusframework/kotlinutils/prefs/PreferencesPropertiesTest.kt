package net.proteusframework.kotlinutils.prefs

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.prefs.Preferences

@TestMethodOrder(MethodOrderer.Alphanumeric::class)
internal class PreferencesPropertiesTest {

    @Test
    fun test01Path() {
        assertEquals(PathPreferences.hello, "goodbye")
        PathPreferences.hello = "hello"
        assertEquals(PathPreferences.hello, "hello")
        val root = Preferences.userRoot()
        val relativePath = PathPreferences.path.joinToString("/")
        assertTrue(root.nodeExists(relativePath))
        val node = root.node(relativePath)
        assertFalse(node.keys().contains("age"))
        PathPreferences.age = 22
        assertEquals(PathPreferences.age, 22)
        assertTrue(node.keys().contains("age"))
    }

    @Test
    fun test02Package() {
        val root = Preferences.userRoot()
        assertFalse(PackagePreferences.agree)
        PackagePreferences.agree = true
        assertTrue(PackagePreferences.agree)
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs"))
        assertFalse(root.nodeExists("net/proteusframework/kotlinutils/prefs/PackagePreferences"))
        assertEquals(PackagePreferences.age2, 77)
        PackagePreferences.age2 = 11
        assertEquals(PackagePreferences.age2, 11)
    }

    @Test
    fun test03PackageSub() {
        val root = Preferences.userRoot()
        assertFalse(PackageSubPreferences.agree)
        PackageSubPreferences.agree = true
        assertTrue(PackageSubPreferences.agree)
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs"))
        assertFalse(root.nodeExists("net/proteusframework/kotlinutils/prefs/PackagePreferences"))
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs/sub"))

        // I intentionally created an overlap between 2 different PreferencesProperties implementations
        assertNotEquals(PackageSubPreferences.age2, 77)
        assertEquals(PackageSubPreferences.age2, 11 /* from previous test*/)
    }

    @Test
    fun test04Class() {
        val root = Preferences.userRoot()
        assertFalse(ClassPreferences.agree)
        ClassPreferences.agree = true
        assertTrue(ClassPreferences.agree)
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs"))
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs/ClassPreferences"))
    }

    @Test
    fun test05ClassSub() {
        val root = Preferences.userRoot()
        assertFalse(ClassSubPreferences.agree)
        ClassSubPreferences.agree = true
        assertTrue(ClassSubPreferences.agree)
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs"))
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs/ClassSubPreferences"))
        assertTrue(root.nodeExists("net/proteusframework/kotlinutils/prefs/ClassSubPreferences/sub"))
    }

    companion object {
        init {
            MemoryPreferencesFactory.install()

        }
    }
}