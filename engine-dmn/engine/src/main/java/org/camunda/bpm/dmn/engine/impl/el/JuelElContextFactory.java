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
package org.camunda.bpm.dmn.engine.impl.el;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;

import org.camunda.bpm.engine.variable.context.VariableContext;

import de.odysseus.el.util.SimpleContext;

/**
 * @author Daniel Meyer
 *
 */
public class JuelElContextFactory {

  protected final ELResolver resolver;

  public JuelElContextFactory(ELResolver resolver) {
    this.resolver = resolver;
  }

  public ELContext createElContext(VariableContext variableContext) {
    SimpleContext elContext = new SimpleContext(resolver);
    elContext.putContext(VariableContext.class, variableContext);
    return elContext;
  }

}
