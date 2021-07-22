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
package org.camunda.bpm.engine.cdi.test;

import org.camunda.bpm.engine.cdi.test.api.BusinessProcessBeanTest;
import org.camunda.bpm.engine.cdi.test.api.annotation.CompleteTaskTest;
import org.camunda.bpm.engine.cdi.test.api.annotation.ProcessVariableLocalTypedTest;
import org.camunda.bpm.engine.cdi.test.api.annotation.ProcessVariableTypedTest;
import org.camunda.bpm.engine.cdi.test.bean.QuarkusDeclarativeProcessController;
import org.camunda.bpm.engine.cdi.test.api.annotation.StartProcessTest;
import org.camunda.bpm.engine.cdi.test.bpmn.SignalEventTest;
import org.camunda.bpm.engine.cdi.test.bean.SignalEventTestBeans;
import org.camunda.bpm.engine.cdi.test.impl.context.BusinessProcessContextTest;
import org.camunda.bpm.engine.cdi.test.bean.BusinessProcessContextTestBeans;
import org.camunda.bpm.engine.cdi.test.impl.context.MultiInstanceTest;
import org.camunda.bpm.engine.cdi.test.bean.QuarkusLocalVariableBean;
import org.camunda.bpm.engine.cdi.test.bean.QuarkusCdiTaskListenerBean;
import org.camunda.bpm.engine.cdi.test.impl.el.TaskListenerInvocationTest;
import org.camunda.bpm.engine.cdi.test.impl.event.EventNotificationTest;
import org.camunda.bpm.engine.cdi.test.impl.event.TestEventListener;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration.DeploymentContentBuilder;
import org.jboss.arquillian.container.test.spi.client.deployment.AutomaticDeployment;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuarkusAutomaticDeployment implements AutomaticDeployment {

  protected Map<Class<?>, Set<Class<?>>> beanDefinitions = new HashMap<Class<?>, Set<Class<?>>>() {{

    Set<Class<?>> processControllerBean = asSet(QuarkusDeclarativeProcessController.class);

    put(CompleteTaskTest.class, processControllerBean);
    put(ProcessVariableLocalTypedTest.class, processControllerBean);
    put(ProcessVariableTypedTest.class, processControllerBean);
    put(StartProcessTest.class, processControllerBean);

    Set<Class<?>> businessProcessContextBeans = asSet(BusinessProcessContextTestBeans.class);
    put(BusinessProcessBeanTest.class, businessProcessContextBeans);
    put(BusinessProcessContextTest.class, businessProcessContextBeans);

    put(SignalEventTest.class, asSet(SignalEventTestBeans.class));
    put(MultiInstanceTest.class, asSet(QuarkusLocalVariableBean.class));
    put(TaskListenerInvocationTest.class, asSet(QuarkusCdiTaskListenerBean.class));
    put(EventNotificationTest.class, asSet(TestEventListener.class));
  }};

  @Override
  public DeploymentConfiguration generateDeploymentScenario(TestClass testClass) {
    Class<?> javaClass = testClass.getJavaClass();
    Class<?>[] classes = getBeanDefinitions(javaClass).toArray(new Class[0]);

    JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addClasses(classes);

    return new DeploymentContentBuilder(jar).get();
  }

  protected Set<Class<?>> getBeanDefinitions(Class<?> javaClass) {
    List<Class<?>> defaultClasses = Arrays.asList(javaClass, javaClass.getSuperclass());

    Set<Class<?>> classes = beanDefinitions.get(javaClass);
    if (classes == null) {
      return new HashSet<>(defaultClasses);

    } else {
      classes.addAll(defaultClasses);
      return classes;

    }
  }

  protected Set<Class<?>> asSet(Class<?>... clazz) {
    return new HashSet<>(Arrays.asList(clazz));
  }

}
