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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

/**
 * <p>A {@link AbstractProcessApplication} Implementation to be used in a Servlet container environment.</p>
 * 
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 * 
 */
public class ServletProcessApplication extends AbstractProcessApplication implements ServletContextListener {

  protected String servletContextName;
  protected String servletContextPath;
  
  protected ProcessApplicationReferenceImpl reference;

  protected ClassLoader processApplicationClassloader;


  protected String autodetectProcessApplicationName() {
    return servletContextName != null ? servletContextName : servletContextPath;
  }

  public ProcessApplicationReference getReference() {
     if(reference == null) {
       reference = new ProcessApplicationReferenceImpl(this);
     } 
     return reference;
  }

  public void contextInitialized(ServletContextEvent sce) {
    servletContextPath = sce.getServletContext().getContextPath();
    servletContextName = sce.getServletContext().getServletContextName();
    
    processApplicationClassloader = initProcessApplicationClassloader(sce);    
    
    // perform lifecycle start
    deploy();
  }

  protected ClassLoader initProcessApplicationClassloader(ServletContextEvent sce) {
    
    if(isServlet30ApiPresent(sce)) {      
      return ClassLoaderUtil.getServletContextClassloader(sce);
      
    }else {
      return ClassLoaderUtil.getClassloader(getClass());
      
    }
    
  }

  private boolean isServlet30ApiPresent(ServletContextEvent sce) {
    return sce.getServletContext().getMajorVersion() >= 3;
  }

  public ClassLoader getProcessApplicationClassloader() {
    return processApplicationClassloader;
  }

  public void contextDestroyed(ServletContextEvent sce) {
    
    // perform lifecycle stop
    undeploy();
    
    // clear the reference
    if(reference != null) {
      reference.clear();
    }
    reference = null;
  }

  public Map<String, String> getProperties() {
    Map<String, String> properties = new HashMap<String, String>();
    
    // set the servlet context path as property
    properties.put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, servletContextPath);
    
    return properties;
  }

}