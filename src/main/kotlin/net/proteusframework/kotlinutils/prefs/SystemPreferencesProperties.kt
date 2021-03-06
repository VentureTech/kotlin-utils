/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package net.proteusframework.kotlinutils.prefs


import java.util.prefs.Preferences

/**
 * System Preferences properties class that provides preferences variables or values
 * as a delegated property.
 * @sample net.proteusframework.kotlinutils.prefs.samples.SamplePreferences
 * @author Russ Tennant (russ@proteus.co)
 */
abstract class SystemPreferencesProperties(
    preferencesNode: PreferencesNode,
    override val prefKeyTransform: (String) -> String = { it }
) : PreferencesProperties() {
    override val preferences: Preferences by lazy {
        preferencesNode.getNode(PreferencesNodeType.SYSTEM)
    }
}

