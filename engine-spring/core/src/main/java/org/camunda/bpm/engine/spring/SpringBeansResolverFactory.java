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

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.scripting.engine.Resolver;
import org.camunda.bpm.engine.impl.scripting.engine.ResolverFactory;
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
      return applicationContext.getBean((String) key);
    } else {
      return null;
    }
  }

  @Override
  public Set<String> keySet() {
    return keySet;
  }
}
