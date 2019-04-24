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
package org.camunda.bpm.engine.impl.core.variable.event;

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableEvent {

  protected VariableInstance variableInstance;
  protected String eventName;
  protected AbstractVariableScope sourceScope;

  public VariableEvent(VariableInstance variableInstance, String eventName, AbstractVariableScope sourceScope) {
    this.variableInstance = variableInstance;
    this.eventName = eventName;
    this.sourceScope = sourceScope;
  }

  public VariableInstance getVariableInstance() {
    return variableInstance;
  }

  public String getEventName() {
    return eventName;
  }

  public AbstractVariableScope getSourceScope() {
    return sourceScope;
  }
}
