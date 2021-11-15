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

package net.proteusframework.kotlinutils.ranges

import net.proteusframework.kotlinutils.collections.ConcatenatedIterator
import net.proteusframework.kotlinutils.std.length


class IntIntervals private constructor(
    override val ranges: List<IntRange>,
    override val start: Int,
    override val endInclusive: Int
) : Intervals<Int> {

    override fun iterator(): Iterator<Int> {
        val list: MutableList<Iterator<Int>> = ranges
            .map { IntRange(it.first, it.last).iterator() }
            .toMutableList()
        return ConcatenatedIterator(list.iterator())
    }

    override fun toString(): String {
        return "IntIntervals(ranges=$ranges, start=$start, endInclusive=$endInclusive)"
    }

    fun length() = ranges.sumBy { it.length() }

    fun atIndex(index: Int): Int {
        var mIndex = index
        for (range in ranges) {
            if (mIndex > range.length()) {
                mIndex = -range.length()
            } else {
                return range.first + mIndex
            }
        }
        throw IndexOutOfBoundsException("Invalid index: $index")
    }

    fun normalize(): IntIntervals {
        if (ranges.isEmpty()) return this
        val newRanges = ranges.flatten()
        val spec = mutableListOf<String>()
        for (range in newRanges) {
            if (range.first == range.last)
                spec.add(range.first.toString())
            else {
                val start = range.first
                val end = range.last.toString()
                spec.add("$start-$end")
            }
        }
        return of(spec.joinToString(separator = ","))
    }

    override fun hashCode(): Int = (start * 31) + endInclusive

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntIntervals) return false
        if (ranges.size != other.ranges.size) return false
        for ((index, range) in ranges.withIndex()) {
            if (range != other.ranges[index])
                return false
        }
        return true
    }

    companion object {
        fun of(ranges: MutableList<ClosedRange<Int>>): IntIntervals {
            ranges.sortBy { it.start }
            return if (ranges.isEmpty())
                IntIntervals(emptyList(), -1, -1)
            else {
                IntIntervals(ranges.map {
                    when (it) {
                        is IntRange -> it
                        else        -> IntRange(it.start, it.endInclusive)
                    }
                }, ranges.first().start, ranges.last().endInclusive)
            }
        }

        /**
         * Create a IntIntervals from the specified range string.
         *
         * Examples:
         *
         * - "685"
         * - "685-686"
         * - "68516"
         * - "68516-68528"
         *
         * @param spec a comma separated list of numbers or ranges (dash delimited)
         * @return the IntIntervals.
         */
        @JvmStatic
        fun of(spec: String): IntIntervals {
            val ranges = mutableListOf<IntRange>()
            if (spec.isNotBlank()) {
                spec.split(',').forEach { item ->
                    if (item.contains('-')) {
                        val split = item.split('-')
                        if (split.size != 2 || split[0].isBlank() || split[1].isBlank())
                            throw IllegalArgumentException("Invalid spec: $spec")
                        ranges.add(IntRange(split[0].toInt(), split[1].toInt()))
                    } else {
                        val n = item.toInt()
                        ranges.add(IntRange(n, n))
                    }
                }
            }
            ranges.sortBy { it.first }
            var start = 0
            var endIntRange = 0
            if (spec.isNotBlank()) {
                start = ranges.first().first
                endIntRange = ranges.last().last
            }
            return IntIntervals(ranges, start, endIntRange)
        }
    }
}
