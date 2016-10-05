/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.event;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ConditionalVariableEventPayload {

  protected final VariableEvent variableEvent;
  protected final VariableScope scope;

  public ConditionalVariableEventPayload(VariableEvent variableEvent, VariableScope scope) {
    this.variableEvent = variableEvent;
    this.scope = scope;
  }

  public VariableEvent getVariableEvent() {
    return variableEvent;
  }

  public VariableScope getScope() {
    return scope;
  }
}
