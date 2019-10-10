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
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class ZipRangeTest : Spek({
    describe("a empty ZipRange spec") {
        val zr = ZipRange.of("")
        it("should be empty") {
            zr.isEmpty().should.be.`true`
        }
        it("should have the correct length") {
            zr.length().should.be.equal(0)
        }
    }
    describe("a ZipRange with only inclusive range and a test Zip in range") {
        val testzip = "68008"
        val zr = ZipRange.of("68007-68009")
        it("should return in-range") {
            (testzip in zr).should.be.`true`
        }
        it("should return the correct zip by index") {
            zr.atIndex(0).should.be.equal(68007)
            zr.atIndex(1).should.be.equal(68008)
            zr.atIndex(2).should.be.equal(68009)
        }
    }
    describe("a ZipRange with only inclusive range and a test Zip not in range") {
        val testzip = "68008"
        val zr = ZipRange.of("78007-78009")
        it("should return not in-range") {
            (testzip in zr).should.be.`false`
        }
        it("should have the correct length") {
            zr.length().should.be.equal(3)
        }
    }
    describe("a ZipRange with non-continuous range") {
        val testzip = "68010,68005,68008"
        val zr = ZipRange.of(testzip)
        it("should return not in-range") {
            ("68006" in zr).should.be.`false`
            ("68004" in zr).should.be.`false`
            ("68009" in zr).should.be.`false`
            ("68005" in zr).should.be.`true`
            ("68008" in zr).should.be.`true`
            ("68010" in zr).should.be.`true`
            zr.start.should.be.equal(68005)
        }
        it("should have the correct length") {
            zr.length().should.be.equal(3)
        }
        it("should return the correct zip by index") {
            zr.atIndex(0).should.be.equal(68005)
            zr.atIndex(1).should.be.equal(68008)
            zr.atIndex(2).should.be.equal(68010)
        }
    }
    describe("a ziprange with multiple concurrent zipcodes") {
        it("should collapse zipcodes into range") {
            val start = ZipRange.of("100-102,10300,10301,10302,10303,10304,10305,10306,10307,10308,10309,10310")
            start.normalize().spec.should.be.equal("100-102,10300-10310")
        }
    }
    describe("a ziprange with multiple ranges and gaps") {
        val zr = ZipRange.of("100,102,103")
        it("should collapse ranges that it can collapse, but keep the gaps") {
            zr.normalize().spec.should.be.equal("100,102-103")
        }
        it("should have the correct length") {
            zr.length().should.be.equal(300)
        }
        it("should return the correct zip by index") {
            zr.atIndex(0).should.be.equal(10000)
            zr.atIndex(1).should.be.equal(10001)
            zr.atIndex(98).should.be.equal(10098)
            zr.atIndex(99).should.be.equal(10099)
            zr.atIndex(100).should.be.equal(10200)
            zr.atIndex(101).should.be.equal(10201)
        }
    }

    describe("a 3 digit ziprange the spec remains 3 digits") {
        val zr = ZipRange.of("100").addSpec("006-009")
        it("should return the original spec ordered") {
            zr.spec.should.be.equal("006-009,100")
        }
    }

    describe("a collapsible 3 digit ziprange the spec is collapsed to 3 digits") {
        val zr = ZipRange.of("100").addSpec("00699-00901")
        it("should return the original spec ordered") {
            zr.spec.should.be.equal("00699,007-008,00900-00901,100")
        }
    }

    describe("a ziprange that spans over 100 values that is bounded on 00 and 99") {
        val zr = ZipRange.of("100,10600-10999,00150-00250")
        it("should return the original spec ordered") {
            zr.normalize().spec.should.be.equal("00150-00250,100,106-109")
        }
    }

    describe("a ziprange that spans over 100 values that is bounded on 00 and 99 and has spaces") {
        val zr = ZipRange.of("100, 10600-10999, 00150-00250")
        it("should return the original spec ordered") {
            zr.normalize().spec.should.be.equal("00150-00250,100,106-109")
        }
    }

    describe("normalizing 2 consecutive 5 digits") {
        val zr = ZipRange.of("68516,68517")
        it("should return a sane range") {
            zr.normalize().spec.should.be.equal("68516-68517")
        }
    }

    describe("a ziprange and a spec to remove") {
        val zr = ZipRange.of("000-999")
        it("should properly remove the zips in the spec to remove from the ziprange") {
            zr.removeSpec("68501").spec.should.be.equal("000,001-684,68500,68502-68599,686-999")
        }
    }

    describe("a ziprange with a single spec") {
        val zr = ZipRange.of("00100-99999")
        it("should properly collapse, if possible") {
            zr.normalize().spec.should.be.equal("001-999")
        }
    }
})