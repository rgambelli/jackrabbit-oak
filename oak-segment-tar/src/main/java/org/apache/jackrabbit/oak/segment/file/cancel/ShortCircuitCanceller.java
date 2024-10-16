/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.segment.file.cancel;

import java.util.concurrent.atomic.AtomicReference;

class ShortCircuitCanceller extends Canceller {

    private final AtomicReference<Cancellation> cancellation = new AtomicReference<>();

    private final Canceller parent;

    ShortCircuitCanceller(Canceller parent) {
        this.parent = parent;
    }

    @Override
    public boolean isCancelable() {
        return parent.isCancelable();
    }

    @Override
    public Cancellation check() {
        return cancellation.updateAndGet(prev -> prev != null && prev.isCancelled() ? prev : parent.check());
    }
}
