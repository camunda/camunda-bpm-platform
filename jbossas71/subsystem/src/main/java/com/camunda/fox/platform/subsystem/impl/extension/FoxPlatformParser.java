package com.camunda.fox.platform.subsystem.impl.extension;

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

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

import com.camunda.fox.platform.FoxPlatformException;


public class FoxPlatformParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {
  
  /** {@inheritDoc} */
  @Override
  public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
    // Require no attributes
    ParseUtils.requireNoAttributes(reader);

    //Add the main subsystem 'add' operation
    final ModelNode subsystemAddress = new ModelNode();
    subsystemAddress.add(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME);
    subsystemAddress.protect();
    
    final ModelNode subsystemAdd = new ModelNode();
    subsystemAdd.get(OP).set(ADD);
    subsystemAdd.get(OP_ADDR).set(subsystemAddress);
    list.add(subsystemAdd);
    
    int iterate;
    try {
      iterate = reader.nextTag();
    } catch (XMLStreamException e) {
      // founding a non tag..go on. Normally non-tag found at beginning are comments or DTD declaration
      iterate = reader.nextTag();
    }

    switch (iterate) {
      case END_ELEMENT: {
        // should mean we're done, so ignore it.
        break;
      }
      case START_ELEMENT: {
        switch (Element.forName(reader.getLocalName())) {
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
  
        break;
      }
      default: {
        throw new IllegalStateException();
      }
    }
  }

  private void parseProcessEngines(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.PROCESS_ENGINES.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Element.forName(reader.getLocalName()) == Element.PROCESS_ENGINES) {
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
          switch (Element.forName(reader.getLocalName())) {
            case PROCESS_ENGINE: {
              parseProcessEngine(reader, list, parentAddress);
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
  
  private void parseProcessEngine(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
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
            PathElement.pathElement(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME),
            PathElement.pathElement(Element.PROCESS_ENGINES.getLocalName(), engineName));
    addProcessEngine.get(OP_ADDR).set(addr.toModelNode());
 
    addProcessEngine.get(Attribute.NAME.getLocalName()).set(engineName);
    
    if(isDefault != null) {
      addProcessEngine.get(Attribute.DEFAULT.getLocalName()).set(isDefault);
    }
    
    checkIfProcessNameWithNameExists(list, engineName);
    
    list.add(addProcessEngine);
    
    // iterate deeper
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Element.forName(reader.getLocalName()) == Element.PROCESS_ENGINE) {
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
          switch (Element.forName(reader.getLocalName())) {
            case PROPERTIES: {
              parseProperties(reader, list, addProcessEngine);
              break;
            }
            case DATASOURCE: {
              parseElement(Element.DATASOURCE, reader, addProcessEngine);
              break;
            }
            case HISTORY_LEVEL: {
              parseElement(Element.HISTORY_LEVEL, reader, addProcessEngine);
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
  
  private void checkIfProcessNameWithNameExists(List<ModelNode> list, String engineName) {
    for (ModelNode modelNode : list) {
      if (modelNode.hasDefined(Attribute.NAME.getLocalName())) {
        String existingEngineName = modelNode.get(Attribute.NAME.getLocalName()).asString();
        if ((existingEngineName.equalsIgnoreCase(engineName))) {
          throw new FoxPlatformException("A process engine with name '" + engineName + "' already exists. Please chose another name.");
        }
      }
    }
  }

  private void parseProperties(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Element.PROPERTIES.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    requireNoAttributes(reader);
    
    ModelNode properties = new ModelNode();
    
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Element.forName(reader.getLocalName()) == Element.PROPERTIES) {
            // should mean we're done, so ignore it.
            parentAddress.get(Element.PROPERTIES.getLocalName()).set(properties);
            return;
          }
        }
        case START_ELEMENT: {
          switch (Element.forName(reader.getLocalName())) {
            case PROPERTY: {
              parseProperty(reader, list, properties);
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
    if (!Tag.JOB_EXECUTOR.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    String jobExecutorThreadPoolName = null;

    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attr = reader.getAttributeLocalName(i);
      if (Attribute.forName(attr).equals(Attribute.NAME)) {
        jobExecutorThreadPoolName = String.valueOf(reader.getAttributeValue(i));
      } else {
        throw unexpectedAttribute(reader, i);
      }
    }

    if (jobExecutorThreadPoolName.equals("null")) {
      throw missingRequiredElement(reader, Collections.singleton(Attribute.NAME.getLocalName()));
    }

    // Add the 'add' operation for each 'job-executor' child
    ModelNode addJobExecutor = new ModelNode();
    addJobExecutor.get(OP).set(ModelDescriptionConstants.ADD);
    PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME),
    PathElement.pathElement(Tag.JOB_EXECUTOR.getLocalName(), jobExecutorThreadPoolName));
    addJobExecutor.get(OP_ADDR).set(addr.toModelNode());

    list.add(addJobExecutor);
  
    // iterate deeper
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
      case END_ELEMENT: {
        if (Tag.forName(reader.getLocalName()) == Tag.JOB_EXECUTOR) {
          // should mean we're done, so ignore it.
          return;
        }
      }
      case START_ELEMENT: {
        switch (Tag.forName(reader.getLocalName())) {
          case JOB_AQUISITIONS: {
            parseJobAcquisitions(reader, list, addJobExecutor);
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
    if (!Tag.JOB_AQUISITIONS.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
//    ModelNode processEngines = new ModelNode();
//    processEngines.get(OP).set(ModelDescriptionConstants.ADD);
//    PathAddress addr = PathAddress.pathAddress(
//            PathElement.pathElement(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME),
//            PathElement.pathElement(Tag.PROCESS_ENGINES.getLocalName())); 
//    processEngines.get(OP_ADDR).set(addr.toModelNode());
//    
//    list.add(processEngines);
    
    
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Tag.forName(reader.getLocalName()) == Tag.JOB_AQUISITIONS) {
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
          switch (Tag.forName(reader.getLocalName())) {
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
    if (!Tag.JOB_AQUISITION.getLocalName().equals(reader.getLocalName())) {
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
    addJobAcquisition.get(OP).set(ModelDescriptionConstants.ADD);
    PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME),
    PathElement.pathElement(Tag.JOB_AQUISITIONS.getLocalName(), acquisitionName));
    addJobAcquisition.get(OP_ADDR).set(addr.toModelNode());

    list.add(addJobAcquisition);
  
    // iterate deeper
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
      case END_ELEMENT: {
        if (Tag.forName(reader.getLocalName()) == Tag.JOB_AQUISITION) {
          // should mean we're done, so ignore it.
          return;
        }
      }
      case START_ELEMENT: {
        switch (Tag.forName(reader.getLocalName())) {
        case PROPERTIES: {
          parseProperties(reader, list, addJobAcquisition);
          break;
        }
        case JOB_ACQUISITION_STRATEGY: {
          parseElement(Tag.JOB_ACQUISITION_STRATEGY, reader, addJobAcquisition);
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
  private void parseElement(Element element, XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
    if (!element.equals(Element.forName(reader.getLocalName()))) {
      throw unexpectedElement(reader);
    }
    
    parentAddress.get(reader.getLocalName()).set(rawElementText(reader));
  }
  
  /** {@inheritDoc} */
  @Override
  public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {

    context.startSubsystemElement(FoxPlatformExtension.NAMESPACE, false);

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
  
        writeProperties(writer, entry);
  
        writer.writeEndElement();
      }
    }
    // end process-engines
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