@file:Suppress("UNUSED_PARAMETER")

package net.proteusframework.kotlinutils.std

class Resources(private val suppressThrowingException: Boolean = false) : AutoCloseable {
    private val resources = mutableListOf<AutoCloseable>()
    private val doLastBlocks = mutableListOf<(exception: Exception?) -> Unit>()

    @Deprecated(replaceWith = ReplaceWith("autoClose()"), message = "Replaced by autoClose")
    fun <T : AutoCloseable> T.use(): T {
        resources += this
        return this
    }

    /**
     * Register this resource so that it is automatically closed
     * when this *Resources* instance is closed.
     * @sample net.proteusframework.kotlinutils.std.samples.withResourcesSample
     */
    fun <T : AutoCloseable> T.autoClose(): T {
        resources += this
        return this
    }

    /**
     * Register callbacks to execute after closing resources
     * with an optional exception in the case of closing a
     * resource yields an exception.
     *
     * Register callbacks should not throw an exception.
     */
    fun doLast(block: (exception: Exception?) -> Unit) {
        doLastBlocks += block
    }

    override fun close() {
        var exception: Exception? = null
        for (resource in resources.reversed()) {
            try {
                resource.close()
            } catch (closeException: Exception) {
                if (exception == null) {
                    exception = closeException
                } else {
                    exception.addSuppressed(closeException)
                }
            }
        }
        doLastBlocks.forEach { block ->
            block(exception)
        }
        if (exception != null && !suppressThrowingException) throw exception
    }
}


/**
 * Utility function to auto-close multiple resources.
 * @param suppressThrowingException flag that when set will suppress any exceptions thrown while calling
 * [AutoCloseable.close] on the resources registered with [Resources].
 * @sample net.proteusframework.kotlinutils.std.samples.withResourcesSample
 */
inline fun <T> withResources(suppressThrowingException: Boolean = false, block: Resources.() -> T): T = Resources().use(block)
