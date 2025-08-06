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
package org.camunda.bpm.container.impl.jboss.extension.handler;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.jboss.threads.EnhancedQueueExecutor;
import org.jboss.threads.EnhancedQueueExecutor.Builder;

public class EnhancedQueueExecutorService implements Service<EnhancedQueueExecutor> {

  protected Builder executorBuilder;
  protected EnhancedQueueExecutor executor;

  public EnhancedQueueExecutorService(Builder executorBuilder) {
    this.executorBuilder = executorBuilder;
  }

  @Override
  public void start(StartContext context) {
    executor = executorBuilder.build();
  }

  @Override
  public void stop(StopContext context) {
    if (executor != null) {
      executor.shutdown();
    }
  }

  @Override
  public EnhancedQueueExecutor getValue() throws IllegalStateException, IllegalArgumentException {
    return executor;
  }
}