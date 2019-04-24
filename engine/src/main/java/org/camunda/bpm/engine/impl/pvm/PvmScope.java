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
package org.camunda.bpm.engine.impl.pvm;

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public interface PvmScope extends PvmProcessElement {

  /**
   * Indicates whether this is a local scope for variables and events
   * if true, there will _always_ be a scope execution created for it.
   *<p>
   * Note: the fact that this is a scope does not mean that it is also a
   * {@link #isSubProcessScope() sub process scope.}
   *
   * @returns true if this activity is a scope
   */
  boolean isScope();

  /** Indicates whether this scope is a sub process scope.
   * A sub process scope is a scope which contains "normal flow".Scopes which are flow scopes but not sub process scopes:
   * <ul>
   * <li>a multi instance body scope</li>
   * <li>leaf scope activities which are pure event scopes (Example: User task with attached boundary event)</li>
   * </ul>
   *
   * @return true if this is a sub process scope
   */
  boolean isSubProcessScope();

  /**
   * The event scope for an activity is the scope in which the activity listens for events.
   * This may or may not be the {@link #getFlowScope() flow scope.}.
   * Consider: boundary events have a different event scope than flow scope.
   *<p>
   * The event scope is always a {@link #isScope() scope}.
   *
   * @return the event scope of the activity
   */
  PvmScope getEventScope();

  /**
   * The flow scope of the activity. The scope in which the activity itself is executed.
   *<p>
   * Note: in order to ensure backwards compatible behavior,  a flow scope is not necessarily
   * a {@link #isScope() a scope}. Example: event sub processes.
   */
  ScopeImpl getFlowScope();

  /**
   * The "level of subprocess scope" as defined in bpmn: this is the subprocess
   * containing the activity. Usually this is the same as the flow scope, instead if
   * the activity is multi instance: in that case the activity is nested inside a
   * mutli instance body but "at the same level of subprocess" as other activities which
   * are siblings to the mi-body.
   *
   * @return the level of subprocess scope as defined in bpmn
   */
  PvmScope getLevelOfSubprocessScope();

  /**
   * Returns the flow activities of this scope. This is the list of activities for which this scope is
   * the {@link PvmActivity#getFlowScope() flow scope}.
   *
   * @return the list of flow activities for this scope.
   */
  List<? extends PvmActivity> getActivities();

  /**
   * Recursively finds a flow activity. This is an activitiy which is in the hierarchy of flow activities.
   *
   * @param activityId the id of the activity to find.
   * @return the activity or null
   */
  PvmActivity findActivity(String activityId);

  /**
   * Finds an activity at the same level of subprocess.
   *
   * @param activityId the id of the activity to find.
   * @return the activity or null
   */
  PvmActivity findActivityAtLevelOfSubprocess(String activityId);

  /**
   * Recursively finds a transition.
   * @param transitionId the transiton to find
   * @return the transition or null
   */
  TransitionImpl findTransition(String transitionId);

}
