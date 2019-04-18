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
package org.camunda.bpm.container.impl.jboss.extension.handler;

import static org.camunda.bpm.container.impl.jboss.extension.ModelConstants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE_TYPE;

import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.extension.Element;
import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessEngineController;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.engine.ProcessEngine;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Provides the description and the implementation of the process-engine#add operation.
 *
 * @author Daniel Meyer
 */
public class ProcessEngineAdd extends AbstractAddStepHandler implements DescriptionProvider {

  public static final ProcessEngineAdd INSTANCE = new ProcessEngineAdd();

  public ModelNode getModelDescription(Locale locale) {
    ModelNode node = new ModelNode();
    node.get(DESCRIPTION).set("Adds a process engine");
    node.get(OPERATION_NAME).set(ADD);

    node.get(REQUEST_PROPERTIES, NAME, DESCRIPTION).set("Name of the process engine");
    node.get(REQUEST_PROPERTIES, NAME, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, NAME, REQUIRED).set(true);

    node.get(REQUEST_PROPERTIES, DATASOURCE, DESCRIPTION).set("Which datasource to use");
    node.get(REQUEST_PROPERTIES, DATASOURCE, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, DATASOURCE, REQUIRED).set(true);

    node.get(REQUEST_PROPERTIES, DEFAULT, DESCRIPTION).set("Should it be the default engine");
    node.get(REQUEST_PROPERTIES, DEFAULT, TYPE).set(ModelType.BOOLEAN);
    node.get(REQUEST_PROPERTIES, DEFAULT, REQUIRED).set(false);

    node.get(REQUEST_PROPERTIES, HISTORY_LEVEL, DESCRIPTION).set("Which history level to use");
    node.get(REQUEST_PROPERTIES, HISTORY_LEVEL, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, HISTORY_LEVEL, REQUIRED).set(false);

    // engine properties
    node.get(REQUEST_PROPERTIES, PROPERTIES, DESCRIPTION).set("Additional properties");
    node.get(REQUEST_PROPERTIES, PROPERTIES, TYPE).set(ModelType.OBJECT);
    node.get(REQUEST_PROPERTIES, PROPERTIES, VALUE_TYPE).set(ModelType.LIST);
    node.get(REQUEST_PROPERTIES, PROPERTIES, REQUIRED).set(false);

    node.get(REQUEST_PROPERTIES, CONFIGURATION, DESCRIPTION).set("Which configuration class to use");
    node.get(REQUEST_PROPERTIES, CONFIGURATION, TYPE).set(ModelType.STRING);
    node.get(REQUEST_PROPERTIES, CONFIGURATION, REQUIRED).set(false);

    // plugins
    node.get(REQUEST_PROPERTIES, PLUGINS, DESCRIPTION).set("Additional plugins for process engine");
    node.get(REQUEST_PROPERTIES, PLUGINS, TYPE).set(ModelType.LIST);
    node.get(REQUEST_PROPERTIES, PLUGINS, VALUE_TYPE).set(ModelType.OBJECT);
    node.get(REQUEST_PROPERTIES, PLUGINS, REQUIRED).set(false);

    return node;
  }

  @Override
  protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    for (AttributeDefinition attr : SubsystemAttributeDefinitons.PROCESS_ENGINE_ATTRIBUTES) {
      attr.validateAndSet(operation, model);
    }
  }

  @Override
  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
          ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
          throws OperationFailedException {

    String engineName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();

    ManagedProcessEngineMetadata processEngineConfiguration = transformConfiguration(context, engineName, model);

    ServiceController<ProcessEngine> controller = installService(context, verificationHandler, processEngineConfiguration);

    newControllers.add(controller);
  }

  protected ServiceController<ProcessEngine> installService(OperationContext context, ServiceVerificationHandler verificationHandler,
      ManagedProcessEngineMetadata processEngineConfiguration) {

    MscManagedProcessEngineController service = new MscManagedProcessEngineController(processEngineConfiguration);
    ServiceName name = ServiceNames.forManagedProcessEngine(processEngineConfiguration.getEngineName());

    ServiceBuilder<ProcessEngine> serviceBuilder = context.getServiceTarget().addService(name, service);

    MscManagedProcessEngineController.initializeServiceBuilder(processEngineConfiguration, service, serviceBuilder, processEngineConfiguration.getJobExecutorAcquisitionName());

    serviceBuilder.addListener(verificationHandler);
    return serviceBuilder.install();
  }

  protected ManagedProcessEngineMetadata transformConfiguration(final OperationContext context, String engineName, final ModelNode model) throws IllegalArgumentException, OperationFailedException {
    return new ManagedProcessEngineMetadata(
        SubsystemAttributeDefinitons.DEFAULT.resolveModelAttribute(context, model).asBoolean(),
        engineName,
        SubsystemAttributeDefinitons.DATASOURCE.resolveModelAttribute(context, model).asString(),
        SubsystemAttributeDefinitons.HISTORY_LEVEL.resolveModelAttribute(context, model).asString(),
        SubsystemAttributeDefinitons.CONFIGURATION.resolveModelAttribute(context, model).asString(),
        getPropertiesMap(SubsystemAttributeDefinitons.PROPERTIES.resolveModelAttribute(context, model)),
        getPlugins(SubsystemAttributeDefinitons.PLUGINS.resolveModelAttribute(context, model)));
  }

  protected List<ProcessEnginePluginXml> getPlugins(final ModelNode plugins) {
    List<ProcessEnginePluginXml> pluginConfigurations =  new ArrayList<ProcessEnginePluginXml>();

    if (plugins.isDefined()) {
      for (final ModelNode plugin : plugins.asList()) {
        ProcessEnginePluginXml processEnginePluginXml = new ProcessEnginePluginXml() {
          @Override
          public String getPluginClass() {
            return plugin.get(Element.PLUGIN_CLASS.getLocalName()).asString();
          }

          @Override
          public Map<String, String> getProperties() {
            return ProcessEngineAdd.this.getPropertiesMap(plugin.get(Element.PROPERTIES.getLocalName()));
          }
        };

        pluginConfigurations.add(processEnginePluginXml);
      }
    }

    return pluginConfigurations;
  }

  protected Map<String, String> getPropertiesMap(ModelNode properties) {
    Map<String, String> propertyMap = new HashMap<String, String>();
    if (properties.isDefined()) {
      for (Property property : properties.asPropertyList()) {
        propertyMap.put(property.getName(), property.getValue().asString());
      }
    }
    return propertyMap;
  }

}
