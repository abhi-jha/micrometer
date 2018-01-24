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
package io.micrometer.core.instrument.cumulative;

import io.micrometer.core.instrument.AbstractMeter;
import io.micrometer.core.instrument.Counter;

import java.util.concurrent.atomic.DoubleAdder;

public class CumulativeCounter extends AbstractMeter implements Counter {
    private final DoubleAdder value;

    /**
     * Create a new instance.
     */
    public CumulativeCounter(Id id) {
        super(id);
        this.value = new DoubleAdder();
    }

    @Override
    public void increment(double amount) {
        value.add(amount);
    }

    @Override
    public double count() {
        return value.sum();
    }
}
