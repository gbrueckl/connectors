/*
 * Copyright (2020-present) The Delta Lake Project Authors.
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

package io.delta.standalone.internal.storage

import scala.collection.JavaConverters._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

/**
 * LogStore implementation for Azure.
 *
 * We assume the following from Azure's [[org.apache.hadoop.fs.FileSystem]] implementations:
 * - Rename without overwrite is atomic.
 * - List-after-write is consistent.
 *
 * Regarding file creation, this implementation:
 * - Uses atomic rename when overwrite is false; if the destination file exists or the rename
 *   fails, throws an exception.
 * - Uses create-with-overwrite when overwrite is true. This does not make the file atomically
 *   visible and therefore the caller must handle partial files.
 */
private[internal] class AzureLogStore(override val initHadoopConf: Configuration)
  extends HadoopFileSystemLogStore(initHadoopConf) {

  override def write(
      path: Path,
      actions: java.util.Iterator[String],
      overwrite: java.lang.Boolean,
      hadoopConf: Configuration): Unit = {
    writeWithRename(path, actions.asScala, overwrite, hadoopConf)
  }

  override def isPartialWriteVisible(path: Path, hadoopConf: Configuration): java.lang.Boolean = {
    true
  }
}
