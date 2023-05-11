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

import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.impl.DefaultArtifactFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Svetlana Dorokhova
 */
public class SpringArtifactFactory implements ArtifactFactory {

  private ArtifactFactory defaultArtifactFactory = new DefaultArtifactFactory();

  private ApplicationContext applicationContext;

  public SpringArtifactFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public <T> T getArtifact(Class<T> clazz) {
    T instance;

    try {
      instance = applicationContext.getBean(clazz);
    } catch (NoSuchBeanDefinitionException ex) {
      // fall back to using newInstance()
      instance = defaultArtifactFactory.getArtifact(clazz);
    }

    return instance;
  }
}
