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
package org.camunda.bpm.container.impl.jmx.deployment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.container.impl.jmx.deployment.util.InjectionUtil;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>Operation step responsible for invoking the {@literal @}{@link PreUndeploy} method of a 
 * ProcessApplication class.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class PreUndeployInvocationStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Invoking @PreUndeploy";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final AbstractProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);
        
    Class<? extends AbstractProcessApplication> paClass = processApplication.getClass();
    Method preUndeployMethod = InjectionUtil.detectAnnotatedMethod(paClass, PreUndeploy.class);

    if(preUndeployMethod == null) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Found no @PreUndeploy annotated method.");
      }
      return;
    }

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Found @PreUndeploy annotated method: " + preUndeployMethod.getName());
    }
    
    // resolve injections
    Object[] injections = InjectionUtil.resolveInjections(operationContext, preUndeployMethod);
    
    try {
      // perform the actual invocation
      preUndeployMethod.invoke(processApplication, injections);
      
    } catch (IllegalArgumentException e) {
      throw new ProcessEngineException("IllegalArgumentException while invoking @PreUndeploy method",e);       
    
    } catch (IllegalAccessException e) {
      throw new ProcessEngineException("IllegalAccessException while invoking @PreUndeploy method", e);
    
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if(cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new ProcessEngineException("Exception while invoking @PreUndeploy method", cause);
      }
    }
      
  }

}
