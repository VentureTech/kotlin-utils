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

package net.proteusframework.kotlinutil.ranges

import net.proteusframework.kotlinutil.std.intersects
import net.proteusframework.kotlinutil.std.isSubset


/**
 * Defines a set of intervals.
 */
interface Intervals<T : Comparable<T>> : Iterable<T> {
    /**
     * The minimum value in the intervals.
     */
    val start: T

    /**
     * The maximum value in the intervals (inclusive).
     */
    val endInclusive: T

    /**
     * List of ranges that comprise the intervals.
     */
    val ranges: List<ClosedRange<T>>

    /**
     * Checks whether the specified [value] belongs to the intervals.
     */
    operator fun contains(value: T): Boolean = ranges.any { it.contains(value) }

    /**
     * Checks whether the intervals are empty.
     */
    fun isEmpty(): Boolean = ranges.isEmpty()

    /**
     * Checks whether the intervals are not empty.
     */
    fun isNotEmpty(): Boolean = ranges.isNotEmpty()

    fun intersects(other: Intervals<T>): Boolean {
        for (range in ranges) {
            for (otherRange in other.ranges) {
                if (range.intersects(otherRange)) {
                    return true
                }
            }
        }
        return false
    }

    fun isSubset(other: Intervals<T>): Boolean {
        for (range in ranges) {
            var subset = false
            for (otherRange in other.ranges) {
                if (range.isSubset(otherRange)) {
                    subset = true
                }
            }
            if (!subset)
                return false
        }
        return true
    }

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean
}
