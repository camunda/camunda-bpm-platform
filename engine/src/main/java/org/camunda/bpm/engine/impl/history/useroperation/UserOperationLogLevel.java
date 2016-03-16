/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.history.useroperation;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;

/**
 * <p>The user-operation log level controls what kind of data is logged to the user-operation history database.
 * </p>
 * @author Thomas Skjolberg
 * @since 7.5
 */
public interface UserOperationLogLevel {

  static UserOperationLogLevel USER_OPERATION_LOG_LEVEL_NONE = new UserOperationLogLevelNone();
  static UserOperationLogLevel USER_OPERATION_LOG_LEVEL_FULL = new UserOperationLogLevelFull();

  /** An unique id identifying the history level.
   * The id is used internally to uniquely identify the history level and also stored in the database.
   */
  int getId();

  /** An unique name identifying the user-operation log level.
   * The name of the user-operation log level can be used when configuring the process engine.
   * @see {@link ProcessEngineConfiguration#setHistory(String)}
   */
  String getName();

  /**
   * Returns true if a given user-operation log entry should be produced.
   * @param eventType the type of the user-operation log entry which is about to be produced
   */
  boolean isUserOperationLogEntryProduced(String eventType);

}
