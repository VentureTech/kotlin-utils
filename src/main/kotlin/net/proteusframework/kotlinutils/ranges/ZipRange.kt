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


import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import net.proteusframework.kotlinutils.collections.ConcatenatedIterator
import net.proteusframework.kotlinutils.json.JSONFactory
import net.proteusframework.kotlinutils.json.JSONProducer
import net.proteusframework.kotlinutils.std.length


/**
 * ZipCode Range.
 */
class ZipRange private constructor(
    val spec: String,
    @Transient
    override val ranges: List<IntRange>,
    @Transient
    override val start: Int,
    @Transient
    override val endInclusive: Int) :
    Intervals<Int>, JSONProducer {

    companion object : JSONFactory<ZipRange> {

        private val REGEX_WHITESPACE = "\\s".toRegex()
        private val DO_NOT_COLLAPSE_3_DIGIT = setOf("09800", "09600", "20500")

        @JvmStatic
        @FromJson
        override fun fromJSON(json: String): ZipRange =
            of(json)

        @JvmStatic
        @ToJson
        fun toJSON(zipRange: ZipRange): String = zipRange.toJSON()

        /**
         * Create a ZipRange from the specified 5 digit zip code.
         *
         * @param fiveDigit a 5 digit zip code.
         * @return the ZipRange.
         */
        @JvmStatic
        fun convertTo3DigitIfPossible(fiveDigit: String): ZipRange {
            assert(fiveDigit.length == 5) { "Expecting 5 digit zip." }
            if (fiveDigit.endsWith("00") && fiveDigit !in DO_NOT_COLLAPSE_3_DIGIT)
                return of(fiveDigit.substring(0..2))
            else
                return of(fiveDigit)
        }

        /**
         * Create a ZipRange from the specified range string.
         *
         * Examples:
         *
         * - "685"
         * - "685-686"
         * - "68516"
         * - "68516-68528"
         *
         * @param spec a three letter prefix, a prefix range, a 5 letter zip code, or a 5 letter zip code range.
         * Can be a comma separated list of the previously described formats.
         * @return the ZipRange.
         */
        @JvmStatic
        fun of(spec: String): ZipRange {
            val ranges = mutableListOf<IntRange>()
            if (spec.isNotBlank()) {
                spec.split(',').let {
                    if (it.isEmpty()) listOf(spec)
                    else it
                }.forEach { rawZippy ->
                    val zippy = rawZippy.replace(REGEX_WHITESPACE, "")
                    when (zippy.length) {
                        3    -> {
                            ranges.add(pad3(zippy))
                        }
                        5    -> {
                            val beginAndEnd = zippy.toInt()
                            ranges.add(IntRange(beginAndEnd, beginAndEnd))
                        }
                        7    -> {
                            val begin = zippy.substring(0..2).padEnd(5, '0').toInt()
                            val end = zippy.substring(4..6).padEnd(5, '9').toInt()
                            ranges.add(IntRange(begin, end))
                        }
                        11   -> {
                            val begin = zippy.substring(0..4).toInt()
                            val end = zippy.substring(6..10).toInt()
                            ranges.add(IntRange(begin, end))
                        }
                        else -> throw IllegalArgumentException("Invalid range: $zippy")
                    }
                }
            }
            ranges.sortBy { it.start }
            var start = 0
            var endIntRange = 0
            if (spec.isNotBlank()) {
                start = ranges.first().start
                endIntRange = ranges.last().endInclusive
            }
            return ZipRange(spec, ranges, start, endIntRange)
        }

        private fun pad3(threeDigit: String): IntRange {
            assert(threeDigit.length == 3) { "Expecting argument to be 3 characters." }
            val begin = threeDigit.padEnd(5, '0').toInt()
            val end = threeDigit.padEnd(5, '9').toInt()
            return IntRange(begin, end)
        }
    }

    fun length() = ranges.sumBy { it.length() }

    fun atIndex(index: Int): Int {
        var mIndex = index
        for (range in ranges) {
            if (mIndex >= range.length()) {
                mIndex -= range.length()
            } else {
                return range.start + mIndex
            }
        }
        throw IndexOutOfBoundsException("Invalid index: $index")
    }

    override fun iterator(): Iterator<Int> {
        if (spec.isBlank()) return emptyList<Int>().iterator()
        val list: MutableList<Iterator<Int>> = ranges.map { it.iterator() }.toMutableList()
        return ConcatenatedIterator(list.iterator())
    }

    override fun toJSON(): String = spec

    override fun isEmpty(): Boolean = spec.isBlank()

    fun addSpec(otherSpec: String): ZipRange = when {
        otherSpec.isBlank() -> this.normalize()
        spec.isBlank()      -> of(otherSpec).normalize()
        else                -> of("$spec,$otherSpec").normalize()
    }

    fun addRange(range: ClosedRange<Int>): ZipRange {
        val start = range.start.toString().padStart(5, '0')
        val end = range.endInclusive.toString().padStart(5, '0')
        return addSpec("$start-$end")
    }

    fun removeSpec(otherSpec: String): ZipRange {
        if (otherSpec.isBlank()) return this.normalize()
        else if (spec.isBlank()) return this
        val otherZipRange = of(otherSpec)
        return of(filter { it !in otherZipRange }
            .joinToString(separator = ",") { it.toString().padStart(5, '0') }).normalize()
    }

    /**
     * Normalize the ZipRange, collapsing any zip codes that are consecutive into a range declaration (ex: 68516-68716)
     */
    fun normalize(): ZipRange {
        if (spec.isBlank()) return this
        val newRanges = ranges.flatten()
        // split ranges to collapse to 3 digits if possible
        for (orig in newRanges.toList()) {
            var start = orig.first
            var endInclusive = orig.endInclusive
            if (start.toString().endsWith("00") && orig.endInclusive.toString().endsWith("99"))
                continue
            val stuff = (endInclusive - start) / 100
            if (stuff > 0) {
                while (!start.toString().endsWith("00")) start++
                while (!endInclusive.toString().endsWith("99")) endInclusive--
                if (start < endInclusive) {
                    newRanges.remove(orig)
                    if (orig.first != start) {
                        val range = orig.first..(start - 1)
                        newRanges.add(range)
                    }
                    newRanges.add(start..endInclusive)
                    if (orig.endInclusive > endInclusive) {
                        newRanges.add((endInclusive + 1)..orig.endInclusive)
                    }
                }
            }
        }
        newRanges.sortBy { it.start }

        val spec = mutableListOf<String>()
        for (range in newRanges) {
            if (range.start == range.endInclusive) {
                spec.add(range.start.toString().padStart(5, '0'))
            } else {
                var start = range.start.toString().padStart(5, '0')
                var end = range.endInclusive.toString().padStart(5, '0')
                if (start.endsWith("00") && end.endsWith("99")) {
                    start = start.substring(0, 3)
                    end = end.substring(0, 3)
                    if (start != end)
                        spec.add("$start-$end")
                    else
                        spec.add(start)
                } else
                    spec.add("$start-$end")
            }
        }
        return ZipRange(spec.joinToString(separator = ","),
            newRanges,
            newRanges.first().start,
            newRanges.last().endInclusive)
    }

    operator fun contains(other: String) = contains(other.toInt(10))

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (!(other is ZipRange)) return false
        return spec == other.spec
    }

    override fun hashCode(): Int = (start.hashCode() * 31) + endInclusive.hashCode()

    override fun toString(): String = spec
}
