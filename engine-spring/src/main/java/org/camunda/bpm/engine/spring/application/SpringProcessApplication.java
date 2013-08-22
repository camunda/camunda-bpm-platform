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
package org.camunda.bpm.engine.spring.application;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationReferenceImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
 * deployed to the camunda BPM platform.</p>
 *
 * @author Daniel Meyer
 *
 */
public class SpringProcessApplication extends AbstractProcessApplication implements ApplicationContextAware, InitializingBean, DisposableBean, BeanNameAware {

  protected Map<String, String> properties = new HashMap<String, String>();
  protected ApplicationContext applicationContext;
  private String beanName;

  protected String autodetectProcessApplicationName() {
    return beanName;
  }

  public ProcessApplicationReference getReference() {
    return new ProcessApplicationReferenceImpl(this);
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void afterPropertiesSet() throws Exception {
    // deploy the process application
    deploy();
  }

  public void destroy() throws Exception {
    // undeploy the process application
    undeploy();
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

}
