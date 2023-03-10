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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * This base class provides a template method to handle servlet applications annotated
 * with the {@link ProcessApplication}.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractServletProcessApplicationDeployer {

  protected static final ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  protected void onStartUp(Set<Class<?>> c, String contextPath, Class<?> servletProcessApplicationClass, Consumer<String> processApplicationClassNameConsumer) throws Exception {
    if (c == null || c.isEmpty()) {
      // skip deployments that do not carry a PA
      return;
    }

    if (c.contains(ProcessApplication.class)) {
      // copy into a fresh Set as we don't know if the original Set is mutable or immutable
      c = new HashSet<>(c);
      // and now remove the annotation itself
      c.remove(ProcessApplication.class);
    }

    if (c.size() > 1) {
      // a deployment must only contain a single PA
      throw getServletException(LOG.multiplePasException(c, contextPath));

    } else if (c.size() == 1) {
      Class<?> paClass = c.iterator().next();

      // validate whether it is a legal Process Application
      if (!AbstractProcessApplication.class.isAssignableFrom(paClass)) {
        throw getServletException(LOG.paWrongTypeException(paClass));
      }

      // add it as listener if it's a servlet process application
      if (servletProcessApplicationClass.isAssignableFrom(paClass)) {
        LOG.detectedPa(paClass);
        processApplicationClassNameConsumer.accept(paClass.getName());
      }
    } else {
      LOG.servletDeployerNoPaFound(contextPath);
    }
  }

  protected abstract Exception getServletException(String message);
}
