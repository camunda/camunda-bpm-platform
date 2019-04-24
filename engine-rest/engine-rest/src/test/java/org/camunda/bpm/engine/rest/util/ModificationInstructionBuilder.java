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
package org.camunda.bpm.engine.rest.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class ModificationInstructionBuilder {

  public ModificationInstructionBuilder(String type) {
    this.type = type;
  }

  protected Map<String, Object> variables;

  protected String type;
  protected String activityId;
  protected String activityInstanceId;
  protected String transitionInstanceId;
  protected String ancestorActivityInstanceId;
  protected String transitionId;
  protected boolean isFlagSet;
  protected boolean cancelCurrentActiveActivityInstances;

  public static ModificationInstructionBuilder cancellation() {
    return new ModificationInstructionBuilder("cancel");
  }

  public static ModificationInstructionBuilder startBefore() {
    return new ModificationInstructionBuilder("startBeforeActivity");
  }

  public static ModificationInstructionBuilder startAfter() {
    return new ModificationInstructionBuilder("startAfterActivity");
  }

  public static ModificationInstructionBuilder startTransition() {
    return new ModificationInstructionBuilder("startTransition");
  }

  public ModificationInstructionBuilder variables(Map<String, Object> variables) {
    this.variables = variables;
    return this;
  }

  public ModificationInstructionBuilder activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public ModificationInstructionBuilder activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public ModificationInstructionBuilder transitionInstanceId(String transitionInstanceId) {
    this.transitionInstanceId = transitionInstanceId;
    return this;
  }

  public ModificationInstructionBuilder ancestorActivityInstanceId(String ancestorActivityInstanceId) {
    this.ancestorActivityInstanceId = ancestorActivityInstanceId;
    return this;
  }

  public ModificationInstructionBuilder transitionId(String transitionId) {
    this.transitionId = transitionId;
    return this;
  }

  public ModificationInstructionBuilder cancelCurrentActiveActivityInstances(
      boolean cancelCurrentActiveActivityInstances) {
    isFlagSet = true;
    this.cancelCurrentActiveActivityInstances = cancelCurrentActiveActivityInstances;
    return this;
  }

  public Map<String, Object> getJson() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("type", type);
    json.put("activityId", activityId);
    json.put("activityInstanceId", activityInstanceId);
    json.put("transitionInstanceId", transitionInstanceId);
    json.put("ancestorActivityInstanceId", ancestorActivityInstanceId);
    json.put("variables", variables);
    json.put("transitionId", transitionId);

    if (type.equals("cancel") && isFlagSet) {
      json.put("cancelCurrentActiveActivityInstances", cancelCurrentActiveActivityInstances);
    }
    return json;
  }
}
