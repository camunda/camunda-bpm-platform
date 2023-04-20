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
package org.camunda.bpm.application.impl;

import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import org.camunda.bpm.application.ProcessApplication;

/**
 * <p>This class is an implementation of {@link ServletContainerInitializer} and
 * is notified whenever a subclass of {@link ServletProcessApplication} annotated
 * with the {@link ProcessApplication} annotation is deployed. In such an event,
 * we automatically add the class as {@link ServletContextListener} to the
 * {@link ServletContext}.</p>
 *
 * <p><strong>NOTE:</strong> Only works with Servlet 3.0 or better.</p>
 *
 * @author Daniel Meyer
 *
 */
@HandlesTypes(ProcessApplication.class)
public class ServletProcessApplicationDeployer extends AbstractServletProcessApplicationDeployer implements ServletContainerInitializer {

  @Override
  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    try {
      onStartUp(c, ctx.getContextPath(), ServletProcessApplication.class, ctx::addListener);
    } catch (Exception e) {
      if (e instanceof ServletException) {
        throw (ServletException) e;
      }
    }
  }

  @Override
  protected Exception getServletException(String message) {
    return new ServletException(message);
  }

}
