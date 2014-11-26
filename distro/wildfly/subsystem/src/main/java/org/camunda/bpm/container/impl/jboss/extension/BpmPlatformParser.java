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
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
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
    
    String engineName = null;
    Boolean isDefault = null;
    
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attr = reader.getAttributeLocalName(i);
      if (Attribute.forName(attr).equals(Attribute.NAME)) {
        engineName = String.valueOf(reader.getAttributeValue(i));
      } else if (Attribute.forName(attr).equals(Attribute.DEFAULT)) {
        isDefault = Boolean.valueOf(reader.getAttributeValue(i));
      } else {
        throw unexpectedAttribute(reader, i);
      }
    }
    
    if (engineName.equals("null")) {
      throw missingRequiredElement(reader, Collections.singleton(Attribute.NAME.getLocalName()));
    }
    
    //Add the 'add' operation for each 'process-engine' child
    ModelNode addProcessEngine = new ModelNode();
    addProcessEngine.get(OP).set(ModelDescriptionConstants.ADD);
    PathAddress addr = PathAddress.pathAddress(
            PathElement.pathElement(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME),
            PathElement.pathElement(Element.PROCESS_ENGINES.getLocalName(), engineName));
    addProcessEngine.get(OP_ADDR).set(addr.toModelNode());
 
    addProcessEngine.get(Attribute.NAME.getLocalName()).set(engineName);
    
    if(isDefault != null) {
      addProcessEngine.get(Attribute.DEFAULT.getLocalName()).set(isDefault);
    }
    
    if(discoveredEngineNames.contains(engineName)) {
      throw new ProcessEngineException("A process engine with name '"+engineName+"' already exists. The process engine name must be unique.");
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
          parseElement(Element.DATASOURCE, reader, addProcessEngine, REQUIRED);
          break;
        }
        case HISTORY_LEVEL: {
          parseElement(Element.HISTORY_LEVEL, reader, addProcessEngine, NOT_REQUIRED);
          break;
        }
        case CONFIGURATION: {
          parseElement(Element.CONFIGURATION, reader, addProcessEngine, NOT_REQUIRED);
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
          parseElement(Element.PLUGIN_CLASS, reader, plugin, REQUIRED);
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
    
    ModelNode properties = new ModelNode();
    
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PROPERTY: {
          parseProperty(reader, list, properties);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
    
    parentAddress.get(Element.PROPERTIES.getLocalName()).set(properties);
  }

  private void parseProperty(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    requireSingleAttribute(reader, Attribute.NAME.getLocalName());
    String name = reader.getAttributeValue(0);
    String value = rawElementText(reader);
      
    if (name == null) {
      throw missingRequired(reader, Collections.singleton(Attribute.NAME));
    }

    parentAddress.get(name).set(value);
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
            parseElement(Element.THREAD_POOL_NAME, reader, addJobExecutor, REQUIRED);
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

    if (acquisitionName.equals("null")) {
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
              parseElement(Element.ACQUISITION_STRATEGY, reader, addJobAcquisition, REQUIRED);
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
  private void parseElement(Element element, XMLExtendedStreamReader reader, ModelNode parentAddress, boolean required) throws XMLStreamException {
    if (!element.equals(Element.forName(reader.getLocalName()))) {
      throw unexpectedElement(reader);
    }

    String elementName = reader.getLocalName();
    String elementValue = rawElementText(reader);
    if (elementValue == null && required) {
      throw missingRequiredElement(reader, Collections.singleton(element.getLocalName()));
    }

    parentAddress.get(elementName).set(elementValue);
  }
  
  /** {@inheritDoc} */
  @Override
  public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {

    context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
    
    writeProcessEnginesContent(writer, context);
    
    writeJobExecutorContent(writer, context);
    
    // end subsystem
    writer.writeEndElement();
  }
  
  private void writeProcessEnginesContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
    
    writer.writeStartElement(Element.PROCESS_ENGINES.getLocalName());

    ModelNode node = context.getModelNode();
    
    ModelNode processEngineConfigurations = node.get(Element.PROCESS_ENGINES.getLocalName());
    if (processEngineConfigurations.isDefined()) {
      for (Property property : processEngineConfigurations.asPropertyList()) {
        // write each child element to xml
        writer.writeStartElement(Element.PROCESS_ENGINE.getLocalName());
        
        writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
        ModelNode entry = property.getValue();
        writeAttribute(Attribute.DEFAULT, writer, entry);
        writeElement(Element.DATASOURCE, writer, entry);
        writeElement(Element.HISTORY_LEVEL, writer, entry);
        writeElement(Element.CONFIGURATION, writer, entry);
  
        writeProperties(writer, entry);
        writePlugins(writer, entry);
  
        writer.writeEndElement();
      }
    }
    // end process-engines
    writer.writeEndElement();
  }
  
  private void writeJobExecutorContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
    ModelNode node = context.getModelNode();
    ModelNode jobExecutorNode = node.get(Element.JOB_EXECUTOR.getLocalName());
    
    if (jobExecutorNode.isDefined()) { 
      writer.writeStartElement(Element.JOB_EXECUTOR.getLocalName());
      
      Property property = jobExecutorNode.asProperty();
      writeElement(Element.THREAD_POOL_NAME, writer, property.getValue());
      
      writeJobAcquisitionsContent(writer, context, property.getValue());
      
      // end job-executor
      writer.writeEndElement();
    }
  }
  
  private void writeJobAcquisitionsContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context, ModelNode parentNode) throws XMLStreamException {
    writer.writeStartElement(Element.JOB_AQUISITIONS.getLocalName());

    ModelNode jobAcquisitionConfigurations = parentNode.get(Element.JOB_AQUISITIONS.getLocalName());
    if (jobAcquisitionConfigurations.isDefined()) {
      for (Property property : jobAcquisitionConfigurations.asPropertyList()) {
        // write each child element to xml
        writer.writeStartElement(Element.JOB_AQUISITION.getLocalName());
        
        writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
        ModelNode entry = property.getValue();
  
        writeElement(Element.ACQUISITION_STRATEGY, writer, entry);
        
        writeProperties(writer, entry);
  
        writer.writeEndElement();
      }
    }
    // end job-acquisitions
    writer.writeEndElement();
  }

  private void writeAttribute(Attribute attribute, final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
    if (entry.hasDefined(attribute.getLocalName())) {
      writer.writeAttribute(attribute.getLocalName(), entry.get(attribute.getLocalName()).asString());
    }
  }

  private void writeElement(Element element, final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
    if (entry.hasDefined(element.getLocalName())) {
      writer.writeStartElement(element.getLocalName());
      writer.writeCharacters(entry.get(element.getLocalName()).asString());
      writer.writeEndElement();
    }
  }
  
  private void writeProperties(final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
    if (entry.hasDefined(Element.PROPERTIES.getLocalName())) {
      writer.writeStartElement(Element.PROPERTIES.getLocalName());
      
      List<Property> propertyList = entry.get(Element.PROPERTIES.getLocalName()).asPropertyList();
      if (!propertyList.isEmpty()) {
        for (Property property : propertyList) {
          writer.writeStartElement(Element.PROPERTY.getLocalName());
          writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
          writer.writeCharacters(property.getValue().asString());
          writer.writeEndElement();
        }
      }
      
      writer.writeEndElement();
    }
  }

  private void writePlugins(final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
    if (entry.hasDefined(Element.PLUGINS.getLocalName())) {
      writer.writeStartElement(Element.PLUGINS.getLocalName());

      List<ModelNode> plugins = entry.get(Element.PLUGINS.getLocalName()).asList();
      if (!plugins.isEmpty()) {
        for (ModelNode plugin : plugins) {
          writePlugin(writer, plugin);
        }
      }

      writer.writeEndElement();
    }
  }

  public void writePlugin(final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
    writer.writeStartElement(Element.PLUGIN.getLocalName());

    writeElement(Element.PLUGIN_CLASS, writer, entry);

    writeProperties(writer, entry);
    writer.writeEndElement();
  }

  /**
   * FIXME Comment this
   *
   * @param reader
   * @return the string representing the raw element text
   * @throws XMLStreamException
   */
  public String rawElementText(XMLStreamReader reader) throws XMLStreamException {
    String elementText = reader.getElementText();
    elementText = elementText == null || elementText.trim().length() == 0 ? null : elementText.trim();
    return elementText;
  }

  /**
   * FIXME Comment this
   *
   * @param reader
   * @param attributeName
   * @return the string representing raw attribute text
   */
  public String rawAttributeText(XMLStreamReader reader, String attributeName) {
    String attributeString = 
            reader.getAttributeValue("", attributeName) == null ? null : reader.getAttributeValue("", attributeName).trim();
    return attributeString;
  }
}
