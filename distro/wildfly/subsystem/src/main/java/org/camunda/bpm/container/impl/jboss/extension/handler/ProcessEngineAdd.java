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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.jboss.config.ManagedProcessEngineMetadata;
import org.camunda.bpm.container.impl.jboss.extension.Element;
import org.camunda.bpm.container.impl.jboss.extension.SubsystemAttributeDefinitons;
import org.camunda.bpm.container.impl.jboss.service.MscManagedProcessEngineController;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;


/**
 * Provides the description and the implementation of the process-engine#add operation.
 *
 * @author Daniel Meyer
 */
public class ProcessEngineAdd extends AbstractAddStepHandler {

  public static final ProcessEngineAdd INSTANCE = new ProcessEngineAdd();

  protected ProcessEngineAdd() {
    super(SubsystemAttributeDefinitons.PROCESS_ENGINE_ATTRIBUTES);
  }

  @Override
  protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {

    String engineName = PathAddress.pathAddress(operation.get(ADDRESS)).getLastElement().getValue();

    ManagedProcessEngineMetadata processEngineConfiguration = transformConfiguration(context, engineName, model);

    installService(context, processEngineConfiguration);
  }

  protected void installService(OperationContext context, ManagedProcessEngineMetadata processEngineConfiguration) {

    MscManagedProcessEngineController service = new MscManagedProcessEngineController(processEngineConfiguration);
    ServiceName serviceName = ServiceNames.forManagedProcessEngine(processEngineConfiguration.getEngineName());

    ServiceBuilder<?> serviceBuilder = context.getServiceTarget().addService(serviceName);

    service.initializeServiceBuilder(processEngineConfiguration, serviceBuilder, serviceName, processEngineConfiguration.getJobExecutorAcquisitionName());

    serviceBuilder.setInstance(service);
    serviceBuilder.install();
  }

  protected ManagedProcessEngineMetadata transformConfiguration(final OperationContext context, String engineName, final ModelNode model) throws OperationFailedException {
    return new ManagedProcessEngineMetadata(
        SubsystemAttributeDefinitons.DEFAULT.resolveModelAttribute(context, model).asBoolean(),
        engineName,
        SubsystemAttributeDefinitons.DATASOURCE.resolveModelAttribute(context, model).asString(),
        SubsystemAttributeDefinitons.HISTORY_LEVEL.resolveModelAttribute(context, model).asString(),
        SubsystemAttributeDefinitons.CONFIGURATION.resolveModelAttribute(context, model).asString(),
        getProperties(SubsystemAttributeDefinitons.PROPERTIES.resolveModelAttribute(context, model)),
        getPlugins(SubsystemAttributeDefinitons.PLUGINS.resolveModelAttribute(context, model))
    );
  }


  protected List<ProcessEnginePluginXml> getPlugins(ModelNode plugins) {
    List<ProcessEnginePluginXml> pluginConfigurations =  new ArrayList<>();

    if (plugins.isDefined()) {
      for (final ModelNode plugin : plugins.asList()) {
        ProcessEnginePluginXml processEnginePluginXml = new ProcessEnginePluginXml() {
          @Override
          public String getPluginClass() {
            return plugin.get(Element.PLUGIN_CLASS.getLocalName()).asString();
          }

          @Override
          public Map<String, String> getProperties() {
            return ProcessEngineAdd.this.getProperties(plugin.get(Element.PROPERTIES.getLocalName()));
          }
        };

        pluginConfigurations.add(processEnginePluginXml);
      }
    }

    return pluginConfigurations;
  }

  protected Map<String, String> getProperties(ModelNode properties) {
    Map<String, String> propertyMap = new HashMap<>();
    if (properties.isDefined()) {
      for (Property property : properties.asPropertyList()) {
        propertyMap.put(property.getName(), property.getValue().asString());
      }
    }
    return propertyMap;
  }

}
