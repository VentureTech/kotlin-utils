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

import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.full.NoSuchPropertyException
import kotlin.reflect.full.memberFunctions
import kotlin.system.measureTimeMillis


/**
 * This is a work around since Kotlin does not currently support property references
 * to Java properties. In Kotlin it is handy to say MyType::myProperty.name to get
 * the name of a property. This allows us to specify property names programmatically
 * without hard coded Strings. This javaPropertyName function is a work around to
 * achieve that effect for Java.
 *
 * Use in the following way (with a getter):
 * val somePropertyName: String = javaPropertyName(MyType::getSomeProperty)
 *
 * We could add an extension function to any function reference, but this obscures
 * the intention by adding an excessively broad receiver type.
 */
inline fun <reified T : Any, U> javaPropertyName(noinline property: (T) -> U?): String {
	return T::class.memberFunctions.find { it == property }?.name?.drop(3)?.decapitalize() ?: throw NoSuchPropertyException(
		IllegalArgumentException(
			"Could not interpret a property name for KFunction $property on class ${T::class}."))
}

/**
 * Is this receiver class assignable from a parameter class?
 */
infix fun <T : Any, U : Any> KClass<T>.isAssignableFrom(otherClass: KClass<U>): Boolean {
	return this.java.isAssignableFrom(otherClass.java)
}


/**
 * If a string is blank then return null
 */
fun String.blankToNull(): String? = if (isBlank()) null else this

fun <T, L : List<T>, D : MutableList<T>> L.addAllTo(dest: D) {
	dest.addAll(this)
}

/**
 * Add null as the first element in the collection and return the new list.
 */
fun <T> Collection<T>.nullFirst(): MutableList<T?> = mutableListOf<T?>(null).apply {
	addAll(this@nullFirst)
}

fun <T, D> Iterable<T>.containsAny(toCheck: Iterable<D>?, check: (T, D) -> Boolean): Boolean =
	if (toCheck == null || none()) false else map { toCheck.asSequence().any { tc -> check(it, tc) } }.reduce { b1, b2 -> b1 && b2 }

/**
 * Get the first value within the list for the given predicate.
 *
 * If no value is found, the given supplier is called,
 *
 * and the result of that is added to the list and returned.
 *
 * @param check the predicate
 * @param supp the supplier
 * @return the existing value in the list, or a new value supplied by the supplier
 */
fun <T> MutableList<T>.getOrAdd(check: (T) -> Boolean, supp: () -> T) = find(check) ?: supp().apply { add(this) }

/**
 * Get the second value within the collection.  If collection is smaller than 2 elements, returns null.
 *
 * @return the second value, or null
 */
fun <T> Collection<T>.second() = if (size < 2) null else iterator().apply { next() }.next()

/**
 * Compares each value in the collection with the next value, using the accumulator.
 * Following that, values are collected into the final reduced value using the collector.
 * If collection is empty or smaller than 2 elements, initial value is returned.
 *
 * @param initial the initial reduce value
 * @param accumulator the accumulator
 * @param collector the collector
 * @return the reduced value
 */
tailrec fun <T, R> Collection<T>.compareReducingTo(initial: R, accumulator: (T, T) -> R, collector: (R, R) -> R): R =
	if (isEmpty() || size < 2) initial
	else drop(1).compareReducingTo(collector(initial, accumulator(first(), second()!!)), accumulator, collector)

/**
 * Add the value to the list, returning the value
 *
 * @param value the value to add
 * @return the value
 */
fun <T> MutableList<T>.addReturning(value: T) = run {
	add(value)
	value
}

@JvmOverloads
tailrec fun String.stripDoubleSpaces(result: String = this): String =
	if (!result.contains("  ")) result
	else stripDoubleSpaces(result.replace("  ", " "))

fun <V> Array<List<V>>.shortest(filter: (List<V>) -> Boolean = { true }) =
	if (isEmpty() || none(filter))
		throw IllegalArgumentException("Empty varargs, or all lists filtered out")
	else filter(filter).reduce { l1, l2 -> if (l1.size <= l2.size) l1 else l2 }

val Iterable<*>?.isNullOrEmpty: Boolean
	get () = this == null || !this.iterator().hasNext()

fun <N : Number> N.secondsToTime(): String {
	val seconds = this.toInt()
	var mins = seconds / 60
	if (mins == 0) {
		return "0m ${seconds}s"
	}
	val secs = seconds % 60
	if (mins < 60) {
		return "${mins}m ${secs}s"
	}
	var hours = mins / 60
	mins %= 60
	if (hours < 24) {
		return "${hours}h ${mins}m ${secs}s"
	}
	val days = hours / 24
	hours %= 24
	return "${days}d ${hours}h ${mins}m ${secs}s"
}


/**
 * Get the calendar year of a date.
 */
fun Date.getCalendarYear(): Int {
	val cal = Calendar.getInstance()
	cal.time = this
	return cal.get(Calendar.YEAR)
}


/**
 * This is like [MutableMap.getOrPut] except for [MutableList].
 */
fun <T> MutableList<T>.findOrAdd(
	predicate: (T) -> Boolean,
	createValue: () -> T
) = firstOrNull(predicate) ?: createValue().also { add(it) }

/**
 * This is like [MutableMap.getOrPut] except for [MutableSet].
 */
fun <T> MutableSet<T>.findOrAdd(
	predicate: (T) -> Boolean,
	createValue: () -> T
) = firstOrNull(predicate) ?: createValue().also { add(it) }

fun <T> measureTimeMillisReturning(block: () -> T): kotlin.Pair<Long, T> {
	val result: AtomicReference<T> = AtomicReference()
	val timing = measureTimeMillis { result.set(block()) }
	return timing to result.get()
}