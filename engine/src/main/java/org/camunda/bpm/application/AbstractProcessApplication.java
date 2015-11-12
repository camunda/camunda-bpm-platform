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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import javax.script.ScriptEngine;

import org.camunda.bpm.application.impl.DefaultElResolverLookup;
import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.application.impl.ProcessApplicationScriptEnvironment;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.javax.el.BeanELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;


/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractProcessApplication implements ProcessApplicationInterface {

  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  protected ELResolver processApplicationElResolver;
  protected BeanELResolver processApplicationBeanElResolver;
  protected ProcessApplicationScriptEnvironment processApplicationScriptEnvironment;

  protected boolean isDeployed = false;

  // deployment /////////////////////////////////////////////////////

  public void deploy() {
    if(isDeployed) {
      LOG.alreadyDeployed();
    }
    else {
      // deploy the application
      RuntimeContainerDelegate.INSTANCE.get().deployProcessApplication(this);
      isDeployed = true;
    }
  }

  public void undeploy() {
    if(!isDeployed) {
      LOG.notDeployed();
    } else {
      // delegate stopping of the process application to the runtime container.
      RuntimeContainerDelegate.INSTANCE.get().undeployProcessApplication(this);
      isDeployed = false;
    }
  }

  public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder) {
    // default implementation does nothing
  }

  // Runtime ////////////////////////////////////////////

  public String getName() {
    Class<? extends AbstractProcessApplication> processApplicationClass = getClass();
    String name = null;

    ProcessApplication annotation = processApplicationClass.getAnnotation(ProcessApplication.class);
    if(annotation != null) {
      name = annotation.value();
    }

    if(name == null || name.length()==0) {
      name = autodetectProcessApplicationName();
    }

    return name;
  }

  /**
   * Override this method to autodetect an application name in case the
   * {@link ProcessApplication} annotation was used but without parameter.
   */
  protected abstract String autodetectProcessApplicationName();

  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    ClassLoader originalClassloader = ClassLoaderUtil.getContextClassloader();

    ClassLoader processApplicationClassloader = getProcessApplicationClassloader();

    try {
      ClassLoaderUtil.setContextClassloader(processApplicationClassloader);

      return callable.call();

    }
    catch(Exception e) {
      throw LOG.processApplicationExecutionException(e);
    }
    finally {
      ClassLoaderUtil.setContextClassloader(originalClassloader);
    }

  }


  public ClassLoader getProcessApplicationClassloader() {
    // the default implementation uses the classloader that loaded
    // the application-provided subclass of this class.
    return ClassLoaderUtil.getClassloader(getClass());
  }

  public ProcessApplicationInterface getRawObject() {
    return this;
  }

  public Map<String, String> getProperties() {
    return Collections.<String, String>emptyMap();
  }

  public ELResolver getElResolver() {
    if(processApplicationElResolver == null) {
      synchronized (this) {
        if(processApplicationElResolver == null) {
          processApplicationElResolver = initProcessApplicationElResolver();
        }
      }
    }
    return processApplicationElResolver;

  }

  public BeanELResolver getBeanElResolver() {
    if(processApplicationBeanElResolver == null) {
      synchronized (this) {
        if(processApplicationBeanElResolver == null) {
          processApplicationBeanElResolver = new BeanELResolver();
        }
      }
    }
    return processApplicationBeanElResolver;
  }

  /**
   * <p>Initializes the process application provided ElResolver. This implementation uses the
   * Java SE {@link ServiceLoader} facilities for resolving implementations of {@link ProcessApplicationElResolver}.</p>
   *
   * <p>If you want to provide a custom implementation in your application, place a file named
   * <code>META-INF/org.camunda.bpm.application.ProcessApplicationElResolver</code> inside your application
   * which contains the fully qualified classname of your implementation. Or simply override this method.</p>
   *
   * @return the process application ElResolver.
   */
  protected ELResolver initProcessApplicationElResolver() {

    return DefaultElResolverLookup.lookupResolver(this);

  }

  public ExecutionListener getExecutionListener() {
    return null;
  }

  public TaskListener getTaskListener() {
    return null;
  }

  /**
   * see {@link ProcessApplicationScriptEnvironment#getScriptEngineForName(String, boolean)}
   */
  public ScriptEngine getScriptEngineForName(String name, boolean cache) {
    return getProcessApplicationScriptEnvironment().getScriptEngineForName(name, cache);
  }

  /**
   * see {@link ProcessApplicationScriptEnvironment#getEnvironmentScripts()}
   */
  public Map<String, List<ExecutableScript>> getEnvironmentScripts() {
    return getProcessApplicationScriptEnvironment().getEnvironmentScripts();
  }

  protected ProcessApplicationScriptEnvironment getProcessApplicationScriptEnvironment() {
    if (processApplicationScriptEnvironment == null) {
      synchronized (this) {
        if (processApplicationScriptEnvironment == null) {
          processApplicationScriptEnvironment = new ProcessApplicationScriptEnvironment(this);
        }
      }
    }
    return processApplicationScriptEnvironment;
  }

}
