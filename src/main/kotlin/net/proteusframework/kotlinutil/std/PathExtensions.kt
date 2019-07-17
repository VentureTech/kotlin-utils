/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package net.proteusframework.kotlinutil.std

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

val RecursiveDeleter = object : SimpleFileVisitor<Path>() {
	override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
		return super.postVisitDirectory(dir, exc).also {
			Files.delete(dir);
		}
	}

	override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
		return super.visitFile(file, attrs).also {
			Files.delete(file);
		}
	}
}

/**
 * Delete path recursively return this.
 */
fun Path.deleteRecursively(): Path = Files.walkFileTree(this, RecursiveDeleter)