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

package org.camunda.bpm.engine.history;

import java.util.Objects;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class HistoryCleanupRetryConfiguration {

  private static final HistoryCleanupRetryConfiguration EMPTY = new HistoryCleanupRetryConfiguration(null);

  protected final Integer maxNumberOfRetries;

  private HistoryCleanupRetryConfiguration(Integer maxNumberOfRetries) {
    this.maxNumberOfRetries = maxNumberOfRetries;
  }

  public static HistoryCleanupRetryConfiguration empty() {
    return EMPTY;
  }

  public static HistoryCleanupRetryConfiguration of(CommandContext context) {
    ProcessEngineConfiguration config = context.getProcessEngineConfiguration();

    final int numberOfRetries = config.getHistoryCleanupDefaultNumberOfRetries();
    return of(numberOfRetries);
  }

  public static HistoryCleanupRetryConfiguration of(Integer maxNumberOfRetries) {
    return new HistoryCleanupRetryConfiguration(maxNumberOfRetries);
  }

  public static HistoryCleanupRetryConfiguration noRetries() {
    return new HistoryCleanupRetryConfiguration(0);
  }

  public int getMaxNumberOfRetries() {
    return maxNumberOfRetries;
  }

  public boolean isEmpty() {
    return Objects.equals(maxNumberOfRetries, EMPTY.maxNumberOfRetries);
  }

}