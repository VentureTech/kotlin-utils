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

import java.util.*

/**
 * String Range.
 */
class StringRange
internal constructor(val first: String, vararg val rest: String) : ClosedRange<String>, Iterable<String> {
    override val start: String
        get() = first
    override val endInclusive: String
        get() = if (rest.isEmpty()) first else rest[rest.size - 1]

    override fun iterator(): Iterator<String> = if (rest.isEmpty()) arrayOf(first).iterator() else arrayOf(first, *rest).iterator()

    override operator fun contains(value: String): Boolean = value == first || value in rest
    override fun toString(): String {
        return "StringRange(first='$first', rest=${Arrays.toString(rest)})"
    }


}