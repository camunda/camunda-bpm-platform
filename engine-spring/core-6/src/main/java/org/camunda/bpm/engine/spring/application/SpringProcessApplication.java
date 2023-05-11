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
package org.camunda.bpm.engine.spring.application;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationReferenceImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <p>Process Application implementation to be used in a Spring Application.</p>
 *
 * <p>This implementation is meant to be bootstrapped by a Spring Application Context.
 * You can either reference the bean in a Spring application-context XML file or use
 * spring annotation-based bootstrapping from a subclass.</p>
 *
 * <p><strong>HINT:</strong> If your application is a Web Application, consider using the
 * {@link SpringServletProcessApplication}</p>
 *
 * <p>The SpringProcessApplication will use the Bean Name assigned to the bean in the spring
 * application context (see {@link BeanNameAware}). You should always assign a unique bean name
 * to a process application bean. That is, the bean name must be unique accross all applications
 * deployed to the Camunda Platform.</p>
 *
 * @author Daniel Meyer
 *
 */
public class SpringProcessApplication extends AbstractProcessApplication implements ApplicationContextAware, BeanNameAware, ApplicationListener<ApplicationContextEvent> {

  protected Map<String, String> properties = new HashMap<String, String>();
  protected ApplicationContext applicationContext;
  protected String beanName;

  @Override
  protected String autodetectProcessApplicationName() {
    return beanName;
  }

  public ProcessApplicationReference getReference() {
    return new ProcessApplicationReferenceImpl(this);
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setBeanName(String name) {
    this.beanName = name;
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void onApplicationEvent(ApplicationContextEvent event) {
    try {
      // we only want to listen for context events of the main application
      // context, not its children
      if (event.getSource().equals(applicationContext)) {
        if (event instanceof ContextRefreshedEvent && !isDeployed) {
          // deploy the process application
          afterPropertiesSet();
        } else if (event instanceof ContextClosedEvent) {
          // undeploy the process application
          destroy();
        } else {
          // ignore
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void start() {
    deploy();
  }

  public void stop() {
    undeploy();
  }

  public void afterPropertiesSet() throws Exception {
    // for backwards compatibility
    start();
  }

  public void destroy() throws Exception {
    // for backwards compatibility
    stop();
  }

}
