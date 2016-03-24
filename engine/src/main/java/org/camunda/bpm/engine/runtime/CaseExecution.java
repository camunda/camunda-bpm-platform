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
package org.camunda.bpm.engine.runtime;

/**
 * <p>Represent a planned item in a case instance.</p>
 *
 * <p>Note that a {@link CaseInstance} also is an case execution.</p>
 *
 * @author Roman Smirnov
 *
 */
public interface CaseExecution {

  /**
   * <p>The unique identifier of the case execution.</p>
   */
  String getId();

  /**
   * <p>Id of the root of the case execution tree representing the case instance.</p>
   *
   * <p>It is the same as {@link #getId()} if this case execution is the case instance.</p>
   */
  String getCaseInstanceId();

  /**
   * <p>The id of the case definition of the case execution.</p>
   */
  String getCaseDefinitionId();

  /**
   * <p>The id of the activity associated with <code>this</code> case execution.</p>
   */
  String getActivityId();

  /**
   * <p>The name of the activity associated with <code>this</code> case execution.</p>
   */
  String getActivityName();

  /**
   * <p>The type of the activity associated with <code>this</code> case execution.</p>
   */
  String getActivityType();

  /**
   * <p>The description of the activity associated with <code>this</code> case execution.</p>
   */
  String getActivityDescription();

  /**
   * <p>The id of the parent of <code>this</code> case execution.</p>
   */
  String getParentId();

  /**
   * <p>Returns <code>true</code> if the case execution is required.</p>
   */
  boolean isRequired();

  /**
   * <p>Returns <code>true</code> if the case execution is available.</p>
   */
  boolean isAvailable();

  /**
   * <p>Returns <code>true</code> if the case execution is active.</p>
   */
  boolean isActive();

  /**
   * <p>Returns <code>true</code> if the case execution is enabled.</p>
   *
   * <p><strong>Note:</strong> If this case execution is the case execution, it will
   * return always <code>false</code>.</p>
   *
   */
  boolean isEnabled();

  /**
   * <p>Returns <code>true</code> if the case execution is disabled.</p>
   *
   * <p><strong>Note:</strong> If this case execution is the case instance, it will
   * return always <code>false</code>.</p>
   */
  boolean isDisabled();

  /**
   * <p>Returns <code>true</code> if the case execution is terminated.</p>
   */
  boolean isTerminated();

  /**
   * The id of the tenant this case execution belongs to. Can be <code>null</code>
   * if the case execution belongs to no single tenant.
   */
  String getTenantId();

}
