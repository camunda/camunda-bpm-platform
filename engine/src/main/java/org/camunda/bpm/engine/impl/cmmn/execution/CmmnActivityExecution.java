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
package org.camunda.bpm.engine.impl.cmmn.execution;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.Stage;

/**
 * @author Roman Smirnov
 *
 */
public interface CmmnActivityExecution extends DelegateCaseExecution {

  /**
   * <p>Returns the {@link CmmnActivity activity} which is associated with
   * <code>this</code> case execution.
   *
   * @return the associated {@link CmmnActivity activity}
   */
  CmmnActivity getActivity();

  /**
   * <p>Creates new child case executions for each given {@link CmmnActivity}.</p>
   *
   * <p>Afterwards each created case executions will be executed (ie. the create
   * listener will be notified etc.).</p>
   *
   * <p>According to the CMMN 1.0 specification:<br>
   * This method can be called when <code>this</code> case execution (which
   * represents a {@link Stage}) transitions to <code>ACTIVE</code> state.
   * The passed collection of {@link CmmnActivity activities} are the planned
   * items that should be executed in this {@link Stage}. So that for each
   * given {@link CmmnActivity} a new case execution will be instantiated.
   * Furthermore for each created child execution there happens a transition
   * to the initial state <code>AVAILABLE</code>.
   * </p>
   *
   * @param activities a collection of {@link CmmnActivity activities} of planned itmes
   *                   to execute inside <code>this</code> case execution
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>ACTIVE</code>.
   *
   */
  void createChildExecutions(List<CmmnActivity> activities);

  /**
   * <p>Transitions to <p>ENABLED</p> state.</p>
   *
   * <p><code>This</code> case execution must be in <code>AVAILABLE</code>
   * state to be able to do this transition.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>AVAILABLE</code>.
   */
  void enable();

  /**
   * <p>Transitions to <p>DISABLED</p> state.</p>
   *
   * <p><code>This</code> case execution must be in <code>ENABLED</code>
   * state to be able to do this transition.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>ENABLED</code>.
   */
  void disable();

  /**
   * <p>Transitions to <p>ENABLED</p> state.</p>
   *
   * <p><code>This</code> case execution must be in <code>DISABLED</code>
   * state to be able to do this transition.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>DISABLED</code>.
   */
  void reenable();

  /**
   * <p>Transitions to <p>ACTIVE</p> state.</p>
   *
   * <p><code>This</code> case execution must be in <code>ENABLED</code>
   * state to be able to do this transition.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>ENABLED</code>.
   */
  void manualStart();

  /**
   * <p>Transitions to <p>ACTIVE</p> state.</p>
   *
   * <p><code>This</code> case execution must be in <code>AVAILABLE</code>
   * state to be able to do this transition.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>AVAILABLE</code>.
   */
  void start();

  /**
   * <p>Transitions to <p>COMPLETE</p> state.</p>
   *
   * <p><code>This</code> case execution must be in <code>ACTIVE</code>
   * state to be able to do this transition.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>ACTIVE</code>.
   */
  void complete();

}
