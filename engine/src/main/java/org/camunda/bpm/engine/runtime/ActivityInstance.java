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
package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.RuntimeService;

/**
 * <p>An activity instance represents an instance of an activity.</p>
 *
 * <p>For documentation, see {@link RuntimeService#getActivityInstance(String)}</p>
 *
 * @author Daniel Meyer
 *
 */
public interface ActivityInstance extends ProcessElementInstance {

  /** the id of the activity */
  String getActivityId();

  /** the name of the activity */
  String getActivityName();

  /**
   * Type of the activity, corresponds to BPMN element name in XML (e.g. 'userTask').
   * The type of the Root activity instance (the one corresponding to the process instance will be 'processDefinition'.
   */
  public String getActivityType();

  /** Returns the child activity instances.
   * Returns an empty list if there are no child instances */
  ActivityInstance[] getChildActivityInstances();

  /** Returns the child transition instances.
   * Returns an empty list if there are no child transition instances */
  TransitionInstance[] getChildTransitionInstances();

  /** the list of executions that are currently waiting in this activity instance */
  String[] getExecutionIds();

  /**
   * all descendant (children, grandchildren, etc.) activity instances that are instances of the supplied activity
   */
  ActivityInstance[] getActivityInstances(String activityId);

  /**
   * all descendant (children, grandchildren, etc.) transition instances that are leaving or entering the supplied activity
   */
  TransitionInstance[] getTransitionInstances(String activityId);

  /** the ids of currently open incidents */
  String[] getIncidentIds();

  /** the list of currently open incidents */
  Incident[] getIncidents();

}
