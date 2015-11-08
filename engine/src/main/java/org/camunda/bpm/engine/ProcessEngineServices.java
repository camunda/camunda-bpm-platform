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
package org.camunda.bpm.engine;


/**
 * <p>Base interface providing access to the process engine's
 * public API services.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface ProcessEngineServices {

  /**
   * Returns the process engine's {@link RuntimeService}.
   *
   * @return the {@link RuntimeService} object.
   */
  RuntimeService getRuntimeService();

  /**
   * Returns the process engine's {@link RepositoryService}.
   *
   * @return the {@link RepositoryService} object.
   */
  RepositoryService getRepositoryService();

  /**
   * Returns the process engine's {@link FormService}.
   *
   * @return the {@link FormService} object.
   */
  FormService getFormService();

  /**
   * Returns the process engine's {@link TaskService}.
   *
   * @return the {@link TaskService} object.
   */
  TaskService getTaskService();

  /**
   * Returns the process engine's {@link HistoryService}.
   *
   * @return the {@link HistoryService} object.
   */
  HistoryService getHistoryService();

  /**
   * Returns the process engine's {@link IdentityService}.
   *
   * @return the {@link IdentityService} object.
   */
  IdentityService getIdentityService();

  /**
   * Returns the process engine's {@link ManagementService}.
   *
   * @return the {@link ManagementService} object.
   */
  ManagementService getManagementService();

  /**
   * Returns the process engine's {@link AuthorizationService}.
   *
   * @return the {@link AuthorizationService} object.
   */
  AuthorizationService getAuthorizationService();

  /**
   * Returns the engine's {@link CaseService}.
   *
   * @return the {@link CaseService} object.
   *
   */
  CaseService getCaseService();

  /**
   * Returns the engine's {@link FilterService}.
   *
   * @return the {@link FilterService} object.
   *
   */
  FilterService getFilterService();

  /**
   * Returns the engine's {@link ExternalTaskService}.
   *
   * @return the {@link ExternalTaskService} object.
   */
  ExternalTaskService getExternalTaskService();

  /**
   * Returns the engine's {@link DecisionService}.
   *
   * @return the {@link DecisionService} object.
   */
  DecisionService getDecisionService();

}
