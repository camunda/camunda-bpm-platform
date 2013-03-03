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
package org.camunda.bpm.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.repository.DeploymentBuilder;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.impl.DefaultElResolverLookup;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;


/**
 * <p>A ProcessApplication is a Java Application that creates a deployment 
 * to an Embedded or Shared ProcessEngine.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public abstract class AbstractProcessApplication {
  
  protected ELResolver processApplicationElResolver;
              
  // deployment /////////////////////////////////////////////////////

  /**
   * Deploy this process application into the runtime container.
   */
  public void deploy() {
    // initialize el resolver
    processApplicationElResolver = initProcessApplicationElResolver();
    // deploy the application
    RuntimeContainerDelegate.INSTANCE.get().deployProcessApplication(this);   
  }

  /**
   * Undeploy this process application from the runtime container.
   */
  public void undeploy() {
    // delegate stopping of the process application to the runtime container.
    RuntimeContainerDelegate.INSTANCE.get().undeployProcessApplication(this);
  }
    
  /**
   * <p>Override this method in order to programmatically add resources to the
   * deployment created by this process application.</p>
   * 
   * <p>This method is invoked at deployment time once for each process archive 
   * deployed by this process application.</p>
   * 
   * <p><strong>NOTE:</strong> this method must NOT call the {@link DeploymentBuilder#deploy()} 
   * method.</p>
   * 
   * @param deploymentBuilder the {@link DeploymentBuilder} used to construct the deployment.
   * @param processArchiveName the name of the processArchive which is currently being deployed.
   */
  public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder) {
    // default implementation does nothing
  }
    
  // Runtime ////////////////////////////////////////////
  
  /**
   * @return the name of this process application
   */
  public String getName() {
    Class<? extends AbstractProcessApplication> processApplicationClass = getClass();
    String name = null;
    
    try {
      ProcessApplication annotation = processApplicationClass.getAnnotation(ProcessApplication.class);      
      name = annotation.value();
    } catch(NullPointerException nullPointerException) {
      // ignore
    }
    
    if(name == null || name.length()==0) {
      name = autodetectProcessApplicationName();
    }    
    
    return name;    
  }
  
  /**
   * override this method to autodetect an application name in case the
   * {@link ProcessApplication} annotation was used but without parameter.
   */
  protected abstract String autodetectProcessApplicationName();
   
  /**
   * <p>Returns a globally sharable reference to this process application. This reference may be safely passed 
   * to the process engine. And other applications.</p>  
   * 
   * @return a globally sharable reference to this process application. 
   */
  public abstract ProcessApplicationReference getReference();

  /**
   * The default implementation simply modifies the Context {@link ClassLoader}
   * 
   * @param callable the callable to be executed "within" the context of this process application.
   * @return the result of the callback
   * @throws Exception 
   */
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    ClassLoader originalClassloader = ClassLoaderUtil.getContextClassloader();
    
    ClassLoader processApplicationClassloader = getProcessApplicationClassloader();
    
    try {
      ClassLoaderUtil.setContextClassloader(processApplicationClassloader);
      
      return callable.call();
      
    } catch(Exception e) {
      throw new ProcessApplicationExecutionException(e);
      
    } finally {
      ClassLoaderUtil.setContextClassloader(originalClassloader);
    }
    
  }

  /**
   * <p>Override this method to provide an environment-specific {@link ClassLoader} to be used by the process 
   * engine for loading resources from the process applicaiton</p>
   * 
   * <p><strong>NOTE: the process engine must <em>never</em> cache any references to this {@link ClassLoader} 
   * or to classes obtained through this {@link ClassLoader}.</strong></p>
   * 
   * @return the {@link ClassLoader} that can be used to load classes and resources from this process application.
   */
  public ClassLoader getProcessApplicationClassloader() {
    // the default implementation uses the classloader that loaded 
    // the application-provided subclass of this class.    
    return getClass().getClassLoader();
  }
  
  /** 
   * <p>override this method in order to provide a map of properties.</p>
   * 
   * <p>The properties are made available globally through the {@link ProcessApplicationService}</p>
   * 
   * @see ProcessApplicationService
   * @see ProcessApplicationInfo#getProperties()
   */
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap( Collections.<String, String>emptyMap() );
  }

  /**
   * <p>This allows the process application to provide a custom ElResolver to the process engine.</p>
   * 
   * <p>The process engine will use this ElResolver whenever it is executing a 
   * process in the context of this process application.</p>
   * 
   * <p>The process engine must only call this method form Callable implementations passed 
   * to {@link #execute(Callable)}</p>
   */
  public ELResolver getElResolver() {
    
    return processApplicationElResolver;
    
  }

  /**
   * <p>Initializes the process application provided ElResolver. This implementation uses the
   * Java SE {@link ServiceLoader} facilities for resolving implementations of {@link ProcessApplicationElResolver}.</p>
   * 
   * <p>If you want to provide a custom implementation in your application, place a file named 
   * <code>META-INF/org.camunda.bpm.application.ProcessApplicationElResolver</code> inside your application
   * which contains the fully qualified classname of your implementation.</p>
   * 
   * <p>Override this method in order to implement a custom resolving scheme.</p>
   *  
   * @return the process application ElResolver. 
   */
  protected ELResolver initProcessApplicationElResolver() {

    return DefaultElResolverLookup.lookupResolver(this);
    
  }
  
}
