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

import org.camunda.bpm.engine.ProcessEngineException;
import org.jboss.as.connector.util.AbstractParser;
import org.jboss.as.connector.util.ParserException;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.camunda.bpm.container.impl.jboss.extension.Attribute.DEFAULT;
import static org.camunda.bpm.container.impl.jboss.extension.Attribute.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.*;

public class BpmPlatformParser1_1 extends AbstractParser {

  public void parse(final XMLExtendedStreamReader reader, final List<ModelNode> operations, ModelNode subsystemAddress) throws Exception {
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PROCESS_ENGINES: {
          parseProcessEngines(reader, operations, subsystemAddress);
          break;
        }
        case JOB_EXECUTOR: {
          parseJobExecutor(reader, operations, subsystemAddress);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }

  protected void parseProcessEngines(final XMLExtendedStreamReader reader, final List<ModelNode> operations, final ModelNode parentAddress) throws XMLStreamException, ParserException {
    List<String> discoveredEngineNames = new ArrayList<String>();

    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PROCESS_ENGINE: {
          parseProcessEngine(reader, operations, parentAddress, discoveredEngineNames);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }

  protected void parseProcessEngine(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode parentAddress, List<String> discoveredEngineNames) throws XMLStreamException, ParserException {
    String engineName = null;

    //Add the 'add' operation for each 'process-engine' child
    ModelNode addProcessEngineOp = new ModelNode();
    addProcessEngineOp.get(OP).set(ADD);

    for (int i = 0; i < reader.getAttributeCount(); i++) {
      Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
      switch(attribute) {
        case NAME: {
          engineName = rawAttributeText(reader, NAME.getLocalName());
          if (engineName != null && !engineName.equals("null")) {
            SubsystemAttributeDefinitons.NAME.parseAndSetParameter(engineName, addProcessEngineOp, reader);
          } else {
            throw missingRequiredElement(reader, Collections.singleton(NAME.getLocalName()));
          }
          break;
        }
        case DEFAULT: {
          final String value = rawAttributeText(reader, DEFAULT.getLocalName());
          if (value != null) {
            SubsystemAttributeDefinitons.DEFAULT.parseAndSetParameter(value, addProcessEngineOp, reader);
          }
          break;
        }
        default:
          throw unexpectedAttribute(reader, i);
      }
    }

    ModelNode processEngineAddress = parentAddress.clone();
    processEngineAddress.add(ModelConstants.PROCESS_ENGINES, engineName);
    addProcessEngineOp.get(OP_ADDR).set(processEngineAddress);

    if(discoveredEngineNames.contains(engineName)) {
      throw new ProcessEngineException("A process engine with name '" + engineName + "' already exists. The process engine name must be unique.");
    } else {
      discoveredEngineNames.add(engineName);
    }

    operations.add(addProcessEngineOp);

    parseAdditionalProcessEngineSettings(reader, operations, addProcessEngineOp);
  }

  protected void parseAdditionalProcessEngineSettings(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode addProcessEngineOp) throws XMLStreamException {
    // iterate deeper
    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case DATASOURCE: {
          parseElement(SubsystemAttributeDefinitons.DATASOURCE, addProcessEngineOp, reader);
          break;
        }
        case HISTORY_LEVEL: {
          parseElement(SubsystemAttributeDefinitons.HISTORY_LEVEL, addProcessEngineOp, reader);
          break;
        }
        case CONFIGURATION: {
          parseElement(SubsystemAttributeDefinitons.CONFIGURATION, addProcessEngineOp, reader);
          break;
        }
        case PROPERTIES: {
          parseProperties(reader, operations, addProcessEngineOp);
          break;
        }
        case PLUGINS: {
          parsePlugins(reader, operations, addProcessEngineOp);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }

  protected void parsePlugins(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode addProcessEngine) throws XMLStreamException {
    requireNoAttributes(reader);

    ModelNode plugins = new ModelNode();

    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PLUGIN: {
          parsePlugin(reader, operations, plugins);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }

    addProcessEngine.get(Element.PLUGINS.getLocalName()).set(plugins);
  }

  protected void parsePlugin(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode plugins) throws XMLStreamException {
    requireNoAttributes(reader);
    ModelNode plugin = new ModelNode();

    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PLUGIN_CLASS: {
          parseElement(SubsystemAttributeDefinitons.PLUGIN_CLASS, plugin, reader);
          break;
        }
        case PROPERTIES: {
          parseProperties(reader, operations, plugin);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }

    plugins.add(plugin);
  }

  protected void parseProperties(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode parentAddress) throws XMLStreamException {
    requireNoAttributes(reader);

    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
      final Element element = Element.forName(reader.getLocalName());
      switch (element) {
        case PROPERTY: {
          String name = reader.getAttributeValue(0);
          String value = rawElementText(reader);

          SubsystemAttributeDefinitons.PROPERTIES.parseAndAddParameterElement(name, value, parentAddress, reader);
          break;
        }
        default: {
          throw unexpectedElement(reader);
        }
      }
    }
  }

  protected void parseJobExecutor(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode parentAddress) throws XMLStreamException {
    // Add the 'add' operation for 'job-executor' parent
    ModelNode addJobExecutorOp = new ModelNode();
    addJobExecutorOp.get(OP).set(ADD);
    ModelNode jobExecutorAddress = parentAddress.clone();
    jobExecutorAddress.add(ModelConstants.JOB_EXECUTOR, ModelConstants.DEFAULT);
    addJobExecutorOp.get(OP_ADDR).set(jobExecutorAddress);

    operations.add(addJobExecutorOp);

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
              parseJobAcquisitions(reader, operations, addJobExecutorOp);
              break;
            }
            case THREAD_POOL_NAME: {
              parseElement(SubsystemAttributeDefinitons.THREAD_POOL_NAME, addJobExecutorOp, reader);
              break;
            }
            case CORE_THREADS: {
              parseElement(SubsystemAttributeDefinitons.CORE_THREADS, addJobExecutorOp, reader);
              break;
            }
            case MAX_THREADS: {
              parseElement(SubsystemAttributeDefinitons.MAX_THREADS, addJobExecutorOp, reader);
              break;
            }
            case QUEUE_LENGTH: {
              parseElement(SubsystemAttributeDefinitons.QUEUE_LENGTH, addJobExecutorOp, reader);
              break;
            }
            case KEEPALIVE_TIME: {
              parseElement(SubsystemAttributeDefinitons.KEEPALIVE_TIME, addJobExecutorOp, reader);
              break;
            }
            case ALLOW_CORE_TIMEOUT: {
              parseElement(SubsystemAttributeDefinitons.ALLOW_CORE_TIMEOUT, addJobExecutorOp, reader);
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

  protected void parseJobAcquisitions(XMLExtendedStreamReader reader, List<ModelNode> operation, ModelNode parentAddress) throws XMLStreamException {
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
              parseJobAcquisition(reader, operation, parentAddress);
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

  protected void parseJobAcquisition(XMLExtendedStreamReader reader, List<ModelNode> operations, ModelNode parentAddress) throws XMLStreamException {
    String acquisitionName = null;

    // Add the 'add' operation for each 'job-acquisition' child
    ModelNode addJobAcquisitionOp = new ModelNode();
    addJobAcquisitionOp.get(OP).set(ADD);

    for (int i = 0; i < reader.getAttributeCount(); i++) {
      Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
      switch(attribute) {
        case NAME: {
          acquisitionName = rawAttributeText(reader, NAME.getLocalName());
          if (acquisitionName != null && !acquisitionName.equals("null")) {
            SubsystemAttributeDefinitons.NAME.parseAndSetParameter(acquisitionName, addJobAcquisitionOp, reader);
          } else {
            throw missingRequiredElement(reader, Collections.singleton(NAME.getLocalName()));
          }
          break;
        }
        default:
          throw unexpectedAttribute(reader, i);
      }
    }

    ModelNode jobAcquisitionAddress = parentAddress.get(OP_ADDR).clone();
    jobAcquisitionAddress.add(ModelConstants.JOB_ACQUISITIONS, acquisitionName);
    addJobAcquisitionOp.get(OP_ADDR).set(jobAcquisitionAddress);

    operations.add(addJobAcquisitionOp);

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
              parseProperties(reader, operations, addJobAcquisitionOp);
              break;
            }
            case ACQUISITION_STRATEGY: {
              parseElement(SubsystemAttributeDefinitons.ACQUISITION_STRATEGY, addJobAcquisitionOp, reader);
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

  protected void parseElement(AttributeDefinition attributeDefinition, ModelNode operation, XMLExtendedStreamReader reader) throws XMLStreamException {
    String value = rawElementText(reader);
    ((SimpleAttributeDefinition) attributeDefinition).parseAndSetParameter(value, operation, reader);
  }



  public static final class BpmPlatformSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    static final BpmPlatformSubsystemParser INSTANCE = new BpmPlatformSubsystemParser();

    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {
      // Require no attributes
      ParseUtils.requireNoAttributes(reader);

      final ModelNode subsystemAddress = new ModelNode();
      subsystemAddress.add(SUBSYSTEM, ModelConstants.SUBSYSTEM_NAME);
      subsystemAddress.protect();

      final ModelNode subsystemAdd = new ModelNode();
      subsystemAdd.get(OP).set(ADD);
      subsystemAdd.get(OP_ADDR).set(subsystemAddress);
      operations.add(subsystemAdd);


      while(reader.hasNext() && !reader.isEndElement()) {
        switch (reader.getLocalName()) {
          case SUBSYSTEM: {
            try {
              final BpmPlatformParser1_1 parser = new BpmPlatformParser1_1();
              parser.parse(reader, operations, subsystemAddress);
            } catch (Exception e) {
              throw new XMLStreamException(e);
            }
          }
        }
      }
    }

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
  }

}
