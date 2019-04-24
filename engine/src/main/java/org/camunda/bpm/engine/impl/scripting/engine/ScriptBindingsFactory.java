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
package org.camunda.bpm.engine.impl.scripting.engine;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;

import org.camunda.bpm.engine.delegate.VariableScope;


/**
 * <p>Factory for the Bindings used by the {@link ScriptingEngines}. The default
 * implementation will wrap the provided default bindings using an {@link ScriptBindings}
 * implementation.</p>
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ScriptBindingsFactory {

  protected List<ResolverFactory> resolverFactories;

  public ScriptBindingsFactory(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
  }

  public Bindings createBindings(VariableScope variableScope, Bindings engineBindings) {
    List<Resolver> scriptResolvers = new ArrayList<Resolver>();
    for (ResolverFactory scriptResolverFactory: resolverFactories) {
      Resolver resolver = scriptResolverFactory.createResolver(variableScope);
      if (resolver!=null) {
        scriptResolvers.add(resolver);
      }
    }
    return new ScriptBindings(scriptResolvers, variableScope, engineBindings);
  }

  public List<ResolverFactory> getResolverFactories() {
    return resolverFactories;
  }

  public void setResolverFactories(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
  }
}
