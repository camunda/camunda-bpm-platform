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
package org.camunda.bpm.engine.impl.core.variable.scope;

import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class CmmnVariableInvocationListener implements VariableInstanceLifecycleListener<VariableInstanceEntity> {

  public static final CmmnVariableInvocationListener INSTANCE = new CmmnVariableInvocationListener();

  @Override
  public void onCreate(VariableInstanceEntity variable, AbstractVariableScope sourceScope) {
    sourceScope.dispatchEvent(new VariableEvent(variable, VariableListener.CREATE, sourceScope));
  }

  @Override
  public void onUpdate(VariableInstanceEntity variable, AbstractVariableScope sourceScope) {
    sourceScope.dispatchEvent(new VariableEvent(variable, VariableListener.UPDATE, sourceScope));
  }

  @Override
  public void onDelete(VariableInstanceEntity variable, AbstractVariableScope sourceScope) {
    sourceScope.dispatchEvent(new VariableEvent(variable, VariableListener.DELETE, sourceScope));
  }
}
