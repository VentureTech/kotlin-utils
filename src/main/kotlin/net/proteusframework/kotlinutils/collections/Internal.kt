/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.proteusframework.kotlinutils.collections

import java.util.*
import java.util.Collections.emptyIterator

// From guava
internal class ConcatenatedIterator<T>(metaIterator: MutableIterator<Iterator<T>>) : MutableIterator<T> {
    /* The last iterator to return an element.  Calls to remove() go to this iterator. */
    private var toRemove: Iterator<T>? = null

    /* The iterator currently returning elements. */
    private var iterator: Iterator<T>? = null

    /*
     * We track the "meta iterators," the iterators-of-iterators, below.  Usually, topMetaIterator
     * is the only one in use, but if we encounter nested concatenations, we start a deque of
     * meta-iterators rather than letting the nesting get arbitrarily deep.  This keeps each
     * operation O(1).
     */

    private var topMetaIterator: MutableIterator<Iterator<T>>? = null

    // Only becomes nonnull if we encounter nested concatenations.
    private var metaIterators: Deque<MutableIterator<Iterator<T>>>? = null

    init {
        iterator = emptyIterator<T>()
        topMetaIterator = checkNotNull(metaIterator)
    }

    // Returns a nonempty meta-iterator or, if all meta-iterators are empty, null.
    private fun getTopMetaIterator(): MutableIterator<Iterator<T>>? {
        while (topMetaIterator == null || !topMetaIterator!!.hasNext()) {
            if (metaIterators != null && !metaIterators!!.isEmpty()) {
                topMetaIterator = metaIterators!!.removeFirst()
            } else {
                return null
            }
        }
        return topMetaIterator
    }

    override fun hasNext(): Boolean {
        while (!checkNotNull<Iterator<T>>(iterator).hasNext()) {
            // this weird checkNotNull positioning appears required by our tests, which expect
            // both hasNext and next to throw NPE if an input iterator is null.

            topMetaIterator = getTopMetaIterator()
            val localTopMetaIterator = topMetaIterator
            if (localTopMetaIterator == null) {
                return false
            }

            iterator = localTopMetaIterator.next()

            if (iterator is ConcatenatedIterator<*>) {
                // Instead of taking linear time in the number of nested concatenations, unpack
                // them into the queue
                val topConcat = iterator as ConcatenatedIterator<T>?
                iterator = topConcat!!.iterator

                // topConcat.topMetaIterator, then topConcat.metaIterators, then this.topMetaIterator,
                // then this.metaIterators

                if (this.metaIterators == null) {
                    this.metaIterators = ArrayDeque()
                }
                this.metaIterators!!.addFirst(this.topMetaIterator)
                if (topConcat.metaIterators != null) {
                    while (!topConcat.metaIterators!!.isEmpty()) {
                        this.metaIterators!!.addFirst(topConcat.metaIterators!!.removeLast())
                    }
                }
                this.topMetaIterator = topConcat.topMetaIterator
            }
        }
        return true
    }

    override fun next(): T {
        if (hasNext()) {
            toRemove = iterator
            return iterator!!.next()
        } else {
            throw NoSuchElementException()
        }
    }

    override fun remove() {
        val localToRemove = toRemove
        if (localToRemove != null && localToRemove is MutableIterator<*>)
            localToRemove.remove()
        toRemove = null
    }
}

/** Used to avoid http://bugs.sun.com/view_bug.do?bug_id=6558557  */
internal fun <T> cast(iterator: Iterator<T>): ListIterator<T> {
    return iterator as ListIterator<T>
}