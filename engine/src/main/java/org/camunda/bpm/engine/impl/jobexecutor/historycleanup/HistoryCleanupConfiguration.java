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

package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class HistoryCleanupConfiguration {

  protected final Integer maxNumberOfRetries;

  private HistoryCleanupConfiguration(Integer maxNumberOfRetries) {
    this.maxNumberOfRetries = maxNumberOfRetries;
  }

  public static HistoryCleanupConfiguration of(CommandContext context) {
    ProcessEngineConfiguration config = context.getProcessEngineConfiguration();

    final int numberOfRetries = config.getHistoryCleanupDefaultNumberOfRetries();
    return of(numberOfRetries);
  }

  public static HistoryCleanupConfiguration of(Integer maxNumberOfRetries) {
    return new HistoryCleanupConfiguration(maxNumberOfRetries);
  }

  public int getMaxNumberOfRetries() {
    return maxNumberOfRetries;
  }

  public boolean isEmpty() {
    return maxNumberOfRetries == null;
  }

}