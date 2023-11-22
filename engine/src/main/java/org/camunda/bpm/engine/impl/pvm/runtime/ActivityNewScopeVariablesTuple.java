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
package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.Map;

/**
 * Tuple of an activity id and variables to be set into triggered scope.
 *
 */
public class ActivityNewScopeVariablesTuple {

  protected final String activityId;
  protected final Map<String, Object> variables;

  public ActivityNewScopeVariablesTuple(String activityId, Map<String, Object> variables) {
    this.activityId = activityId;
    this.variables = variables;
  }

  public String getActivityId() {
    return activityId;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }
}
