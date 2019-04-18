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

import javax.servlet.ServletContext;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.springframework.web.context.ServletContextAware;

/**
 * <p>Process Application to be used in a Spring Web Application.</p>
 *
 * <p>Requires the <em>spring-web</em> module to be on the classpath</p>
 *
 * <p>In addition to the services provided by the {@link SpringProcessApplication},
 * this {@link ProcessApplication} exposes the servlet context path of the web application
 * which it is a part of (see {@link ProcessApplicationInfo#PROP_SERVLET_CONTEXT_PATH}).</p>
 *
 * <p>This implementation should be used with Spring Web Applications.</p>
 *
 * @author Daniel Meyer
 *
 */
public class SpringServletProcessApplication extends SpringProcessApplication implements ServletContextAware {

  protected ServletContext servletContext;

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  @Override
  public void start() {
    properties.put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, servletContext.getContextPath());
    super.start();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // for backwards compatibility
    start();
  }

}
