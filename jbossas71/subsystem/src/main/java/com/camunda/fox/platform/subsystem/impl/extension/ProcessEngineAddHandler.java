/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.subsystem.impl.extension;


import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_DATASOURCE;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_HISTORY_LEVEL;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.List;
import java.util.Locale;

import javax.transaction.TransactionManager;

import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.platform.ContainerPlatformService;
import com.camunda.fox.platform.subsystem.impl.platform.ProcessEngineConfigurationImpl;
import com.camunda.fox.platform.subsystem.impl.platform.ProcessEngineControllerService;

/**
 * Provides the description and the implementation of the process-engine#add operation.
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineAddHandler extends AbstractAddStepHandler implements DescriptionProvider {
  
  public static final String ATTR_HISTORY_LEVEL = "history-level";
  
  public static final ProcessEngineAddHandler INSTANCE = new ProcessEngineAddHandler();

  public ModelNode getModelDescription(Locale locale) {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Adds a process engine");
    node.get(REQUEST_PROPERTIES, ATTR_DATASOURCE, DESCRIPTION).set("Which datasource to use");
    node.get(REQUEST_PROPERTIES, ATTR_DATASOURCE, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, ATTR_DATASOURCE, REQUIRED).set(true);
    
    node.get(REQUEST_PROPERTIES, ATTR_HISTORY_LEVEL, DESCRIPTION).set("Which history level to use");
    node.get(REQUEST_PROPERTIES, ATTR_HISTORY_LEVEL, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, ATTR_HISTORY_LEVEL, REQUIRED).set(false);
    node.get(REQUEST_PROPERTIES, ATTR_HISTORY_LEVEL, DEFAULT).set("audit");

    return node;
  }

  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    String historyLevel = "audit";
    if (operation.hasDefined(ATTR_HISTORY_LEVEL)) {
      historyLevel = operation.get(ATTR_HISTORY_LEVEL).asString();
    }
    model.get(ATTR_HISTORY_LEVEL).set(historyLevel);
    
    String datasource = "java:jboss/datasources/ExampleDS";
    if (operation.hasDefined(ATTR_DATASOURCE)) {
      datasource = operation.get(ATTR_DATASOURCE).asString();
    }
    model.get(ATTR_DATASOURCE).set(datasource);
    
  }
  
  @Override
  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
          ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
          throws OperationFailedException {
    
    String engineName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();    
    String datasource = operation.get(ATTR_DATASOURCE).asString();   
    String historyLevel = operation.get(ATTR_HISTORY_LEVEL).asString();
    
    // TODO: read these values from config
    boolean activateJobExecutor=true;
    boolean isAutoUpdateSchema =true;
    int jobExecutor_maxJobsPerAcquisition =3;
    int jobExecutor_corePoolSize=1;
    int jobExecutor_maxPoolSize=3;
    int jobExecutor_queueSize=3;
    int jobExecutor_lockTimeInMillis= 5 * 60 * 1000;
    int jobExecutor_waitTimeInMillis = 5 * 1000;
    
    ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfigurationImpl(true, engineName, datasource, historyLevel, isAutoUpdateSchema, activateJobExecutor);        
    ProcessEngineControllerService service = new ProcessEngineControllerService(processEngineConfiguration);
        
    ServiceName name = ProcessEngineControllerService.createServiceName(engineName);    
    String datasourceJndiName = operation.get(ATTR_DATASOURCE).asString();    
    ContextNames.BindInfo datasourceBindInfo = ContextNames.bindInfoFor(datasourceJndiName);
    
    ServiceController<ProcessEngineControllerService> controller = context.getServiceTarget()           
            .addService(name, service)
            .addDependency(ServiceName.JBOSS.append("txn").append("TransactionManager"), TransactionManager.class, service.getTransactionManagerInjector())
            .addDependency(datasourceBindInfo.getBinderServiceName(), DataSourceReferenceFactoryService.class, service.getDatasourceBinderServiceInjector())
            .addDependency(ContainerPlatformService.getServiceName(), ContainerPlatformService.class, service.getContainerPlatformServiceInjector())
            .addListener(verificationHandler)
            .setInitialMode(Mode.ACTIVE)
            .install();
    
    newControllers.add(controller);
    
  }

}
