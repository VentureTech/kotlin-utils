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

package net.proteusframework.kotlinutils.std

import net.proteusframework.kotlinutils.ranges.IntIntervals
import net.proteusframework.kotlinutils.ranges.Intervals
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.full.NoSuchPropertyException
import kotlin.reflect.full.memberFunctions
import kotlin.streams.asSequence
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

fun <V> Spliterator<V>.asSequence() = StreamSupport.stream(this, false).asSequence()

internal val numberCharacters = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.', 'E')
fun String.isNumber(): Boolean {
	for (c in this) {
		if (!(c in numberCharacters))
			return false
	}
	return true
}


fun ClosedRange<Int>.atIndex(index: Int): Int {
	if (index >= length()) throw IndexOutOfBoundsException("Invalid index: $index")
	return start + index
}

fun ClosedRange<Int>.length() = (endInclusive - start) + 1

fun <T : Comparable<T>> ClosedRange<T>.intersects(other: ClosedRange<T>): Boolean {
	val s1 = start
	val s2 = other.start
	val e1 = endInclusive
	val e2 = other.endInclusive
	if (s1 is String && e1 is String && s2 is String && e2 is String
		&& s1.isNumber() && e1.isNumber() && s2.isNumber() && e2.isNumber()
	) {
		return s1.toLong(10) <= e2.toLong(10) && s2.toLong(10) <= e1.toLong(10)
	}

	return s1 <= e2 && s2 <= e1

}

fun <T : Comparable<T>> ClosedRange<T>.isSubset(other: ClosedRange<T>): Boolean {
	val s1 = start
	val s2 = other.start
	val e1 = endInclusive
	val e2 = other.endInclusive

	return s1 >= s2 && e1 <= e2

}

fun ClosedRange<Int>.adjacent(other: ClosedRange<Int>): Boolean {
	val s1 = start
	val s2 = other.start
	val e1 = endInclusive
	val e2 = other.endInclusive
	return if (e1 < s2) s2 - e1 == 1 else s1 - e2 == 1
}

fun ClosedRange<Int>.union(other: ClosedRange<Int>): IntRange =
	IntRange(Math.min(start, other.start), Math.max(endInclusive, other.endInclusive))


fun ClosedRange<Int>.diff(other: ClosedRange<Int>): Intervals<Int> {
	val s1 = start
	val s2 = other.start
	val e1 = endInclusive
	val e2 = other.endInclusive

	if (isSubset(other)) {
		val ranges = mutableListOf<ClosedRange<Int>>()
		if (s1 != s2) ranges.add(IntRange(s2, s1 - 1))
		if (e1 != e2) ranges.add(IntRange(e1 + 1, e2))
		return IntIntervals.of(ranges)

	} else if (other.isSubset(this)) {
		val ranges = mutableListOf<ClosedRange<Int>>()
		if (s1 != s2) ranges.add(IntRange(s1, s2 - 1))
		if (e1 != e2) ranges.add(IntRange(e2 + 1, e1))
		return IntIntervals.of(ranges)
	} else if (intersects(other)) {
		val ranges = mutableListOf<ClosedRange<Int>>()
		if (s2 < s1) {
			if (s2 + 1 != s1)
				ranges.add(IntRange(s2, s1 - 1))
		} else if (s1 < s2) {
			if (s2 - 1 != s1)
				ranges.add(IntRange(s1, s2 - 1))
		}

		if (e2 < e1) {
			if (e2 + 1 != e1)
				ranges.add(IntRange(e2 + 1, e1))
		} else if (e1 < e2) {
			if (e2 - 1 != e1)
				ranges.add(IntRange(e1 + 1, e2))
		}
		return IntIntervals.of(ranges)
	} else {
		return IntIntervals.of(mutableListOf(this, other))
	}
}


fun String.fileBasename(): String {
	// there are libraries that do this, but didn't want to add a dep for such a trivial method
	var baseName = this
	val i = this.lastIndexOf('.')
	if (i > 0)
		baseName = this.substring(0, i)
	return baseName
}

fun String.fileExtension(): String {
	// there are libraries that do this, but didn't want to add a dep for such a trivial method
	var extension = ""
	val i = this.lastIndexOf('.')
	if (i > 0)
		extension = this.substring(i + 1)
	return extension
}


fun String.truncate(newLength: Int) = if (length > newLength) substring(0, newLength) else this