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
package org.camunda.bpm.engine.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;

public class CustomJacksonDateFormatListener implements ServletContextListener {

  public final static String CONTEXT_PARAM_NAME = "org.camunda.bpm.engine.rest.jackson.dateFormat";

  public void contextInitialized(ServletContextEvent sce) {
    String dateFormat = sce.getServletContext().getInitParameter(CONTEXT_PARAM_NAME);
    if (dateFormat != null) {
      JacksonConfigurator.setDateFormatString(dateFormat);
    }
  }

  public void contextDestroyed(ServletContextEvent sce) {
    // reset to default format
    JacksonConfigurator.setDateFormatString(JacksonConfigurator.DEFAULT_DATE_FORMAT);
  }

}
