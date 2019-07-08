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

package net.proteusframework.kotlinutil.data

import org.hibernate.Hibernate.getClass
import java.io.Serializable
import java.util.*

abstract class KEntity<T : Serializable> {

	abstract var id: T

	override fun equals(other: Any?): Boolean = when {
		this === other                    -> true
		other == null                     -> false
		getClass(other) != getClass(this) -> false
		else                              -> Objects.equals(id, (other as KEntity<*>).id)
	}


	override fun hashCode(): Int = id.hashCode()

	/**
	 * Returns "${Hibernate.getClass(this).simpleName}#${this.id}"
	 */
	override fun toString(): String = "${getClass(this).simpleName}#${this.id}"

}