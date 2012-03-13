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
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ATTR_NAME;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ELEMENT_PROCESS_ENGINE;
import static com.camunda.fox.platform.subsystem.impl.extension.ModelConstants.ELEMENT_PROCESS_ENGINES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * Defines the fox-platform subsystem for jboss application server
 * 
 * @author Daniel Meyer
 */
public class FoxPlatformExtension implements Extension {

  /** The name space used for the {@code subsystem} element */
  public static final String NAMESPACE = "urn:com.camunda.fox.fox-platform-ce:1.0";

  /** The name of our subsystem within the model. */
  public static final String SUBSYSTEM_NAME = "fox-platform-ce";
  
  /** The parser used for parsing our subsystem */
  private final SubsystemParser parser = new SubsystemParser();

  public void initialize(ExtensionContext context) {
    // Register the subsystem and operation handlers
    SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, /* TODO: use versioning of the management interface?*/ 1, 0);
    ManagementResourceRegistration subsystemModel = subsystem.registerSubsystemModel(ModelDescriptionProviders.SUBSYSTEM);
    subsystemModel.registerOperationHandler(ADD, SubsystemAddHandler.INSTANCE, ModelDescriptionProviders.SUBSYSTEM_ADD, false);
    subsystemModel.registerOperationHandler(DESCRIBE, SubsystemDescribeHandler.INSTANCE, SubsystemDescribeHandler.INSTANCE, false,OperationEntry.EntryType.PRIVATE);
    
    // Add the process-engine child and operation handlers
    ManagementResourceRegistration processEngineChild = subsystemModel.registerSubModel(PathElement.pathElement("process-engine"), ModelDescriptionProviders.PROCESS_ENGINE_DEC);
    processEngineChild.registerOperationHandler(ModelDescriptionConstants.ADD, ProcessEngineAddHandler.INSTANCE, ProcessEngineAddHandler.INSTANCE);
    processEngineChild.registerOperationHandler(ModelDescriptionConstants.REMOVE, ProcessEngineRemoveHandler.INSTANCE, ProcessEngineRemoveHandler.INSTANCE);
    
    // THINK: here we could add handlers for additional read-write attributes. They would react to a change in the model. 
    // A process engine is mostly read only. However, I could imagine values like the locktime of the jobexecutor to be configurable here.
    // a change to such a value through one of the management interfaces would be persisted in the configuration (Storage.CONFIGURATION) 
    // and distributed across a cluster / domain
    // Example: processEngineChild.registerReadWriteAttribute("jobExecututorLockTime", null, JobExecutorLockTimeHandler.INSTANCE, Storage.CONFIGURATION);

    subsystem.registerXMLElementWriter(parser);

  }

  public void initializeParsers(ExtensionParsingContext context) {
    context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
  }

  private static ModelNode createAddSubsystemOperation() {
    final ModelNode subsystem = new ModelNode();
    subsystem.get(OP).set(ADD);
    subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
    return subsystem;
  }
  
  /**
   * The subsystem parser, which uses stax to read and write to and from xml
   */
  private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

      /** {@inheritDoc} */
      @Override
      public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
          // Require no attributes
          ParseUtils.requireNoAttributes(reader);

          //Add the main subsystem 'add' operation
          list.add(createAddSubsystemOperation());

          //Read the children
          while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
              if (!reader.getLocalName().equals(ELEMENT_PROCESS_ENGINES)) {
                  throw ParseUtils.unexpectedElement(reader);
              }
              while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                  if (reader.isStartElement()) {
                      readProcessEngineElement(reader, list);
                  }
              }
          }
      }

      private void readProcessEngineElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
          if (!reader.getLocalName().equals(ELEMENT_PROCESS_ENGINE)) {
              throw ParseUtils.unexpectedElement(reader);
          }
          String engineName = null;
          String datasource = null;
          String historyLevel = null;
          for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
              String attr = reader.getAttributeLocalName(i);
              if (attr.equals(ATTR_NAME)){
                  engineName = String.valueOf(reader.getAttributeValue(i));
              } else if (attr.equals(ATTR_DATASOURCE)) {
                  datasource = String.valueOf(reader.getAttributeValue(i));
              } else if (attr.equals(ATTR_HISTORY_LEVEL)) {
                historyLevel = String.valueOf(reader.getAttributeValue(i));
              } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
              }
          }
          ParseUtils.requireNoContent(reader);
          if (engineName.equals("null")) {
              throw ParseUtils.missingRequiredElement(reader, Collections.singleton("name"));
          }

          //Add the 'add' operation for each 'process-engine' child
          ModelNode addProcessEngine = new ModelNode();
          addProcessEngine.get(OP).set(ModelDescriptionConstants.ADD);
          PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement(ELEMENT_PROCESS_ENGINE, engineName));
          addProcessEngine.get(OP_ADDR).set(addr.toModelNode());
       
          if(datasource != null) {
            addProcessEngine.get(ATTR_DATASOURCE).set(datasource);
          } 
          if(historyLevel != null) {
            addProcessEngine.get(ATTR_HISTORY_LEVEL).set(historyLevel);
          }
          list.add(addProcessEngine);          
      }

      /** {@inheritDoc} */
      @Override
      public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {

          context.startSubsystemElement(FoxPlatformExtension.NAMESPACE, false);

          writer.writeStartElement(ELEMENT_PROCESS_ENGINES);

          ModelNode node = context.getModelNode();
          ModelNode processEngine = node.get(ELEMENT_PROCESS_ENGINE);
          for (Property property : processEngine.asPropertyList()) {

              //write each child element to xml
              writer.writeStartElement(ELEMENT_PROCESS_ENGINE);
              writer.writeAttribute(ATTR_NAME, property.getName());
              ModelNode entry = property.getValue();
              if (entry.hasDefined(ATTR_DATASOURCE)) {
                  writer.writeAttribute(ATTR_DATASOURCE, entry.get(ATTR_DATASOURCE).asString());
              }
              if (entry.hasDefined(ATTR_HISTORY_LEVEL)) {
                writer.writeAttribute(ATTR_HISTORY_LEVEL, entry.get(ATTR_HISTORY_LEVEL).asString());
              }
              writer.writeEndElement();
          }
          // end process-engine
          writer.writeEndElement();
          // end process-engines
          writer.writeEndElement();
      }
  }

  /**
   * Recreate the steps to put the subsystem in the same state it was in. This
   * is used in domain mode to query the profile being used, in order to get the
   * steps needed to create the servers
   */
  private static class SubsystemDescribeHandler implements OperationStepHandler, DescriptionProvider {

    static final SubsystemDescribeHandler INSTANCE = new SubsystemDescribeHandler();

    public ModelNode getModelDescription(Locale locale) {
      return CommonDescriptions.getSubsystemDescribeOperation(locale);
    }

    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
      // Add the main operation
      context.getResult().add(createAddSubsystemOperation());
      
      //Add the operations to create each child
      ModelNode node = context.readModel(PathAddress.EMPTY_ADDRESS);
      for (Property property : node.get(ELEMENT_PROCESS_ENGINE).asPropertyList()) {
          ModelNode addType = new ModelNode();
          addType.get(OP).set(ModelDescriptionConstants.ADD);
          PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement(ELEMENT_PROCESS_ENGINE, property.getName()));
          addType.get(OP_ADDR).set(addr.toModelNode());
          if (property.getValue().hasDefined(ATTR_DATASOURCE)) {
              addType.get(ATTR_DATASOURCE).set(property.getValue().get(ATTR_DATASOURCE).asString());
          }
          if (property.getValue().hasDefined(ATTR_HISTORY_LEVEL)) {
            addType.get(ATTR_HISTORY_LEVEL).set(property.getValue().get(ATTR_HISTORY_LEVEL).asString());
          }
          context.getResult().add(addType);
      }
      context.completeStep();
    }

  }

}
