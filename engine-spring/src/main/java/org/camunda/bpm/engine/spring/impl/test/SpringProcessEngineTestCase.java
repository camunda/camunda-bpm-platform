/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.spring.impl.test;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;


/**
 * @author Joram Barrez
 */
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
public class SpringProcessEngineTestCase extends AbstractProcessEngineTestCase implements ApplicationContextAware {

  protected static Map<String, ProcessEngine> cachedProcessEngines = new HashMap<String, ProcessEngine>();

  protected TestContextManager testContextManager;

  @Autowired
  protected ApplicationContext applicationContext;

  public SpringProcessEngineTestCase() {
    super();
    this.testContextManager = new TestContextManager(getClass());
  }

  @Override
  public void runBare() throws Throwable {
    testContextManager.prepareTestInstance(this); // this will initialize all dependencies
    super.runBare();
  }

  @Override
  protected void initializeProcessEngine() {
    ContextConfiguration contextConfiguration = getClass().getAnnotation(ContextConfiguration.class);
    String processEngineKey = createProcessEngineKey(contextConfiguration);

    processEngine = cachedProcessEngines.get(processEngineKey);
    if (processEngine==null) {
      processEngine = applicationContext.getBean(ProcessEngine.class);
      cachedProcessEngines.put(processEngineKey, processEngine);
    }
  }

  protected String createProcessEngineKey(ContextConfiguration contextConfiguration) {
    String processEngineKey = null;
    String[] value = contextConfiguration.value();
    Class<?>[] classes = contextConfiguration.classes();

    if (value.length > 1 || classes.length > 1) {
      throw new ProcessEngineException("SpringProcessEngineTestCase requires exactly one value in annotation ContextConfiguration");
    }

    if (value != null && value.length == 1) {
      processEngineKey = value[0];

    } else if (classes != null && classes.length == 1) {
      processEngineKey = classes[0].getName();
    }

    if (processEngineKey == null) {
      throw new ProcessEngineException("value or classes is mandatory in ContextConfiguration");
    }
    return processEngineKey;
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
