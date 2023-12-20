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
package org.camunda.bpm.engine.impl.history;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * Interface for Commands that synchronously modify multiple entities in one operation.
 * The methods of this interface take care of producing operation log entries based on the
 * {@link ProcessEngineConfigurationImpl#getLogEntriesPerSyncOperationLimit() logEntriesPerSyncOperationLimit} property.
 */
public interface SynchronousOperationLogProducer<T> {

  Long SUMMARY_LOG = 1L;
  Long UNLIMITED_LOG = -1L;

  /**
   * Returns a map containing a list of changed properties for every result of the operation.
   * Used to produce an operation log entry per entry contained in the returned map.
   */
  Map<T, List<PropertyChange>> getPropChangesForOperation(List<T> results);

  /**
   * Returns a list of changed properties summarizing the whole operation involving multiple entities.
   */
  List<PropertyChange> getSummarizingPropChangesForOperation(List<T> results);

  /**
   * Calls the code that produces the operation log. Usually <code>commandContext.getOperationLogManager().log...</code>
   *
   * The implementation must be capable of producing a single, summarizing operation log that contain information about an operation
   * spanning affecting multiple entities as well as producing a single, detailed operation log containing information about a single
   * affected entity. This method is called by the {@link SynchronousOperationLogProducer#produceOperationLog(CommandContext, List) produceOperationLog}
   * method.
   *
   * @param commandContext the current command context
   * @param result An object resulting from the operation for which this method produces the operation log. In case the operation produced
   * multiple objects, depending on the implementation a representative object from the list of results or null can be passed.
   * @param propChanges property changes to be attached to the operation log
   * @param isSummary indicates whether the implementation should produce a summary log or a detailed log
   */
  void createOperationLogEntry(CommandContext commandContext, T result, List<PropertyChange> propChanges, boolean isSummary);

  /**
   * The implementing command can call this method to produce the operation log entries for the current operation.
   */
  default void produceOperationLog(CommandContext commandContext, List<T> results) {
    if(results == null || results.isEmpty()) {
      return;
    }

    long logEntriesPerSyncOperationLimit = commandContext.getProcessEngineConfiguration()
        .getLogEntriesPerSyncOperationLimit();
    if(logEntriesPerSyncOperationLimit == SUMMARY_LOG && results.size() > 1) {
      // create summary from multi-result operation
      List<PropertyChange> propChangesForOperation = getSummarizingPropChangesForOperation(results);
      if(propChangesForOperation == null) {
        // convert null return value to empty list
        propChangesForOperation = Collections.singletonList(PropertyChange.EMPTY_CHANGE);
      }
      // use first result as representative for summarized operation log entry
      createOperationLogEntry(commandContext, results.get(0), propChangesForOperation, true);
    } else {
      // create detailed log for each operation result
      Map<T, List<PropertyChange>> propChangesForOperation = getPropChangesForOperation(results);
      if(propChangesForOperation == null ) {
        // create a map with empty result lists for each result item
        propChangesForOperation = results.stream().collect(Collectors.toMap(Function.identity(), (result) -> Collections.singletonList(PropertyChange.EMPTY_CHANGE)));
      }
      if (logEntriesPerSyncOperationLimit != UNLIMITED_LOG && logEntriesPerSyncOperationLimit < propChangesForOperation.size()) {
        throw new ProcessEngineException(
            "Maximum number of operation log entries for operation type synchronous APIs reached. Configured limit is "
                + logEntriesPerSyncOperationLimit + " but " + propChangesForOperation.size() + " entities were affected by API call.");
      } else {
        // produce one operation log per affected entity
        for (Entry<T, List<PropertyChange>> propChanges : propChangesForOperation.entrySet()) {
          createOperationLogEntry(commandContext, propChanges.getKey(), propChanges.getValue(), false);
        }
      }
    }
  }
}
