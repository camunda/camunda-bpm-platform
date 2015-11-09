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
package org.camunda.bpm.application.impl;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

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
public class ServletProcessApplicationDeployer implements ServletContainerInitializer {

  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    if(c == null || c.isEmpty()) {
      // skip deployments that do not carry a PA
      return;

    }

    if (c.contains(ProcessApplication.class)) {
      // this is a workaround for a bug in WebSphere-8.5 who
      // ships the annotation itself as part of the discovered classes.

      // copy into a fresh Set as we don't know if the original Set is mutable or immutable.
      c = new HashSet<Class<?>>(c);

      // and now remove the annotation itself.
      c.remove(ProcessApplication.class);
    }


    String contextPath = ctx.getContextPath();
    if(c.size() > 1) {
      // a deployment must only contain a single PA
      throw LOG.multiplePasException(c, contextPath);

    } else if(c.size() == 1) {
      Class<?> paClass = c.iterator().next();

      // validate whether it is a legal Process Application
      if(!AbstractProcessApplication.class.isAssignableFrom(paClass)) {
        throw LOG.paWrongTypeException(paClass);
      }

      // add it as listener if it's a ServletProcessApplication
      if(ServletProcessApplication.class.isAssignableFrom(paClass)) {
        LOG.detectedPa(paClass);
        ctx.addListener(paClass.getName());
      }
    }
    else {
      LOG.servletDeployerNoPaFound(contextPath);
    }

  }

}
