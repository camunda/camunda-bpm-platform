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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Svetlana Dorokhova
 */
public class SpringProcessEngineConfiguration extends SpringTransactionsProcessEngineConfiguration
    implements ApplicationContextAware {

  protected ApplicationContext applicationContext;

  @Override
  protected void initArtifactFactory() {
    if (artifactFactory == null && applicationContext != null) {
      artifactFactory = new SpringArtifactFactory(applicationContext);
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  protected void initScripting() {
    super.initScripting();
    if (beans == null || beans == DEFAULT_BEANS_MAP) {
      // no custom beans defined, make Spring context beans available for scripting
      this.getResolverFactories().add(new SpringBeansResolverFactory(applicationContext));
    }
  }
}
