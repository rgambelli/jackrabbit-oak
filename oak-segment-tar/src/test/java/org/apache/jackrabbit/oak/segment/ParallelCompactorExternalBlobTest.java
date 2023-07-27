/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jackrabbit.oak.segment;

import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.GCNodeWriteMonitor;
import org.apache.jackrabbit.oak.segment.file.tar.GCGeneration;
import org.apache.jackrabbit.oak.spi.gc.GCMonitor;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.apache.jackrabbit.oak.segment.DefaultSegmentWriterBuilder.defaultSegmentWriterBuilder;

@RunWith(Parameterized.class)
public class ParallelCompactorExternalBlobTest extends AbstractCompactorExternalBlobTest {

    private final int concurrency;

    @Parameterized.Parameters
    public static List<Integer> concurrencyLevels() {
        return Arrays.asList(1, 2, 4, 8, 16);
    }

    public ParallelCompactorExternalBlobTest(int concurrency) {
        this.concurrency = concurrency;
    }

    @Override
    protected ParallelCompactor createCompactor(@NotNull FileStore fileStore, @NotNull GCGeneration generation) {
        SegmentWriter writer = defaultSegmentWriterBuilder("c")
                .withGeneration(generation)
                .withWriterPool(SegmentBufferWriterPool.PoolType.THREAD_SPECIFIC)
                .build(fileStore);

        return new ParallelCompactor(
                GCMonitor.EMPTY,
                fileStore.getReader(),
                writer,
                fileStore.getBlobStore(),
                GCNodeWriteMonitor.EMPTY,
                concurrency);
    }
}
