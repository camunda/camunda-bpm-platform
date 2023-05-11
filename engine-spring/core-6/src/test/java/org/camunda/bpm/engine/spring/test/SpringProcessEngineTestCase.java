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
package org.camunda.bpm.engine.spring.test;

import java.util.ServiceLoader;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


/**
 * @author Joram Barrez
 */
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
public abstract class SpringProcessEngineTestCase extends AbstractProcessEngineTestCase implements ApplicationContextAware {

  protected TestContextManager testContextManager;

  @Autowired
  protected ConfigurableApplicationContext applicationContext;

  public SpringProcessEngineTestCase() {
    super();
    this.testContextManager = new TestContextManager(getClass());

    SpringTestHelper testHelper = lookupTestHelper();
    testHelper.beforeTestClass(testContextManager);
  }

  protected SpringTestHelper lookupTestHelper()
  {
    ServiceLoader<SpringTestHelper> serviceLoader = ServiceLoader.load(SpringTestHelper.class);
    return serviceLoader.iterator().next();
  }

  @Override
  public void runBare() throws Throwable {
    testContextManager.prepareTestInstance(this); // this will initialize all dependencies
    try {
      super.runBare();
    }
    finally {
      testContextManager.afterTestClass();
      applicationContext.close();
      applicationContext = null;
      processEngine = null;
      testContextManager = null;
      CachedIntrospectionResults.clearClassLoader(getClass().getClassLoader());
    }
  }

  @Override
  protected void initializeProcessEngine() {
    ContextConfiguration contextConfiguration = getClass().getAnnotation(ContextConfiguration.class);
    processEngine = applicationContext.getBean(ProcessEngine.class);
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = (ConfigurableApplicationContext) applicationContext;
  }

}
