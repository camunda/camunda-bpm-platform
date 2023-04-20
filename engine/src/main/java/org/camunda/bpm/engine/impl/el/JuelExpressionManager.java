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
package org.camunda.bpm.engine.impl.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElProvider;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.dmn.el.ProcessEngineJuelElProvider;
import org.camunda.bpm.engine.impl.javax.el.ArrayELResolver;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.javax.el.ExpressionFactory;
import org.camunda.bpm.engine.impl.javax.el.FunctionMapper;
import org.camunda.bpm.engine.impl.javax.el.ListELResolver;
import org.camunda.bpm.engine.impl.javax.el.MapELResolver;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.camunda.bpm.engine.impl.juel.ExpressionFactoryImpl;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.test.mock.MockElResolver;
import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * JUEL-specific implementation of an {@link ExpressionManager}.
 *
 * @author Tom Baeyens
 * @author Dave Syer
 * @author Frederik Heremans
 */
public class JuelExpressionManager implements ExpressionManager, ElProviderCompatible {

  protected Map<String, Method> functions = new HashMap<String, Method>();
  protected ExpressionFactory expressionFactory;
  protected Map<Object, Object> beans;
  protected volatile boolean initialized = false;
  protected ELResolver elResolver;
  protected FunctionMapper functionMapper;
  // Default implementation (does nothing)
  protected ELContext parsingElContext;
  protected volatile ElProvider elProvider;

  public JuelExpressionManager() {
    this(null);
  }

  public JuelExpressionManager(Map<Object, Object> beans) {
    // Use the ExpressionFactoryImpl built-in version of juel, with parametrised
    // method expressions enabled
    expressionFactory = new ExpressionFactoryImpl();
    this.beans = beans;
  }

  @Override
  public Expression createExpression(String expression) {
    ensureInitialized();
    ValueExpression valueExpression = createValueExpression(expression);
    return new JuelExpression(valueExpression, this, expression);
  }

  @Override
  public void addFunction(String name, Method function) {
    EnsureUtil.ensureNotEmpty("name", name);
    EnsureUtil.ensureNotNull("function", function);
    functions.put(name, function);
  }

  public ValueExpression createValueExpression(String expression) {
    ensureInitialized();
    return expressionFactory.createValueExpression(parsingElContext, expression, Object.class);
  }

  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  public ELContext getElContext(VariableScope variableScope) {
    ensureInitialized();
    ELContext elContext = null;
    if (variableScope instanceof AbstractVariableScope) {
      AbstractVariableScope variableScopeImpl = (AbstractVariableScope) variableScope;
      elContext = variableScopeImpl.getCachedElContext();
    }

    if (elContext == null) {
      elContext = createElContext(variableScope);
      if (variableScope instanceof AbstractVariableScope) {
        ((AbstractVariableScope) variableScope).setCachedElContext(elContext);
      }
    }

    return elContext;
  }

  public ELContext createElContext(VariableContext variableContext) {
    ensureInitialized();
    ProcessEngineElContext elContext = new ProcessEngineElContext(functionMapper, elResolver);
    elContext.putContext(ExpressionFactory.class, expressionFactory);
    elContext.putContext(VariableContext.class, variableContext);
    return elContext;
  }

  protected ProcessEngineElContext createElContext(VariableScope variableScope) {
    ensureInitialized();
    ProcessEngineElContext elContext = new ProcessEngineElContext(functionMapper, elResolver);
    elContext.putContext(ExpressionFactory.class, expressionFactory);
    elContext.putContext(VariableScope.class, variableScope);
    return elContext;
  }

  protected void ensureInitialized() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          elResolver = createElResolver();
          functionMapper = createFunctionMapper();
          parsingElContext = new ProcessEngineElContext(functionMapper);
          initialized = true;
        }
      }
    }
  }

  protected ELResolver createElResolver() {
    CompositeELResolver elResolver = new CompositeELResolver();
    elResolver.add(new VariableScopeElResolver());
    elResolver.add(new VariableContextElResolver());
    elResolver.add(new MockElResolver());

    if (beans != null) {
      // ACT-1102: Also expose all beans in configuration when using standalone
      // engine, not
      // in spring-context
      elResolver.add(new ReadOnlyMapELResolver(beans));
    }

    elResolver.add(new ProcessApplicationElResolverDelegate());

    elResolver.add(new ArrayELResolver());
    elResolver.add(new ListELResolver());
    elResolver.add(new MapELResolver());
    elResolver.add(new ProcessApplicationBeanElResolverDelegate());

    return elResolver;
  }

  protected FunctionMapper createFunctionMapper() {
    FunctionMapper functionMapper = new FunctionMapper() {
      @Override
      public Method resolveFunction(String prefix, String localName) {
        String fullName = localName;
        if (prefix != null && !prefix.trim().isEmpty()) {
          fullName = prefix + ":" + localName;
        }
        return functions.get(fullName);
      }

    };
    return functionMapper;
  }

  @Override
  public ElProvider toElProvider() {
    if (elProvider == null) {
      synchronized (this) {
        if (elProvider == null) {
          elProvider = createElProvider();
        }
      }
    }
    return elProvider;
  }

  protected ElProvider createElProvider() {
    return new ProcessEngineJuelElProvider(this);
  }
}
