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
package org.camunda.bpm.engine.delegate;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * Listener interface implemented by user code which wants to be notified
 * when a state transition happens on a {@link CaseExecution}.
 *
 * <p>The following state transition are supported on a {@link CaseInstance}:
 * <ul>
 * <li>{@link #CREATE}</li>
 * <li>{@link #COMPLETE}</li>
 * <li>{@link #TERMINATE}</li>
 * <li>{@link #SUSPEND}</li>
 * <li>{@link #RE_ACTIVATE}</li>
 * <li>{@link #CLOSE}</li>
 * </ul>
 * </p>
 *
 * <p>And on a {@link CaseExecution} which is not a {@link CaseInstance} and which
 * is associated with a {@link Task} or a {@link Stage} the following state transition
 * are supported:
 * <ul>
 * <li>{@link #CREATE}</li>
 * <li>{@link #ENABLE}</li>
 * <li>{@link #DISABLE}</li>
 * <li>{@link #RE_ENABLE}</li>
 * <li>{@link #START}</li>
 * <li>{@link #MANUAL_START}</li>
 * <li>{@link #COMPLETE}</li>
 * <li>{@link #TERMINATE}</li>
 * <li>{@link #EXIT}</li>
 * <li>{@link #SUSPEND}</li>
 * <li>{@link #RESUME}</li>
 * <li>{@link #PARENT_SUSPEND}</li>
 * <li>{@link #PARENT_RESUME}</li>
 * </ul>
 * </p>
 *
 * @author Roman Smirnov
 *
 */
public interface CaseExecutionListener extends DelegateListener<DelegateCaseExecution> {

  String CREATE = "create";
  String ENABLE = "enable";
  String DISABLE = "disable";
  String RE_ENABLE = "reenable";
  String START = "start";
  String MANUAL_START = "manualStart";
  String COMPLETE = "complete";
  String RE_ACTIVATE = "reactivate";
  String TERMINATE = "terminate";
  String EXIT = "exit";
  String PARENT_TERMINATE = "parentTerminate";
  String SUSPEND = "suspend";
  String RESUME = "resume";
  String PARENT_SUSPEND = "parentSuspend";
  String PARENT_RESUME = "parentResume";
  String CLOSE = "close";
  String OCCUR = "occur";

  void notify(DelegateCaseExecution caseExecution) throws Exception;

}
