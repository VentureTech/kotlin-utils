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

import com.winterbe.expekt.should
import net.proteusframework.kotlinutils.std.diff
import net.proteusframework.kotlinutils.std.intersects
import net.proteusframework.kotlinutils.std.isSubset
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class IntervalsTest : Spek({

    describe("an IntRange that is a subset of another") {
        val r1 = 3..4
        val r2 = 1..10
        it("should be a subset") {
            (r1.isSubset(r2)).should.be.`true`
        }
        it("should not be a subset in reverse") {
            (r2.isSubset(r1)).should.be.`false`
        }
        it("should intersect both directions") {
            (r1.intersects(r2)).should.be.`true`
            (r2.intersects(r1)).should.be.`true`
        }
        it("should have a consistent diff in both directions") {
            val diff1 = r1.diff(r2)
            val diff2 = r2.diff(r1)
            diff1.should.equal(diff2)
            diff1.ranges[0].start.should.equal(1)
            diff1.ranges[0].endInclusive.should.equal(2)
            diff1.ranges[1].start.should.equal(5)
            diff1.ranges[1].endInclusive.should.equal(10)
        }
    }

    describe("an IntRange that intersects with another") {
        val r1 = 5..14
        val r2 = 1..10
        it("should not be a subset") {
            (r1.isSubset(r2)).should.be.`false`
        }
        it("should not be a subset in reverse") {
            (r2.isSubset(r1)).should.be.`false`
        }
        it("should intersect both directions") {
            (r1.intersects(r2)).should.be.`true`
            (r2.intersects(r1)).should.be.`true`
        }
        it("should have a consistent diff in both directions") {
            val diff1 = r1.diff(r2)
            val diff2 = r2.diff(r1)
            diff1.should.equal(diff2)
            diff1.ranges[0].start.should.equal(1)
            diff1.ranges[0].endInclusive.should.equal(4)
            diff1.ranges[1].start.should.equal(11)
            diff1.ranges[1].endInclusive.should.equal(14)
        }
    }


    describe("an IntRange that does not intersect with another") {
        val r1 = 15..24
        val r2 = 1..10
        it("should not be a subset") {
            (r1.isSubset(r2)).should.be.`false`
        }
        it("should not be a subset in reverse") {
            (r2.isSubset(r1)).should.be.`false`
        }
        it("should not intersect both directions") {
            (r1.intersects(r2)).should.be.`false`
            (r2.intersects(r1)).should.be.`false`
        }
        it("should have a consistent diff in both directions") {
            val diff1 = r1.diff(r2)
            val diff2 = r2.diff(r1)
            diff1.should.equal(diff2)
            diff1.ranges[0].start.should.equal(1)
            diff1.ranges[0].endInclusive.should.equal(10)
            diff1.ranges[1].start.should.equal(15)
            diff1.ranges[1].endInclusive.should.equal(24)
        }
    }

    describe("duplicate IntRanges") {
        val r1 = 15..24
        val r2 = 15..24
        it("should be a subset") {
            (r1.isSubset(r2)).should.be.`true`
        }
        it("should be a subset in reverse") {
            (r2.isSubset(r1)).should.be.`true`
        }
        it("should intersect both directions") {
            (r1.intersects(r2)).should.be.`true`
            (r2.intersects(r1)).should.be.`true`
        }
        it("should have a consistent diff in both directions") {
            val diff1 = r1.diff(r2)
            val diff2 = r2.diff(r1)
            println(diff1)
            println(diff2)
            diff1.should.equal(diff2)
            diff1.ranges.should.be.empty
        }
    }
})