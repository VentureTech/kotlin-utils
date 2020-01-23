/*
 *  Copyright (c) Interactive Information R & D (I2RD) LLC.
 *  All Rights Reserved.
 *   
 *  This software is confidential and proprietary information of
 *  I2RD LLC ("Confidential Information"). You shall not disclose
 *  such Confidential Information and shall use it only in 
 *  accordance with the terms of the license agreement you entered
 *  into with I2RD.
 */
package net.proteusframework.kotlinutils.prefs

import java.util.*
import java.util.prefs.AbstractPreferences
import java.util.prefs.BackingStoreException

/**
 * Memory based preferences. Note that care should be taken that an instance of this class is not accessible to the installed
 * preferences factory.
 *
 * @author Pat B. Double (double@i2rd.com)
 */
class MemoryPreferences(
    /** The parent node.  */
    parent: AbstractPreferences?,
    /** The node name.  */
    name: String,
    private val userNode: Boolean
) : AbstractPreferences(parent, name) {
    /** The list of children.  */
    private val _children: MutableSet<String> = HashSet()
    /** The preference data.  */
    private val _data: MutableMap<String, String> = HashMap()

    override fun childSpi(name: String): AbstractPreferences {
        _children.add(name)
        return MemoryPreferences(this, name, userNode)
    }

    @Throws(BackingStoreException::class)
    override fun childrenNamesSpi(): Array<String> {
        return _children.toTypedArray()
    }

    @Throws(BackingStoreException::class)
    override fun flushSpi() { // Nothing to do
    }

    override fun getSpi(key: String): String {
        return _data[key]!!
    }

    @Throws(BackingStoreException::class)
    override fun keysSpi(): Array<String> {
        return _data.keys.toTypedArray()
    }

    override fun putSpi(key: String, value: String) {
        _data[key] = value
    }

    @Throws(BackingStoreException::class)
    override fun removeNodeSpi() {
        _children.clear()
    }

    override fun removeSpi(name: String) {
        _data.remove(name)
    }

    @Throws(BackingStoreException::class)
    override fun syncSpi() { // Nothing to do
    }

    override fun isUserNode(): Boolean {
        return userNode
    }

}