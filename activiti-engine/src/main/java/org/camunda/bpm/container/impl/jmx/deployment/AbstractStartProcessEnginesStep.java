package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

/**
 * <p>Deployment operation step that is responsible for starting all process
 * engines declared in a {@link List} of {@link ProcessEngineXml} files.</p>
 * 
 * <p>This step does not start the process engines directly but rather creates
 * individual {@link StartProcessEngineStep} instances that each start a process
 * engine.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public abstract class AbstractStartProcessEnginesStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Start process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    List<ProcessEngineXml> processEngines = getProcessEnginesXmls(operationContext);

    for (ProcessEngineXml parsedProcessEngine : processEngines) {
      // for each process engine add a new deployment step
      operationContext.addStep(new StartProcessEngineStep(parsedProcessEngine));
    }

  }

  protected abstract List<ProcessEngineXml> getProcessEnginesXmls(MBeanDeploymentOperation operationContext);

}
