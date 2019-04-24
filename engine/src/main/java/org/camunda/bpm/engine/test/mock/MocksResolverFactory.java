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
package org.camunda.bpm.engine.test.mock;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.scripting.engine.Resolver;
import org.camunda.bpm.engine.impl.scripting.engine.ResolverFactory;

import java.util.Set;

/**
 * @author Tassilo Weidner
 */
public class MocksResolverFactory implements ResolverFactory, Resolver {

  @Override
  public Resolver createResolver(VariableScope variableScope) {
    return this;
  }

  @Override
  public boolean containsKey(Object key) {
    return Mocks.get(key) != null;
  }

  @Override
  public Object get(Object key) {
    return Mocks.get(key);
  }

  @Override
  public Set<String> keySet() {
    return Mocks.getMocks().keySet();
  }

}
