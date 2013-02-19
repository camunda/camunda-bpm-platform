package org.camunda.bpm.container.impl.jmx.kernel;

/**
 * <p>An atomic step that is part of a composite {@link MBeanDeploymentOperation}.</p>
 * 
 * @author Daniel Meyer
 *
 */
public abstract class MBeanDeploymentOperationStep {
  
  public abstract String getName();
  
  public abstract void performOperationStep(MBeanDeploymentOperation operationContext);
  
  public void cancelOperationStep(MBeanDeploymentOperation operationContext){
    // default behavior is to to nothing if the step fails
  }

  
}
