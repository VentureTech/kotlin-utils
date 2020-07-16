package net.proteusframework.kotlinutils.std.samples

import net.proteusframework.kotlinutils.std.withResources
import java.io.File


fun withResourcesSample() {
    withResources {
        val file1 = File.createTempFile("foo", "ext")
        val file2 = File.createTempFile("boo", "ext")

        // Caller use to record that you are using this resource so it will be
        val writer1 = file1.bufferedWriter().use()
        val writer2 = file2.bufferedWriter().use()
        writer1.write("foo")
        writer2.write("boo")

        doLast {
            file1.delete()
            file2.delete()
        }
    }
}