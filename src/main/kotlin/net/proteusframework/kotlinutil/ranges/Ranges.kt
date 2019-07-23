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

import net.proteusframework.kotlinutil.std.adjacent
import net.proteusframework.kotlinutil.std.intersects
import net.proteusframework.kotlinutil.std.union

fun List<IntRange>.flatten(): MutableList<IntRange> {
    val newRanges = mutableListOf<IntRange>()
    var lastRange: IntRange = this.first()
    for (range: IntRange in this) {
        if (lastRange == range) continue
        lastRange = if (range.intersects(lastRange) || range.adjacent(lastRange))
            range.union(lastRange)
        else {
            newRanges.add(lastRange)
            range
        }
    }
    if (newRanges.isEmpty() || newRanges.last() != lastRange)
        newRanges.add(lastRange)
    return newRanges
}