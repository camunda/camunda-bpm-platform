package com.camunda.fox.platform.subsystem.impl.extension;

<<<<<<< HEAD
=======
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
>>>>>>> platform-ce/master
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
<<<<<<< HEAD
import java.util.HashMap;
import java.util.List;
import java.util.Map;
=======
import java.util.List;
>>>>>>> platform-ce/master

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
<<<<<<< HEAD
=======
import org.jboss.as.controller.parsing.ParseUtils;
>>>>>>> platform-ce/master
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

<<<<<<< HEAD
=======
import com.camunda.fox.platform.FoxPlatformException;

>>>>>>> platform-ce/master

public class FoxPlatformParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {
  
  /** {@inheritDoc} */
  @Override
  public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
    // Require no attributes
<<<<<<< HEAD
//    ParseUtils.requireNoAttributes(reader);

    //Add the main subsystem 'add' operation
    ModelNode subsystemAddress = FoxPlatformExtension.createAddSubsystemOperation();
    list.add(subsystemAddress);

=======
    ParseUtils.requireNoAttributes(reader);

    //Add the main subsystem 'add' operation
    final ModelNode subsystemAddress = new ModelNode();
    subsystemAddress.add(SUBSYSTEM, FoxPlatformExtension.SUBSYSTEM_NAME);
    subsystemAddress.protect();
    
    final ModelNode subsystemAdd = new ModelNode();
    subsystemAdd.get(OP).set(ADD);
    subsystemAdd.get(OP_ADDR).set(subsystemAddress);
    list.add(subsystemAdd);
    
>>>>>>> platform-ce/master
    int iterate;
    try {
      iterate = reader.nextTag();
    } catch (XMLStreamException e) {
<<<<<<< HEAD
      // founding a non tag..go on. Normally non-tag found at beginning are
      // comments or DTD declaration
=======
      // founding a non tag..go on. Normally non-tag found at beginning are comments or DTD declaration
>>>>>>> platform-ce/master
      iterate = reader.nextTag();
    }

    switch (iterate) {
      case END_ELEMENT: {
        // should mean we're done, so ignore it.
        break;
      }
      case START_ELEMENT: {
<<<<<<< HEAD
        switch (Tag.forName(reader.getLocalName())) {
=======
        switch (Element.forName(reader.getLocalName())) {
>>>>>>> platform-ce/master
          case PROCESS_ENGINES: {
            parseProcessEngines(reader, list, subsystemAddress);
            break;
          }
          case JOB_EXECUTOR: {
<<<<<<< HEAD
            parseJobExecutor(reader, list, subsystemAddress);
=======
>>>>>>> platform-ce/master
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
<<<<<<< HEAD
    if (!Tag.PROCESS_ENGINES.getLocalName().equals(reader.getLocalName())) {
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
          if (Tag.forName(reader.getLocalName()) == Tag.PROCESS_ENGINES) {
=======
    if (!Element.PROCESS_ENGINES.getLocalName().equals(reader.getLocalName())) {
      throw unexpectedElement(reader);
    }
    
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
          if (Element.forName(reader.getLocalName()) == Element.PROCESS_ENGINES) {
>>>>>>> platform-ce/master
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
<<<<<<< HEAD
          switch (Tag.forName(reader.getLocalName())) {
=======
          switch (Element.forName(reader.getLocalName())) {
>>>>>>> platform-ce/master
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
<<<<<<< HEAD
    if (!Tag.PROCESS_ENGINE.getLocalName().equals(reader.getLocalName())) {
=======
    if (!Element.PROCESS_ENGINE.getLocalName().equals(reader.getLocalName())) {
>>>>>>> platform-ce/master
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
<<<<<<< HEAD
//            PathElement.pathElement(Tag.PROCESS_ENGINES.getLocalName()),
            PathElement.pathElement(Tag.PROCESS_ENGINE.getLocalName(), engineName));
    addProcessEngine.get(OP_ADDR).set(addr.toModelNode());
 
=======
            PathElement.pathElement(Element.PROCESS_ENGINES.getLocalName(), engineName));
    addProcessEngine.get(OP_ADDR).set(addr.toModelNode());
 
    addProcessEngine.get(Attribute.NAME.getLocalName()).set(engineName);
    
>>>>>>> platform-ce/master
    if(isDefault != null) {
      addProcessEngine.get(Attribute.DEFAULT.getLocalName()).set(isDefault);
    }
    
<<<<<<< HEAD
=======
    checkIfProcessNameWithNameExists(list, engineName);
    
>>>>>>> platform-ce/master
    list.add(addProcessEngine);
    
    // iterate deeper
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
<<<<<<< HEAD
          if (Tag.forName(reader.getLocalName()) == Tag.PROCESS_ENGINE) {
=======
          if (Element.forName(reader.getLocalName()) == Element.PROCESS_ENGINE) {
>>>>>>> platform-ce/master
            // should mean we're done, so ignore it.
            return;
          }
        }
        case START_ELEMENT: {
<<<<<<< HEAD
          switch (Tag.forName(reader.getLocalName())) {
=======
          switch (Element.forName(reader.getLocalName())) {
>>>>>>> platform-ce/master
            case PROPERTIES: {
              parseProperties(reader, list, addProcessEngine);
              break;
            }
            case DATASOURCE: {
<<<<<<< HEAD
              parseElement(Tag.DATASOURCE, reader, addProcessEngine);
              break;
            }
            case HISTORY_LEVEL: {
              parseElement(Tag.HISTORY_LEVEL, reader, addProcessEngine);
=======
              parseElement(Element.DATASOURCE, reader, addProcessEngine);
              break;
            }
            case HISTORY_LEVEL: {
              parseElement(Element.HISTORY_LEVEL, reader, addProcessEngine);
>>>>>>> platform-ce/master
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
  
<<<<<<< HEAD
  private void parseProperties(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode parentAddress) throws XMLStreamException {
    if (!Tag.PROPERTIES.getLocalName().equals(reader.getLocalName())) {
=======
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
>>>>>>> platform-ce/master
      throw unexpectedElement(reader);
    }
    
    requireNoAttributes(reader);
    
    ModelNode properties = new ModelNode();
    
    while (reader.hasNext()) {
      switch (reader.nextTag()) {
        case END_ELEMENT: {
<<<<<<< HEAD
          if (Tag.forName(reader.getLocalName()) == Tag.PROPERTIES) {
            // should mean we're done, so ignore it.
            parentAddress.get(Tag.PROPERTIES.getLocalName()).set(properties);
=======
          if (Element.forName(reader.getLocalName()) == Element.PROPERTIES) {
            // should mean we're done, so ignore it.
            parentAddress.get(Element.PROPERTIES.getLocalName()).set(properties);
>>>>>>> platform-ce/master
            return;
          }
        }
        case START_ELEMENT: {
<<<<<<< HEAD
          switch (Tag.forName(reader.getLocalName())) {
=======
          switch (Element.forName(reader.getLocalName())) {
>>>>>>> platform-ce/master
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

<<<<<<< HEAD
    parentAddress.add(name, value);
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
  
  private void parseElement(Tag element, XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
    if (!element.equals(Tag.forName(reader.getLocalName()))) {
=======
    parentAddress.get(name).set(value);
  }

  private void parseJobExecutor(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
    // TODO: implement
  }

  private void parseJobAcquisitions(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
    // TODO: implement
  }

  private void parseJobAcquisition(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
    // TODO: implement
  }
  
  private void parseElement(Element element, XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
    if (!element.equals(Element.forName(reader.getLocalName()))) {
>>>>>>> platform-ce/master
      throw unexpectedElement(reader);
    }
    
    parentAddress.get(reader.getLocalName()).set(rawElementText(reader));
  }
  
  /** {@inheritDoc} */
  @Override
  public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {

    context.startSubsystemElement(FoxPlatformExtension.NAMESPACE, false);

<<<<<<< HEAD
    writer.writeStartElement(Tag.PROCESS_ENGINES.getLocalName());

    ModelNode node = context.getModelNode();
    ModelNode processEngine = node.get(Tag.PROCESS_ENGINE.getLocalName());
    for (Property property : processEngine.asPropertyList()) {

      // write each child element to xml
      writer.writeStartElement(Tag.PROCESS_ENGINE.getLocalName());
      
      writer.writeAttribute(Attribute.NAME.getLocalName(), property.getName());
      ModelNode entry = property.getValue();
      writeAttribute(Attribute.DEFAULT, writer, entry);
      writeElement(Tag.DATASOURCE, writer, entry);
      writeElement(Tag.HISTORY_LEVEL, writer, entry);

      writeProperties(writer, entry);

      writer.writeEndElement();
    }
    // end process-engine
    writer.writeEndElement();
=======
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
>>>>>>> platform-ce/master
    // end process-engines
    writer.writeEndElement();
  }

  private void writeAttribute(Attribute attribute, final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
    if (entry.hasDefined(attribute.getLocalName())) {
      writer.writeAttribute(attribute.getLocalName(), entry.get(attribute.getLocalName()).asString());
    }
  }

<<<<<<< HEAD
  private void writeElement(Tag element, final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
=======
  private void writeElement(Element element, final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
>>>>>>> platform-ce/master
    if (entry.hasDefined(element.getLocalName())) {
      writer.writeStartElement(element.getLocalName());
      writer.writeCharacters(entry.get(element.getLocalName()).asString());
      writer.writeEndElement();
    }
  }
  
  private void writeProperties(final XMLExtendedStreamWriter writer, ModelNode entry) throws XMLStreamException {
<<<<<<< HEAD
    if (entry.hasDefined(Tag.PROPERTIES.getLocalName())) {
      writer.writeStartElement(Tag.PROPERTIES.getLocalName());
      
      List<Property> propertyList = entry.get(Tag.PROPERTIES.getLocalName()).asPropertyList();
      if (!propertyList.isEmpty()) {
        for (Property property : propertyList) {
          writer.writeStartElement(Tag.PROPERTY.getLocalName());
=======
    if (entry.hasDefined(Element.PROPERTIES.getLocalName())) {
      writer.writeStartElement(Element.PROPERTIES.getLocalName());
      
      List<Property> propertyList = entry.get(Element.PROPERTIES.getLocalName()).asPropertyList();
      if (!propertyList.isEmpty()) {
        for (Property property : propertyList) {
          writer.writeStartElement(Element.PROPERTY.getLocalName());
>>>>>>> platform-ce/master
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
<<<<<<< HEAD
  
  /**
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   */
  public enum Tag {
      /**
       * always first
       */
      UNKNOWN(null),

      /**
       * Parent tag for process-engines
       */
      PROCESS_ENGINES("process-engines"),
      
      /**
       * Tag for single process-engine
       */
      PROCESS_ENGINE("process-engine"),
      
      /**
       * Datasource for process-engine
       */
      DATASOURCE("datasource"),
      
      /**
       * History level for process-engine
       */
      HISTORY_LEVEL("history-level"),
      
      /**
       * Parent tag for job-acquisitions
       */
      JOB_EXECUTOR("job-executor"),
      
      /**
       * Parent tag for job-acquisition
       */
      JOB_AQUISITIONS("job-acquisitions"),
      
      /**
       * Tag for single job-acquisition
       */
      JOB_AQUISITION("job-acquisition"),
      
      /**
       * Tag for acquisition-strategy
       */
      JOB_ACQUISITION_STRATEGY("acquisition-strategy"),
      
      /**
       * Parent tag for property
       */
      PROPERTIES("properties"),
      
      /**
       * Tag for specify any property
       */
      PROPERTY("property");

      private final String name;

      /**
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name) {
          this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName() {
          return name;
      }

      private static final Map<String, Tag> MAP;

      static {
          final Map<String, Tag> map = new HashMap<String, Tag>();
          for (Tag element : values()) {
              final String name = element.getLocalName();
              if (name != null)
                  map.put(name, element);
          }
          MAP = map;
      }

      /**
       * Static method to get enum instance given localName string
       *
       * @param localName a string used as localname (typically tag name as defined in xsd)
       * @return the enum instance
       */
      public static Tag forName(String localName) {
          final Tag element = MAP.get(localName);
          return element == null ? UNKNOWN : element;
      }

  }
  
  /**
   * An attribute.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   */
  public enum Attribute {
      /**
       * always first
       */
      UNKNOWN(null),

      NAME("name"),
      
      DEFAULT("default");
      
      private final String name;

      /**
       * Create a new attribute.
       *
       * @param name a name
       */
      Attribute(final String name) {
          this.name = name;
      }

      /**
       * Get the local name of this attribute.
       *
       * @return the local name
       */
      public String getLocalName() {
          return name;
      }

      private static final Map<String, Attribute> MAP;

      static {
          final Map<String, Attribute> map = new HashMap<String, Attribute>();
          for (Attribute attribute : values()) {
              final String name = attribute.getLocalName();
              if (name != null)
                  map.put(name, attribute);
          }
          MAP = map;
      }

      /**
       * Static method to get enum instance given localName string
       *
       * @param localName a string used as localname (typically attribute name as defined in xsd)
       * @return the enum instance
       */
      public static Attribute forName(String localName) {
          final Attribute attribute = MAP.get(localName);
          return attribute == null ? UNKNOWN : attribute;
      }

  }
=======
>>>>>>> platform-ce/master
}
