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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.jboss.dmr.Property;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformParser.Attribute;
import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformParser.Tag;
import com.camunda.fox.platform.subsystem.impl.platform.ContainerJobExecutorService;
import com.camunda.fox.platform.subsystem.impl.platform.ContainerPlatformService;
import com.camunda.fox.platform.subsystem.impl.platform.ProcessEngineConfigurationImpl;
import com.camunda.fox.platform.subsystem.impl.platform.ProcessEngineControllerService;

/**
 * Provides the description and the implementation of the process-engine#add operation.
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineAddHandler extends AbstractAddStepHandler implements DescriptionProvider {
    
  public static final ProcessEngineAddHandler INSTANCE = new ProcessEngineAddHandler();

  public ModelNode getModelDescription(Locale locale) {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Adds a process engine");
    node.get(OPERATION_NAME).set(ADD);
    
    node.get(REQUEST_PROPERTIES, Attribute.DEFAULT.getLocalName(), DESCRIPTION).set("Should it be the default engine");
    node.get(REQUEST_PROPERTIES, Attribute.DEFAULT.getLocalName(), TYPE).set(ModelType.BOOLEAN);
    node.get(REQUEST_PROPERTIES, Attribute.DEFAULT.getLocalName(), REQUIRED).set(false);
    
    node.get(REQUEST_PROPERTIES, Tag.DATASOURCE.getLocalName(), DESCRIPTION).set("Which datasource to use");
    node.get(REQUEST_PROPERTIES, Tag.DATASOURCE.getLocalName(), TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, Tag.DATASOURCE.getLocalName(), REQUIRED).set(true);
    
    node.get(REQUEST_PROPERTIES, Tag.HISTORY_LEVEL.getLocalName(), DESCRIPTION).set("Which history level to use");
    node.get(REQUEST_PROPERTIES, Tag.HISTORY_LEVEL.getLocalName(), TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, Tag.HISTORY_LEVEL.getLocalName(), REQUIRED).set(false);
    
    node.get(REQUEST_PROPERTIES, Tag.PROPERTIES.getLocalName(), DESCRIPTION).set("Additional properties");
    node.get(REQUEST_PROPERTIES, Tag.PROPERTIES.getLocalName(), TYPE).set(ModelType.LIST);
    node.get(REQUEST_PROPERTIES, Tag.PROPERTIES.getLocalName(), VALUE_TYPE).set(ModelType.PROPERTY);
    node.get(REQUEST_PROPERTIES, Tag.PROPERTIES.getLocalName(), REQUIRED).set(false);

    return node;
  }

  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    Boolean isDefault = Boolean.TRUE;
    if (operation.hasDefined(Attribute.DEFAULT.getLocalName())) {
      isDefault = operation.get(Attribute.DEFAULT.getLocalName()).asBoolean();
    }
    model.get(Attribute.DEFAULT.getLocalName()).set(isDefault);
    
    String historyLevel = "audit";
    if (operation.hasDefined(Tag.HISTORY_LEVEL.getLocalName())) {
      historyLevel = operation.get(Tag.HISTORY_LEVEL.getLocalName()).asString();
    }
    model.get(Tag.HISTORY_LEVEL.getLocalName()).set(historyLevel);
    
    String datasource = "java:jboss/datasources/ExampleDS";
    if (operation.hasDefined(Tag.DATASOURCE.getLocalName())) {
      datasource = operation.get(Tag.DATASOURCE.getLocalName()).asString();
    }
    model.get(Tag.DATASOURCE.getLocalName()).set(datasource);
    
    // retrieve all properties
    List<ModelNode> properties = null;
    if (operation.hasDefined(Tag.PROPERTIES.getLocalName())) {
      properties = operation.get(Tag.PROPERTIES.getLocalName()).asList();
    }
    model.get(Tag.PROPERTIES.getLocalName()).set(properties);
  }
  
  @Override
  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
          ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
          throws OperationFailedException {
    
    String engineName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
    String datasourceJndiName = operation.get(Tag.DATASOURCE.getLocalName()).asString();   
    String historyLevel = operation.get(Tag.HISTORY_LEVEL.getLocalName()).asString();
    boolean isDefault = operation.get(Attribute.DEFAULT.getLocalName()).asBoolean();
    
    List<Property> propertyList = operation.get(Tag.PROPERTIES.getLocalName()).asPropertyList();
    Map<String,Object> properties = null;
    if (!propertyList.isEmpty()) {
      properties = new HashMap<String, Object>();
      for (Property property : propertyList) {
        properties.put(property.getName(), property.getValue().asString());
      }
    }
    
    // TODO: read these values from config
    boolean activateJobExecutor=true;
    boolean isAutoUpdateSchema =true;
    int jobExecutor_maxJobsPerAcquisition =3;
    int jobExecutor_corePoolSize=1;
    int jobExecutor_maxPoolSize=3;
    int jobExecutor_queueSize=3;
    int jobExecutor_lockTimeInMillis= 5 * 60 * 1000;
    int jobExecutor_waitTimeInMillis = 5 * 1000;
    
    ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfigurationImpl(isDefault, engineName, datasourceJndiName, historyLevel, isAutoUpdateSchema, activateJobExecutor);
    processEngineConfiguration.getProperties().putAll(properties);
    ProcessEngineControllerService service = new ProcessEngineControllerService(processEngineConfiguration);
        
    ServiceName name = ProcessEngineControllerService.createServiceName(engineName);    
    ContextNames.BindInfo datasourceBindInfo = ContextNames.bindInfoFor(datasourceJndiName);
    
    ServiceController<ProcessEngineControllerService> controller = context.getServiceTarget()           
            .addService(name, service)
            .addDependency(ServiceName.JBOSS.append("txn").append("TransactionManager"), TransactionManager.class, service.getTransactionManagerInjector())
            .addDependency(datasourceBindInfo.getBinderServiceName(), DataSourceReferenceFactoryService.class, service.getDatasourceBinderServiceInjector())
            .addDependency(ContainerPlatformService.getServiceName(), ContainerPlatformService.class, service.getContainerPlatformServiceInjector())
            .addDependency(ContainerJobExecutorService.getServiceName(), ContainerJobExecutorService.class, service.getContainerJobExecutorInjector())
            .addListener(verificationHandler)
            .setInitialMode(Mode.ACTIVE)
            .install();
    
    newControllers.add(controller);
    
  }

}
