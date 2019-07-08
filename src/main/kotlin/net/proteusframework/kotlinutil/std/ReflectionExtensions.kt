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

package net.proteusframework.kotlinutil.std

import kotlin.reflect.KFunction

/**
 * Get a [KFunction] for the given [KFunction] that has overloads.
 *
 * Kotlin makes it impossible to get a function reference for reflection if the function has overloads.
 * This method allows for getting an unambiguous [KFunction] for those overloaded function references.
 *
 * @param fn the [KFunction] to get the unambiguous [KFunction] for.
 * @return the unambiguous [KFunction]
 * @sample net.proteusframework.kotlin.std.samples.unambiguousSample
 */
fun <T : KFunction<*>> unambiguous(fn: T) = fn as KFunction<*>