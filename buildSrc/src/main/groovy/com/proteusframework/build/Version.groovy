/*
 * Copyright (c) 2019 Interactive Information Research & Development
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.proteusframework.build

import java.text.SimpleDateFormat

/**
 * Version class for build script.
 * @author Russ Tennant (russ@proteus.co)
 * @since 12/3/13 4:02 PM
 */
class Version
{
    enum Lang {
        Java,
        Kotlin
    }

    /**
     * Optional variable that can be defined in the build.gradle
     * to determine where the ProjectInformation.java class is put
     * when {@link #generateProjectInformationClass(Lang)} is called.
     * If not defined, the package name is <code> "${project.group}.${project.name}"</code>.
     * <p>
     *     Example: <br>
     *     <code>ext[Version.PROJECT_INFORMATION_PACKAGE] = 'net.venturetech.proteus_kotlin.config'
     * </p>
     */
    public static final String PROJECT_INFORMATION_PACKAGE = 'projectInformationPackage'

    /**
     * Capitalize the string based on the delimiters or whitespace if no delimiters are specified.
     *
     * @param s the string.
     * @param delimiters optional delimiters.
     * @return the capitalized string.
     */
    static String capitalize(String s, char... delimiters)
    {
        final int dl
        if (delimiters == null)
        {
            dl = 0
        }
        else
        {
            dl = delimiters.length
            Arrays.sort(delimiters)
        }
        boolean capitalizeNextCharacter = true
        final int sl = s.length()
        final StringBuilder captialized = new StringBuilder(sl)
        for (int i = 0; i < sl; i++)
        {
            final char c = s.charAt(i)
            final boolean isDelimiter
            if (dl == 0)
                isDelimiter = Character.isWhitespace(c)
            else
            {
                isDelimiter = Arrays.binarySearch(delimiters, c) >= 0
            }

            if (isDelimiter)
            {
                captialized.append(c)
                capitalizeNextCharacter = true
            }
            else if (capitalizeNextCharacter)
            {
                captialized.append(Character.toTitleCase(c))
                capitalizeNextCharacter = false
            }
            else
            {
                captialized.append(c)
            }
        }
        return captialized.toString()

    }

    String versionNumber
    String originalVersion
    String thisVersion
    String status
    Date buildTime
    def project

    Version(String versionValue, def project)
    {
        this.project = project
        buildTime = new Date()
        originalVersion = versionValue
        if (originalVersion.endsWith('-SNAPSHOT'))
        {
            status = 'integration'
            versionNumber = originalVersion.substring(0, originalVersion.length() - '-SNAPSHOT'.length())
            //            thisVersion = "${versionNumber}-${getTimestamp()}"
            thisVersion = versionValue
        }
        else
        {
            status = 'release'
            thisVersion = versionValue
            versionNumber = versionValue
        }
    }

    boolean isSnapshot()
    {
        originalVersion.endsWith('-SNAPSHOT')
    }

    String getTimestamp()
    {
        // version is incorporated into iml files - keep simple for VCS
        if(project.isIDEA) return 'SNAPSHOT'

        // Convert local file timestamp to UTC
        def format = new SimpleDateFormat('yyyyMMddHHmmss')
        format.setCalendar(Calendar.getInstance(TimeZone.getTimeZone('Etc/UTC')))
        return format.format(buildTime)
    }

    int getCommitAsNumber()
    {
        Integer.parseInt(project.gitinfo.commit.substring(0,7), 16)
    }

    String toString()
    {
        thisVersion
    }

    private def getCommit(String text)
    {
        def matcher = text =~ /_commit = "([^"]+)/
        return matcher ? matcher[0][1] : 'unknown commit'
    }

    private def getVersion(String text)
    {
        def matcher = text =~ /_version = "([^"]+)/
        return matcher ? matcher[0][1] : 'unknown version'
    }

    /**
     * Generate a version class for the project.
     */
    void generateProjectInformationClass(Lang lang)
    {
        if (lang == Lang.Java) {
            generateProjectInformationJavaClass()
        } else if (lang == Lang.Kotlin) {
            generateProjectInformationKotlinClass()
        } else {
            throw new IllegalArgumentException("Invalid lang: $lang")
        }
    }
    /**
     * Generate a version class for the project.
     */
    void generateProjectInformationJavaClass()
    {
        def now = new SimpleDateFormat('yyyyMMdd\'T\'HHmmss.SSSZ').format(new java.util.Date())
        def srcDir = project.sourceSets.main.java.srcDirs.iterator().next()
        def packageName = project.hasProperty(PROJECT_INFORMATION_PACKAGE) ?
            project[PROJECT_INFORMATION_PACKAGE] : "${project.group.toString()}.${project.name}"
        def packageDir = new File(srcDir, packageName.replace('.', '/'))
        packageDir.mkdirs()
        def gitIgnore = new File(packageDir, '.gitignore')
        if(!gitIgnore.exists()) {
            gitIgnore.text = """# Ignore auto generated file ProjectInformation.java
ProjectInformation.java
.gitignore"""
        }
        def generatedClass = new File(packageDir, 'ProjectInformation.java')
        if(generatedClass.exists()
            && getCommit(generatedClass.text) == project?.gitinfo?.commit?:''
            && getVersion(generatedClass.text) == versionNumber
        )
        {
            return
        }
        generatedClass.text = """/*
 * Copyright (c) 2019 Interactive Information Research & Development
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ${packageName};

//import javax.annotation.processing.Generated;

//@Generated(value= "Generated by build system", date = "${now}")
/**
 * Project Information.
 * @author Auto Generated (noreply@i2rd.com)
 */
 @SuppressWarnings("ALL")
public final class ProjectInformation
{
    /** Application Name. */
    public static final String APPLICATION_NAME = "${capitalize(project.name.replace('-', ' '))}";
    /** Name. */
    private static final String _name = "${project.name}";
    /** Group. */
    private static final String _group = "${project.group}";
    /** Version. */
    private static final String _version = "${versionNumber}";
    /** Full Version (optionally includes SNAPSHOT qualifier). */
    private static final String _fullVersion = "${thisVersion}";
    /** Commit. */
    private static final String _commit = "${project?.gitinfo?.commit?:''}";
    /** Branch. */
    private static final String _branch = "${project?.gitinfo?.branch?:''}";
    /** Status: release, milestone, integration. */
    private static final String _status = "${status}";

    /**
    * Utility class constructor.
    */
    private ProjectInformation()
    {
    }

    /**
     * Get the project name: "{@value #_name}"
     * @return the name.
     */
    public static String getName() 
    { 
        return _name; 
    }

    /**
     * Get the project group: "{@value #_group}"
     * @return the group.
     */
    public static String getGroup() 
    { 
        return _group; 
    }

    /**
     * Get the project version: "{@value #_version}"
     * @return the version.
     */
    public static String getVersion() 
    { 
        return _version; 
    }

    /**
     * Get the project version: "{@value #_fullVersion}"
     * @return the version.
     */
    public static String getFullVersion() 
    { 
        return _fullVersion; 
    }

    /**
     * Get the commit id: "{@value #_commit}"
     * @return the commit id.
     */
    public static String getCommit() 
    { 
        return _commit; 
    }

    /**
     * Get the branch: "{@value #_branch}"
     * @return the branch.
     */
    public static String getBranch() 
    { 
        return _branch; 
    }

    /**
     * Get the project status: "{@value #_status}"
     * @return the status.
     */
    public static String getStatus() 
    { 
        return _status; 
    }

    /**
     * Get the major version number.
     * @return the major version number
     * @throws NumberFormatException if version is in the wrong format.
     * @throws IndexOutOfBoundsException if version is in the wrong format.
     */
    public static int getMajorVersion() 
    { 
        return Integer.parseInt(getVersion().substring(0, getVersion().indexOf('.'))); 
    }
    
}
"""

    }

    /**
     * Generate a version class for the project.
     */
    void generateProjectInformationKotlinClass()
    {
        def srcDir = project.sourceSets.main.kotlin.srcDirs.iterator().next()
        def packageName = project.hasProperty(PROJECT_INFORMATION_PACKAGE) ?
            project[PROJECT_INFORMATION_PACKAGE] : "${project.group.toString()}.${project.name}"
        def packageDir = new File(srcDir, packageName.replace('.', '/'))
        packageDir.mkdirs()
        def gitIgnore = new File(packageDir, '.gitignore')
        if(!gitIgnore.exists()) {
            gitIgnore.text = """# Ignore auto generated file ProjectInformation.kt
ProjectInformation.kt
.gitignore"""
        }
        def generatedClass = new File(packageDir, 'ProjectInformation.kt')
        if (generatedClass.exists()
            && getCommit(generatedClass.text) == project?.gitinfo?.commit?:''
            && getVersion(generatedClass.text) == versionNumber
        )
        {
            return
        }
        generatedClass.text = """/*
 * Copyright (c) 2019 Interactive Information Research & Development
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package ${packageName}

/**
 * Project Information.
 * @author Auto Generated (noreply@i2rd.com)
 */
object ProjectInformation
{
    /** Application Name. */
    const val APPLICATION_NAME = "${capitalize(project.name.replace('-', ' '))}"
    /** Name. */
    const val NAME = "${project.name}"
    /** Group. */
    const val GROUP = "${project.group}"
    /** Version. */
    const val VERSION = "${versionNumber}"
    /** Full Version (optionally includes SNAPSHOT qualifier). */
    const val FULL_VERSION = "${thisVersion}"
    /** Commit. */
    const val COMMIT = "${project?.gitinfo?.commit?:''}"
    /** Branch. */
    const val BRANCH = "${project?.gitinfo?.branch?:''}"
    /** Status: release, milestone, integration. */
    const val STATUS = "${status}"
    /** Major part of version. */
    const val MAJOR_VERSION: Int = ${Integer.parseInt(versionNumber.substring(0, versionNumber.indexOf('.')))}

    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getName() = NAME
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getGroup() = GROUP
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getVersion() = VERSION
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getFullVersion() = FULL_VERSION
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getMajorVersion() = MAJOR_VERSION
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getCommit() = COMMIT
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getBranch() = BRANCH
    /** Java API Compatibility with ProjectInformation.java. */
    @JvmStatic
    fun getStatus() = STATUS
}
"""
    }
}
