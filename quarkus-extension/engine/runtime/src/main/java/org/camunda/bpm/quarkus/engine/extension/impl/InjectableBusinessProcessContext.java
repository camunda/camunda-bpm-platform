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
package org.camunda.bpm.quarkus.engine.extension.impl;

import io.quarkus.arc.InjectableContext;
import org.camunda.bpm.engine.cdi.impl.context.BusinessProcessContext;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.inject.spi.BeanManager;

public class InjectableBusinessProcessContext extends BusinessProcessContext implements InjectableContext {

  @Override
  protected BeanManager getBeanManager() {
    return BeanManagerLookup.localInstance;
  }

  @Override
  public ContextState getState() {
    // Not needed internally by Quarkus
    throw new UnsupportedOperationException("io.quarkus.arc.InjectableContext#getState is unsupported");
  }

  @Override
  public void destroy() {
    // Not needed internally by Quarkus
    throw new UnsupportedOperationException("io.quarkus.arc.InjectableContext#destroy is unsupported");
  }

  @Override
  public void destroy(Contextual<?> contextual) {
    // Not needed internally by Quarkus
    throw new UnsupportedOperationException("io.quarkus.arc.InjectableContext#destroy(contextual) is unsupported");
  }

}
