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
package org.camunda.bpm.container.impl.jmx.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>A DeploymentOperation allows bundling multiple deployment steps into a
 * composite operation that succeeds or fails atomically.</p>
 *  
 * <p>The DeploymentOperation is composed of a list of individual steps (
 * {@link MBeanDeploymentOperationStep}). Each step may or may not install new
 * services into the container. If one of the steps fails, the operation makes
 * sure that
 * <ul>
 *  <li>all successfully completed steps are notified by calling their
 *  {@link MBeanDeploymentOperationStep#cancelOperationStep(MBeanDeploymentOperation)}
 *  method.</li>
 *  <li>all services installed in the context of the operation are removed from the container.</li>
 * </ul>
 * 
 * @author Daniel Meyer
 * 
 */
public class MBeanDeploymentOperation {
  
  protected Logger log = Logger.getLogger(MBeanDeploymentOperation.class.getName());

  /** the name of this composite operation */
  protected final String name;
  
  /** the service container */
  protected final MBeanServiceContainer serviceContainer;
  
  /** the list of steps that make up this composite operation */
  protected final List<MBeanDeploymentOperationStep> steps;
  
  /** a list of steps that completed successfully */
  protected final List<MBeanDeploymentOperationStep> successfulSteps = new ArrayList<MBeanDeploymentOperationStep>();

  /** the list of services installed by this operation. The {@link #rollbackOperation()} must make sure 
   * all these services are removed if the operation fails. */
  protected List<ObjectName> installedServices = new ArrayList<ObjectName>();
  
  /** a list of attachments allows to pass state from one operation to another */
  protected Map<String, Object> attachments = new HashMap<String, Object>();

  protected boolean isRollbackOnFailure = true;

  protected MBeanDeploymentOperationStep currentStep;
  
  public MBeanDeploymentOperation(String name, MBeanServiceContainer container, List<MBeanDeploymentOperationStep> steps) {
    this.name = name;
    this.serviceContainer = container;
    this.steps = steps;
  }
  
  // getter / setters /////////////////////////////////
  
  @SuppressWarnings("unchecked")
  public <S> S getAttachment(String name) {
    return (S) attachments.get(name);    
  }
  
  public void addAttachment(String name, Object value) {
    attachments.put(name, value);
  }
  
  /**
   * Add a new atomic step to the composite operation.
   * If the operation is currently executing a step, the step is added after the current step. 
   */
  public void addStep(MBeanDeploymentOperationStep step) {
    if(currentStep != null) {
      steps.add(steps.indexOf(currentStep)+1, step);
    } else {
      steps.add(step);
    }
  }
  
  void serviceAdded(ObjectName serviceName) {
    installedServices.add(serviceName);
  }
  
  public MBeanServiceContainer getServiceContainer() {
    return serviceContainer;
  }
  
  // runtime aspect ///////////////////////////////////

  public void execute() {

    while (!steps.isEmpty()) {
      currentStep = steps.remove(0);

      try {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Performing operation step: '" + currentStep.getName() + "'");
        }
        currentStep.performOperationStep(this);
        successfulSteps.add(currentStep);
        if (log.isLoggable(Level.FINE)) {
          log.fine("Successfully performed operation step: '" + currentStep.getName() + "'");
        }
      } catch (Exception e) {
        
        if(isRollbackOnFailure) {
          
          try {
            rollbackOperation();     
            
          } catch(Exception e2) {
            log.log(Level.SEVERE, "Exception while rolling back operation " + e2.getMessage(), e2);
          }   
          
          // re-throw the original exception
          throw new ProcessEngineException("Exception while performing '" + name+" => "+currentStep.getName()+"': " + e.getMessage(), e);
          
        } else {
          log.log(Level.SEVERE, "Exception while performing operation step '" + currentStep.getName() + "': " + e.getMessage(), e);
          
        }
        
      }
    }

  }
  
  protected void rollbackOperation() {
    
    // first, rollback all successful steps    
    for (MBeanDeploymentOperationStep step : successfulSteps) {      
      try {
        step.cancelOperationStep(this);        
        
      } catch(Exception e) {
        log.log(Level.SEVERE, "Exception while cancelling '"+step.getName()+"'", e);
        
      }      
    }
    
    // second, remove services    
    for (ObjectName serviceName : installedServices) {      
      try {
        serviceContainer.stopService(serviceName);
        
      } catch(Exception e) {
        log.log(Level.SEVERE, "Exception while stopping service", e);
        
      }      
    }   
  }
  
  public List<ObjectName> getInstalledServices() {
    return installedServices;
  }
  
  // builder /////////////////////////////
  
  public static class MBeanDeploymentOperationBuilder {
    
    protected MBeanServiceContainer container;
    protected String name;
    protected boolean isUndeploymentOperation = false;
    protected List<MBeanDeploymentOperationStep> steps = new ArrayList<MBeanDeploymentOperationStep>();
    protected Map<String, Object> initialAttachments = new HashMap<String, Object>();
    
    public MBeanDeploymentOperationBuilder(MBeanServiceContainer container, String name) {
      this.container = container;
      this.name = name;
    }
    
    public MBeanDeploymentOperationBuilder addStep(MBeanDeploymentOperationStep step) {
      steps.add(step);
      return this;
    }

    public MBeanDeploymentOperationBuilder addAttachment(String name, Object value) {
      initialAttachments.put(name, value);
      return this;
    }
    
    public MBeanDeploymentOperationBuilder setUndeploymentOperation() {
      isUndeploymentOperation = true;
      return this;
    }
    
    public void execute() {
      MBeanDeploymentOperation operation = new MBeanDeploymentOperation(name, container, steps);
      operation.isRollbackOnFailure = !isUndeploymentOperation;
      operation.attachments.putAll(initialAttachments);
      container.executeDeploymentOperation(operation);
    }
    
  }
    

}
