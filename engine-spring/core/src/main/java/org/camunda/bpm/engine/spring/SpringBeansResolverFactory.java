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
package org.camunda.bpm.engine.spring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.scripting.engine.Resolver;
import org.camunda.bpm.engine.impl.scripting.engine.ResolverFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * {@link ResolverFactory} and {@link Resolver} classes to make the beans
 * managed by the Spring container available in scripting
 * </p>
 * 
 * <p>
 * {@see org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration#initScripting()}
 * <p>
 *
 */
public class SpringBeansResolverFactory implements ResolverFactory, Resolver {

  protected static Logger LOG = Logger.getLogger(SpringBeansResolverFactory.class.getName());

  protected static String SCOPE_NOT_ACTIVE_EXCEPTION = "org.springframework.beans.factory.support.ScopeNotActiveException";

  private ApplicationContext applicationContext;
  private Set<String> keySet;

  public SpringBeansResolverFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;

    String[] beannames = applicationContext.getBeanDefinitionNames();
    this.keySet = new HashSet<>(Arrays.asList(beannames));
  }

  @Override
  public Resolver createResolver(VariableScope variableScope) {
    return this;
  }

  @Override
  public boolean containsKey(Object key) {
    if (key instanceof String) {
      return keySet.contains((String) key);
    } else {
      return false;
    }
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      try {
        return applicationContext.getBean((String) key);
      } catch (BeanCreationException ex) {
        // Only swallow exceptions for beans with inactive scope.
        // Unfortunately, we cannot use ScopeNotActiveException directly as
        // it is only available starting with Spring 5.3, but we still support Spring 4.
        if (SCOPE_NOT_ACTIVE_EXCEPTION.equals(ex.getClass().getName())) {
          LOG.info("Bean '" + key + "' cannot be accessed since scope is not active. Instead, null is returned. "
              + "Full exception message: " + ex.getMessage());
          return null;

        } else {
          throw ex;

        }
      }
    } else {
      return null;
    }
  }

  @Override
  public Set<String> keySet() {
    return keySet;
  }
}
