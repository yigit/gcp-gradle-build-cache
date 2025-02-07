/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package androidx.build.gradle.gcpbuildcache

import java.io.File
import java.io.InputStream
import java.nio.file.Files

/**
 * An implementation of the [StorageService] that is backed by a file system.
 */
internal class FileSystemStorageService(
    override val projectId: String,
    override val bucketName: String,
    override val isPush: Boolean,
    override val isEnabled: Boolean
) : StorageService {

    private val location = Files.createTempDirectory("tmp$projectId$bucketName").toFile()

    override fun load(cacheKey: String): InputStream? {
        if (!isEnabled) {
            return null
        }

        val file = File(location, cacheKey)
        return if (file.exists() && file.isFile) {
            file.inputStream()
        } else {
            null
        }
    }

    override fun store(cacheKey: String, contents: ByteArray): Boolean {
        if (!isEnabled) {
            return false
        }

        if (!isPush) {
            return false
        }

        val file = File(location, cacheKey)
        val output = file.outputStream()
        output.use {
            output.write(contents)
        }
        return true
    }

    override fun delete(cacheKey: String): Boolean {
        if (!isEnabled) {
            return false
        }

        if (!isPush) {
            return false
        }

        val file = File(location, cacheKey)
        return file.delete()
    }

    override fun close() {
        location.deleteRecursively()
    }

    companion object {
        private fun File.deleteRecursively() {
            val files = listFiles()
            for (file in files) {
                if (file.isFile) {
                    file.delete()
                }
                if (file.isDirectory) {
                    file.deleteRecursively()
                }
            }
            delete()
        }
    }

}
