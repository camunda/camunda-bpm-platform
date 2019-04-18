/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * MPSC Condition implementation.
 * <p>
 * Implementation Notes:
 * <ul>
 * <li>{@link #await(long)} may spuriously return before the deadline is reached.</li>
 * <li>if {@link #signal()} is called before the consumer thread calls {@link #await(long)},
 * the next call to {@link #await(long)} returns immediately.</li>
 * </ul>
 */
public class SingleConsumerCondition {

  // note: making this private & final because it cannot be subclassed
  // and replaced in a meaningful way without breaking the implementation
  private final Thread consumer;

  public SingleConsumerCondition(Thread consumer) {
    if (consumer == null) {
      throw new IllegalArgumentException("Consumer thread cannot be null");
    }

    this.consumer = consumer;
  }

  public void signal() {
    LockSupport.unpark(consumer);
  }

  public void await(long millis) {
    if (Thread.currentThread() != consumer) {
      throw new RuntimeException("Wrong usage of SingleConsumerCondition: can only await in consumer thread.");
    }

    // NOTE: may spuriously return before deadline
    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(millis));
  }

}
