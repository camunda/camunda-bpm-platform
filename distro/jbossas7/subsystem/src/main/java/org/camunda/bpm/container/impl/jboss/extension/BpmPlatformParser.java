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
package org.camunda.bpm.container.impl.jboss.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequiredElement;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireSingleAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.camunda.bpm.engine.ProcessEngineException;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;


public class BpmPlatformParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

  public static final boolean REQUIRED = true;
  public static final boolean NOT_REQUIRED = false;

  /** {@inheritDoc} */
  @Override
  public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
    // Require no attributes
    ParseUtils.requireNoAttributes(reader);

    //Add the main subsystem 'add' operation
    final ModelNode subsystemAddress = new ModelNode();
    subsystemAddress.add(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME);
    subsystemAddress.protect();
    
    final ModelNode subsystemAdd = new ModelNode();
    subsystemAdd.get(OP).set(ADD);
    subsystemAdd.get(OP_ADDR).set(subsystemAddress);
    list.add(subsystemAdd);
    
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
    final Element element = Element.forName(reader.getLocalName());
    switch (element) {
          case PROCESS_ENGINES: {
            parseProcessEngines(reader, list, subsystemAddress);
            break;
          }
          case JOB_EXECUTOR: {
            parseJobExecutor(reader, list, subsystemAddress);		  
            break;
          }
          default: {
            throw unexpectedElement(reader);
          }
        }
    }
  }

  private void parseProcessEngines(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.PROCESS_ENGINES.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    List<String> discoveredEngineNames = new ArrayList<String>();
    
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PROCESS_ENGINE: {
          parseProcessEngine(reader, list, parentAddress, discoveredEngineNames);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }
  
  private void parseProcessEngine(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress, List<String> discoveredEngineNames) throws XMLStreamException {
    if (!Element.PROCESS_ENGINE.getLocalName().equals(reader.getLocalName())) {
        throw unexpectedElement(reader);
    }
    
    ModelNode addProcessEngine = new ModelNode();
    String engineName = null;
    
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attr = reader.getAttributeLocalName(i);
      if (Attribute.forName(attr).equals(Attribute.NAME)) {
        engineName = String.valueOf(reader.getAttributeValue(i));
      } else if (Attribute.forName(attr).equals(Attribute.DEFAULT)) {
        SubsystemAttributeDefinitons.DEFAULT.parseAndSetParameter(reader.getAttributeValue(i), addProcessEngine, reader);;
      } else {
        throw unexpectedAttribute(reader, i);
      }
    }
    
    if ("null".equals(engineName)) {
      throw missingRequiredElement(reader, Collections.singleton(Attribute.NAME.getLocalName()));
    }
    
    //Add the 'add' operation for each 'process-engine' child
    addProcessEngine.get(OP).set(ModelDescriptionConstants.ADD);
    PathAddress addr = PathAddress.pathAddress(
            PathElement.pathElement(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME),
            PathElement.pathElement(Element.PROCESS_ENGINES.getLocalName(), engineName));
    addProcessEngine.get(OP_ADDR).set(addr.toModelNode());
 
    addProcessEngine.get(Attribute.NAME.getLocalName()).set(engineName);
    
    if (discoveredEngineNames.contains(engineName)) {
      throw new ProcessEngineException("A process engine with name '" + engineName + "' already exists. The process engine name must be unique.");
    } else {
      discoveredEngineNames.add(engineName);
    }
    
    list.add(addProcessEngine);
    
    // iterate deeper
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PLUGINS: {
          parsePlugins(reader, list, addProcessEngine);
          break;
        }
        case PROPERTIES: {
          parseProperties(reader, list, addProcessEngine);
          break;
        }
        case DATASOURCE: {
          parseElement(SubsystemAttributeDefinitons.DATASOURCE, reader, addProcessEngine);
          break;
        }
        case HISTORY_LEVEL: {
          parseElement(SubsystemAttributeDefinitons.HISTORY_LEVEL, reader, addProcessEngine);
          break;
        }
        case CONFIGURATION: {
          parseElement(SubsystemAttributeDefinitons.CONFIGURATION, reader, addProcessEngine);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }

  private void parsePlugins(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode addProcessEngine) throws XMLStreamException {
    if (!Element.PLUGINS.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }

    requireNoAttributes(reader);

    ModelNode plugins = new ModelNode();

    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PLUGIN: {
          parsePlugin(reader, list, plugins);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }

    addProcessEngine.get(Element.PLUGINS.getLocalName()).set(plugins);
  }

  private void parsePlugin(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode plugins) throws XMLStreamException {
    if (!Element.PLUGIN.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }

    requireNoAttributes(reader);
    ModelNode plugin = new ModelNode();

    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PLUGIN_CLASS: {
          parseElement(SubsystemAttributeDefinitons.PLUGIN_CLASS, reader, plugin);
          break;
        }
        case PROPERTIES: {
          parseProperties(reader, list, plugin);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }

    plugins.add(plugin);
  }

  private void parseProperties(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.PROPERTIES.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    requireNoAttributes(reader);
    
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PROPERTY: {
          parseProperty(reader, list, parentAddress);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }

  private void parseProperty(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    requireSingleAttribute(reader, Attribute.NAME.getLocalName());
    String name = reader.getAttributeValue(0);
    String value = rawElementText(reader);
      
    if (name == null) {
      throw missingRequired(reader, Collections.singleton(Attribute.NAME));
    }

    SubsystemAttributeDefinitons.PROPERTIES.parseAndAddParameterElement(name, value, parentAddress, reader);
  }

  private void parseJobExecutor(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.JOB_EXECUTOR.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    // Add the 'add' operation for 'job-executor' parent
    ModelNode addJobExecutor = new ModelNode();
    addJobExecutor.get(OP).set(ModelDescriptionConstants.ADD);
    PathAddress addr = PathAddress.pathAddress(
              PathElement.pathElement(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME),
              PathElement.pathElement(Element.JOB_EXECUTOR.getLocalName(), ModelConstants.DEFAULT));
    addJobExecutor.get(OP_ADDR).set(addr.toModelNode());
    
    list.add(addJobExecutor);
  
    // iterate deeper
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
      case END_ELEMENT: {
        if (Element.forName(reader.getLocalName()) == Element.JOB_EXECUTOR) {
          // should mean we're done, so ignore it.
          return;
        }
      }
      case START_ELEMENT: {
        switch (Element.forName(reader.getLocalName())) {
          case JOB_AQUISITIONS: {
            parseJobAcquisitions(reader, list, addJobExecutor);
            break;
          }
          case THREAD_POOL_NAME: {
            parseElement(SubsystemAttributeDefinitons.THREAD_POOL_NAME, reader, addJobExecutor);
            break;
          }
          default: {
            throw unexpectedElement(reader);
          }
        }
        break;
      }
     }
    }
  }

  private void parseJobAcquisitions(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.JOB_AQUISITIONS.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Element.forName(reader.getLocalName()) == Element.JOB_AQUISITIONS) {
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
          switch (Element.forName(reader.getLocalName())) {
            case JOB_AQUISITION: {
              parseJobAcquisition(reader, list, parentAddress);
              break;
            }
            default: {
              throw unexpectedElement(reader);
            }
          }
          break;
        }
      }
    }
  }

  private void parseJobAcquisition(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.JOB_AQUISITION.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }

    String acquisitionName = null;

    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attr = reader.getAttributeLocalName(i);
      if (Attribute.forName(attr).equals(Attribute.NAME)) {
        acquisitionName = String.valueOf(reader.getAttributeValue(i));
      } else {
        throw unexpectedAttribute(reader, i);
      }
    }

    if ("null".equals(acquisitionName)) {
      throw missingRequiredElement(reader, Collections.singleton(Attribute.NAME.getLocalName()));
    }

    // Add the 'add' operation for each 'job-acquisition' child
    ModelNode addJobAcquisition = new ModelNode();
    addJobAcquisition.get(OP).set(ADD);
    PathAddress addr = PathAddress.pathAddress(
            PathElement.pathElement(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME),
            PathElement.pathElement(Element.JOB_EXECUTOR.getLocalName(), ModelConstants.DEFAULT),
            PathElement.pathElement(Element.JOB_AQUISITIONS.getLocalName(), acquisitionName));
    addJobAcquisition.get(OP_ADDR).set(addr.toModelNode());
    
    addJobAcquisition.get(Attribute.NAME.getLocalName()).set(acquisitionName);

    list.add(addJobAcquisition);
    
    // iterate deeper
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Element.forName(reader.getLocalName()) == Element.JOB_AQUISITION) {
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
          switch (Element.forName(reader.getLocalName())) {
            case PROPERTIES: {
              parseProperties(reader, list, addJobAcquisition);
              break;
            }
            case ACQUISITION_STRATEGY: {
              parseElement(SubsystemAttributeDefinitons.ACQUISITION_STRATEGY, reader, addJobAcquisition);
              break;
            }
            default: {
              throw unexpectedElement(reader);
            }
          }
          break;
        }
      }
    }
  }  
  private void parseElement(AttributeDefinition element, XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
    String value = rawElementText(reader);
    ((SimpleAttributeDefinition) element).parseAndSetParameter(value, parentAddress, reader);
  }
  
  /** {@inheritDoc} */
  @Override
  public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {

    context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
    
    writeProcessEnginesContent(writer, context);
    
    writeJobExecutorContent(writer, context);
    
    // end subsystem
    writer.writeEndElement();
  }
  
  protected void writeProcessEnginesContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
    
    writer.writeStartElement(Element.PROCESS_ENGINES.getLocalName());

    ModelNode node = context.getModelNode();
    
    ModelNode processEngineConfigurations = node.get(Element.PROCESS_ENGINES.getLocalName());
    if (processEngineConfigurations.isDefined()) {
      for (Property property : processEngineConfigurations.asPropertyList()) {
        // write each child element to xml
        writer.writeStartElement(Element.PROCESS_ENGINE.getLocalName());
        
        ModelNode propertyValue = property.getValue();
        for (AttributeDefinition processEngineAttribute : SubsystemAttributeDefinitons.PROCESS_ENGINE_ATTRIBUTES) {
          if (processEngineAttribute.equals(SubsystemAttributeDefinitons.NAME) || processEngineAttribute.equals(SubsystemAttributeDefinitons.DEFAULT)) {
            ((SimpleAttributeDefinition) processEngineAttribute).marshallAsAttribute(propertyValue, writer);
          } else {
            processEngineAttribute.marshallAsElement(propertyValue, writer);
          }
        }

  
        writer.writeEndElement();
      }
    }
    // end process-engines
    writer.writeEndElement();
  }
  
  protected void writeJobExecutorContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
    ModelNode node = context.getModelNode();
    ModelNode jobExecutorNode = node.get(Element.JOB_EXECUTOR.getLocalName());
    
    if (jobExecutorNode.isDefined()) { 
      writer.writeStartElement(Element.JOB_EXECUTOR.getLocalName());
      
      for (Property property : jobExecutorNode.asPropertyList()) {
        ModelNode propertyValue = property.getValue();

        for (AttributeDefinition jobExecutorAttribute : SubsystemAttributeDefinitons.JOB_EXECUTOR_ATTRIBUTES) {
          if (jobExecutorAttribute.equals(SubsystemAttributeDefinitons.NAME)) {
            ((SimpleAttributeDefinition) jobExecutorAttribute).marshallAsAttribute(propertyValue, writer);
          } else {
            jobExecutorAttribute.marshallAsElement(propertyValue, writer);
          }
        }

        writeJobAcquisitionsContent(writer, context, propertyValue);
      }

      // end job-executor
      writer.writeEndElement();
    }
  }
  
  protected void writeJobAcquisitionsContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context, ModelNode parentNode) throws XMLStreamException {
    writer.writeStartElement(Element.JOB_AQUISITIONS.getLocalName());

    ModelNode jobAcquisitionConfigurations = parentNode.get(Element.JOB_AQUISITIONS.getLocalName());
    if (jobAcquisitionConfigurations.isDefined()) {
      for (Property property : jobAcquisitionConfigurations.asPropertyList()) {
        // write each child element to xml
        writer.writeStartElement(Element.JOB_AQUISITION.getLocalName());

        for (AttributeDefinition jobAcquisitionAttribute : SubsystemAttributeDefinitons.JOB_ACQUISITION_ATTRIBUTES) {
          if (jobAcquisitionAttribute.equals(SubsystemAttributeDefinitons.NAME)) {
            ((SimpleAttributeDefinition) jobAcquisitionAttribute).marshallAsAttribute(property.getValue(), writer);
          } else {
            jobAcquisitionAttribute.marshallAsElement(property.getValue(), writer);
          }
        }

        writer.writeEndElement();
      }
    }
    // end job-acquisitions
    writer.writeEndElement();
  }

  /**
   * Reads and trims the element text and returns it or {@code null}
   *
   * @param reader  source for the element text
   * @return the string representing the trimmed element text or {@code null} if there is none or it is an empty string
   * @throws XMLStreamException
   */
  public String rawElementText(XMLStreamReader reader) throws XMLStreamException {
    String elementText = reader.getElementText();
    elementText = elementText == null || elementText.trim().length() == 0 ? null : elementText.trim();
    return elementText;
  }

}
