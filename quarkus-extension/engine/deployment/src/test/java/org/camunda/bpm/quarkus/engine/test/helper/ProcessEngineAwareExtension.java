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
package org.camunda.bpm.quarkus.engine.test.helper;

import io.quarkus.arc.Arc;
import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.test.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

public class ProcessEngineAwareExtension extends QuarkusUnitTest {

  protected String deploymentId;
  protected Supplier<JavaArchive> archiveProducer;
  protected String withConfigurationResource;

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    super.beforeEach(context);

    Method testMethod = context.getTestMethod().orElse(null);
    if (testMethod != null) {
      Deployment annotation = testMethod.getAnnotation(Deployment.class);
      if (annotation != null) {
        Object processEngine = getBean("processEngine");

        String testMethodName = testMethod.getName();
        Class<?> testHelperClass = loadClass(TestHelper.class);
        Method annotationDeploymentSetUp = testHelperClass.getMethod("annotationDeploymentSetUp",
            loadClass(ProcessEngine.class), String[].class, Class.class, String.class);

        String[] resources = annotation.resources();
        Class<?> testClass = context.getTestClass().orElse(null);
        deploymentId = (String) annotationDeploymentSetUp.invoke(testHelperClass, processEngine, resources, testClass,
            testMethodName);
      }
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (deploymentId != null) {
      Object repositoryService = getBean("repositoryService");
      Method deleteDeployment = repositoryService.getClass().getMethod("deleteDeployment", String.class, Boolean.TYPE);
      deleteDeployment.invoke(repositoryService, deploymentId, true);
      deploymentId = null;
    }
    super.afterEach(context);
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (withConfigurationResource == null) {
      super.withConfigurationResource("application.properties");

    } else {
      super.withConfigurationResource(withConfigurationResource);

    }

    JavaArchive javaArchive = archiveProducer.get();
    javaArchive.addClass(ProcessEngineAwareExtension.class);

    Method[] testMethods = context.getTestClass().get().getMethods();
    Arrays.stream(testMethods).forEach(method -> {
      if (method.getAnnotation(Test.class) != null) {
        Deployment annotation = method.getAnnotation(Deployment.class);
        if (annotation != null) {
          String[] resources = annotation.resources();
          if (resources.length == 0) {
            Class<?> testClass = context.getTestClass().orElse(null);
            String testMethodName = method.getName();
            String resource = TestHelper.getBpmnProcessDefinitionResource(testClass, testMethodName);
            javaArchive.addAsResource(resource);

          } else {
            Arrays.stream(resources).forEach(javaArchive::addAsResource);

          }
        }
      }
    });
    super.setArchiveProducer(() -> javaArchive);
    super.beforeAll(context);
  }

  @Override
  public QuarkusUnitTest setArchiveProducer(Supplier<JavaArchive> archiveProducer) {
    this.archiveProducer = archiveProducer;
    return this;
  }

  @Override
  public ProcessEngineAwareExtension withConfigurationResource(String resourceName) {
    this.withConfigurationResource = resourceName;
    return this;
  }

  protected Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
    return Thread.currentThread().getContextClassLoader().loadClass(clazz.getName());
  }

  protected Object getBean(String beanName) throws Exception {
    Class<?> arcClass = loadClass(Arc.class);
    Method containerMethod = arcClass.getMethod("container");

    Object container = containerMethod.invoke(null);
    Method instanceMethod = container.getClass().getMethod("instance", String.class);
    Object instanceHandle = instanceMethod.invoke(container, beanName);

    Method getMethod = instanceHandle.getClass().getMethod("get");
    getMethod.setAccessible(true);

    Object bean = getMethod.invoke(instanceHandle);

    return bean;
  }

}
