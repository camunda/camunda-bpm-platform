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
package org.camunda.bpm.container.impl.spi;

import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>A DeploymentOperation allows bundling multiple deployment steps into a
 * composite operation that succeeds or fails atomically.</p>
 *
 * <p>The DeploymentOperation is composed of a list of individual steps (
 * {@link DeploymentOperationStep}). Each step may or may not install new
 * services into the container. If one of the steps fails, the operation makes
 * sure that
 * <ul>
 *  <li>all successfully completed steps are notified by calling their
 *  {@link DeploymentOperationStep#cancelOperationStep(DeploymentOperation)}
 *  method.</li>
 *  <li>all services installed in the context of the operation are removed from the container.</li>
 * </ul>
 *
 * @author Daniel Meyer
 *
 */
public class DeploymentOperation {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  /** the name of this composite operation */
  protected final String name;

  /** the service container */
  protected final PlatformServiceContainer serviceContainer;

  /** the list of steps that make up this composite operation */
  protected final List<DeploymentOperationStep> steps;

  /** a list of steps that completed successfully */
  protected final List<DeploymentOperationStep> successfulSteps = new ArrayList<DeploymentOperationStep>();

  /** the list of services installed by this operation. The {@link #rollbackOperation()} must make sure
   * all these services are removed if the operation fails. */
  protected List<String> installedServices = new ArrayList<String>();

  /** a list of attachments allows to pass state from one operation to another */
  protected Map<String, Object> attachments = new HashMap<String, Object>();

  protected boolean isRollbackOnFailure = true;

  protected DeploymentOperationStep currentStep;

  public DeploymentOperation(String name, PlatformServiceContainer container, List<DeploymentOperationStep> steps) {
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
  public void addStep(DeploymentOperationStep step) {
    if(currentStep != null) {
      steps.add(steps.indexOf(currentStep)+1, step);
    } else {
      steps.add(step);
    }
  }

  public void serviceAdded(String serviceName) {
    installedServices.add(serviceName);
  }

  public PlatformServiceContainer getServiceContainer() {
    return serviceContainer;
  }

  // runtime aspect ///////////////////////////////////

  public void execute() {

    while (!steps.isEmpty()) {
      currentStep = steps.remove(0);

      try {
        LOG.debugPerformOperationStep(currentStep.getName());

        currentStep.performOperationStep(this);
        successfulSteps.add(currentStep);

        LOG.debugSuccessfullyPerformedOperationStep(currentStep.getName());
      }
      catch (Exception e) {

        if(isRollbackOnFailure) {

          try {
            rollbackOperation();
          }
          catch(Exception e2) {
            LOG.exceptionWhileRollingBackOperation(e2);
          }
          // re-throw the original exception
          throw LOG.exceptionWhilePerformingOperationStep(name, currentStep.getName(), e);
        }

        else {
          LOG.exceptionWhilePerformingOperationStep(currentStep.getName(), e);
        }

      }
    }

  }

  protected void rollbackOperation() {

    // first, rollback all successful steps
    for (DeploymentOperationStep step : successfulSteps) {
      try {
        step.cancelOperationStep(this);
      }
      catch(Exception e) {
        LOG.exceptionWhileRollingBackOperation(e);
      }
    }

    // second, remove services
    for (String serviceName : installedServices) {
      try {
        serviceContainer.stopService(serviceName);
      }
      catch(Exception e) {
        LOG.exceptionWhileStopping("service", serviceName, e);
      }
    }
  }

  public List<String> getInstalledServices() {
    return installedServices;
  }

  // builder /////////////////////////////

  public static class DeploymentOperationBuilder {

    protected PlatformServiceContainer container;
    protected String name;
    protected boolean isUndeploymentOperation = false;
    protected List<DeploymentOperationStep> steps = new ArrayList<DeploymentOperationStep>();
    protected Map<String, Object> initialAttachments = new HashMap<String, Object>();

    public DeploymentOperationBuilder(PlatformServiceContainer container, String name) {
      this.container = container;
      this.name = name;
    }

    public DeploymentOperationBuilder addStep(DeploymentOperationStep step) {
      steps.add(step);
      return this;
    }

    public DeploymentOperationBuilder addSteps(Collection<DeploymentOperationStep> steps) {
      for (DeploymentOperationStep step: steps) {
        addStep(step);
      }
      return this;
    }

    public DeploymentOperationBuilder addAttachment(String name, Object value) {
      initialAttachments.put(name, value);
      return this;
    }

    public DeploymentOperationBuilder setUndeploymentOperation() {
      isUndeploymentOperation = true;
      return this;
    }

    public void execute() {
      DeploymentOperation operation = new DeploymentOperation(name, container, steps);
      operation.isRollbackOnFailure = !isUndeploymentOperation;
      operation.attachments.putAll(initialAttachments);
      container.executeDeploymentOperation(operation);
    }

  }


}
