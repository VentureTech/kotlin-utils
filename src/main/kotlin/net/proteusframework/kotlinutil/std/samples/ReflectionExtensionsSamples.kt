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

package net.proteusframework.kotlinutil.std.samples

import net.proteusframework.kotlinutil.std.unambiguous

internal fun unambiguousSample() {
	class ClassWithAmbiguity {
		fun doTheThing() {
			// Do Something
		}
		fun doTheThing(arg: Any) {
			// Do Something
		}
	}

	val amb = ClassWithAmbiguity()

	// Does not compile due to resolution ambiguity
	// val doTheThingName = amb::doTheThing.name

	val doTheThingName = unambiguous(amb::doTheThing).name
}