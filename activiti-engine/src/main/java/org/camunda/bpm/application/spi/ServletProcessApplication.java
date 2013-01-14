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
package org.camunda.bpm.application.spi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.impl.ProcessApplicationReferenceImpl;

/**
 * <p>A {@link ProcessApplication} Implementation to be used in a Servlet container environment.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ServletProcessApplication extends ProcessApplication implements ServletContextListener {

  protected String servletContextName;
  
  protected ProcessApplicationReferenceImpl reference;

  protected ClassLoader processApplicationClassloader;

  public String getName() {
    return servletContextName;
  }

  public ProcessApplicationReference getReference() {
     if(reference == null) {
       reference = new ProcessApplicationReferenceImpl(this);
     } 
     return reference;
  }

  public void contextInitialized(ServletContextEvent sce) {
    servletContextName = sce.getServletContext().getServletContextName();
    if(servletContextName == null) {
      servletContextName = sce.getServletContext().getContextPath();
    }
    processApplicationClassloader = sce.getServletContext().getClassLoader();    
  }
  
  public ClassLoader getProcessApplicationClassloader() {
    return processApplicationClassloader;
  }

  public void contextDestroyed(ServletContextEvent sce) {
    if(reference != null) {
      reference.clear();
    }
    reference = null;
  }

  protected void lookupArchiveService() {
    
  }

  protected void lookupProcessEngineService() {
    
  }

}