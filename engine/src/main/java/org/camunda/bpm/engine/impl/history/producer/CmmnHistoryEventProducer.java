/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * <p>The producer for CMMN history events. The history event producer is
 * responsible for extracting data from the runtime structures
 * (Executions, Tasks, ...) and adding the data to a {@link HistoryEvent}.
 *
 * @author Sebastian Menski
 */
public interface CmmnHistoryEventProducer {

  /**
   * Creates the history event fired when a case instance is <strong>created</strong>.
   *
   * @param caseExecution the current case execution
   * @return the created history event
   */
  public HistoryEvent createCaseInstanceCreateEvt(DelegateCaseExecution caseExecution);

  /**
   * Creates the history event fired when a case instance is <strong>updated</strong>.
   *
   * @param caseExecution the current case execution
   * @return the created history event
   */
  public HistoryEvent createCaseInstanceUpdateEvt(DelegateCaseExecution caseExecution);

  /**
   * Creates the history event fired when a case instance is <strong>closed</strong>.
   *
   * @param caseExecution the current case execution
   * @return the created history event
   */
  public HistoryEvent createCaseInstanceCloseEvt(DelegateCaseExecution caseExecution);

  /**
   * Creates the history event fired when a case activity instance is <strong>created</strong>.
   *
   * @param caseExecution the current case execution
   * @return the created history event
   */
  public HistoryEvent createCaseActivityInstanceCreateEvt(DelegateCaseExecution caseExecution);

  /**
   * Creates the history event fired when a case activity instance is <strong>updated</strong>.
   *
   * @param caseExecution the current case execution
   * @return the created history event
   */
  public HistoryEvent createCaseActivityInstanceUpdateEvt(DelegateCaseExecution caseExecution);

  /**
   * Creates the history event fired when a case activity instance is <strong>ended</strong>.
   *
   * @param caseExecution the current case execution
   * @return the created history event
   */
  public HistoryEvent createCaseActivityInstanceEndEvt(DelegateCaseExecution caseExecution);

}
