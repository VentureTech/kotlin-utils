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

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import net.proteusframework.kotlinutils.json.JSONFactory
import net.proteusframework.kotlinutils.json.JSONProducer
import java.util.*

/**
 * Closed date range with optional start/end properties.
 * One of the two properties must be specified.
 * @param dstart the start date.
 * @param dend the end date.
 * @author Russ Tennant (russ@proteus.co)
 */
class DateRange(var dstart: Date?, val dend: Date?)
    : ClosedRange<Date>, JSONProducer {
    companion object : JSONFactory<DateRange> {
        val adapter: JsonAdapter<DateRange> = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
            .adapter(DateRange::class.java)
        private val FIRST_DAY: Date

        @JvmStatic
        override fun fromJSON(json: String): DateRange? = adapter.fromJson(json)

        init {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            cal.set(1, 0, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            FIRST_DAY = cal.time
        }
    }

    init {
        assert(((dstart != null) || (dend != null))) { "Either start or end must be non-null." }
    }

    /**
     * Checks whether the specified [value] belongs to the range.
     */
    override operator fun contains(value: Date): Boolean = when {
        dend == null   -> value >= dstart
        dstart == null -> value <= dend
        else           -> value >= dstart && value <= dend
    }

    override fun isEmpty(): Boolean = false

    override val start: Date
        get() = dstart ?: FIRST_DAY

    override val endInclusive: Date
        get() = dend ?: Date(Long.MAX_VALUE)

    override fun toJSON(): String = adapter.toJson(this)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (!(other is DateRange)) return false
        return dstart == other.dstart && dend == other.dend
    }

    override fun hashCode(): Int = (dstart?.hashCode() ?: 0 * 31) + (dend?.hashCode() ?: 0)

}