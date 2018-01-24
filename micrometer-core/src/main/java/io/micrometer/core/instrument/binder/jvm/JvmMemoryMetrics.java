/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.jvm;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

import static java.util.Collections.emptyList;

/**
 * Record metrics that report utilization of various memory and buffer pools.
 *
 * @author Jon Schneider
 * @see MemoryPoolMXBean
 * @see BufferPoolMXBean
 */
@NonNullApi
@NonNullFields
public class JvmMemoryMetrics implements MeterBinder {
    private final Iterable<Tag> tags;

    public JvmMemoryMetrics() {
        this(emptyList());
    }

    public JvmMemoryMetrics(Iterable<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (BufferPoolMXBean bufferPoolBean : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            Iterable<Tag> tagsWithId = Tags.concat(tags, "id", bufferPoolBean.getName());

            Gauge.builder("jvm.buffer.count", bufferPoolBean, BufferPoolMXBean::getCount)
                .tags(tagsWithId)
                .description("An estimate of the number of buffers in the pool")
                .register(registry);

            Gauge.builder("jvm.buffer.memory.used", bufferPoolBean, BufferPoolMXBean::getMemoryUsed)
                .tags(tagsWithId)
                .description("An estimate of the memory that the Java virtual machine is using for this buffer pool")
                .baseUnit("bytes")
                .register(registry);

            Gauge.builder("jvm.buffer.total.capacity", bufferPoolBean, BufferPoolMXBean::getTotalCapacity)
                .tags(tagsWithId)
                .description("An estimate of the total capacity of the buffers in this pool")
                .baseUnit("bytes")
                .register(registry);
        }

        for (MemoryPoolMXBean memoryPoolBean : ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class)) {
            String area = MemoryType.HEAP.equals(memoryPoolBean.getType()) ? "heap" : "nonheap";
            Iterable<Tag> tagsWithId = Tags.concat(tags, "id", memoryPoolBean.getName(), "area", area);

            Gauge.builder("jvm.memory.used", memoryPoolBean, (mem) -> mem.getUsage().getUsed())
                .tags(tagsWithId)
                .description("The amount of used memory")
                .baseUnit("bytes")
                .register(registry);

            Gauge.builder("jvm.memory.committed", memoryPoolBean, (mem) -> mem.getUsage().getCommitted())
                .tags(tagsWithId)
                .description("The amount of memory in bytes that is committed for  the Java virtual machine to use")
                .baseUnit("bytes")
                .register(registry);

            Gauge.builder("jvm.memory.max", memoryPoolBean, (mem) -> mem.getUsage().getMax())
                .tags(tagsWithId)
                .description("The maximum amount of memory in bytes that can be used for memory management")
                .baseUnit("bytes")
                .register(registry);
        }
    }
}
