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
package org.camunda.bpm.spring.boot.starter;

import static org.camunda.bpm.application.ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH;
import static org.camunda.bpm.spring.boot.starter.util.GetProcessApplicationNameFromAnnotation.processApplicationNameFromAnnotation;
import static org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger.LOG;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletContext;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaDeploymentConfiguration;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.camunda.bpm.spring.boot.starter.event.PreUndeployEvent;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;

@Configuration
public class SpringBootProcessApplication extends SpringProcessApplication {

  @Bean
  public static CamundaDeploymentConfiguration deploymentConfiguration() {
    return new CamundaDeploymentConfiguration() {
      @Override
      public Set<Resource> getDeploymentResources() {
        return Collections.emptySet();
      }

      @Override
      public void preInit(ProcessEngineConfigurationImpl configuration) {
        LOG.skipAutoDeployment();
      }

      @Override
      public String toString() {
        return "disableDeploymentResourcePattern";
      }
    };
  }

  @Value("${spring.application.name:null}")
  protected Optional<String> springApplicationName;

  protected String contextPath = "/";

  @Autowired
  protected CamundaBpmProperties camundaBpmProperties;

  @Autowired
  protected ProcessEngine processEngine;

  @Autowired
  protected ApplicationEventPublisher eventPublisher;

  @Override
  public void afterPropertiesSet() throws Exception {
    processApplicationNameFromAnnotation(applicationContext)
      .apply(springApplicationName)
      .ifPresent(this::setBeanName);

    if (camundaBpmProperties.getGenerateUniqueProcessApplicationName()) {
      setBeanName(CamundaBpmProperties.getUniqueName(CamundaBpmProperties.UNIQUE_APPLICATION_NAME_PREFIX));
    }

    String processEngineName = processEngine.getName();
    setDefaultDeployToEngineName(processEngineName);

    RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine);

    properties.put(PROP_SERVLET_CONTEXT_PATH, contextPath);
    super.afterPropertiesSet();
  }

  @Override
  public void destroy() throws Exception {
    super.destroy();
    RuntimeContainerDelegate.INSTANCE.get().unregisterProcessEngine(processEngine);
  }

  @PostDeploy
  public void onPostDeploy(ProcessEngine processEngine) {
    eventPublisher.publishEvent(new PostDeployEvent(processEngine));
  }

  @PreUndeploy
  public void onPreUndeploy(ProcessEngine processEngine) {
    eventPublisher.publishEvent(new PreUndeployEvent(processEngine));
  }

  @ConditionalOnWebApplication
  @Configuration
  class WebApplicationConfiguration implements ServletContextAware {

    @Override
    public void setServletContext(ServletContext servletContext) {
      if (!StringUtils.isEmpty(servletContext.getContextPath())) {
        contextPath = servletContext.getContextPath();
      }
    }
  }
}
