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
package org.camunda.bpm.model.bpmn.impl;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_20_SCHEMA_LOCATION;

import java.io.InputStream;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.impl.ModelImpl;
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.impl.util.ReflectUtil;
import org.camunda.bpm.model.xml.instance.DomDocument;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

/**
 * <p>The parser used when parsing BPMN Files</p>
 *
 * @author Daniel Meyer
 *
 */
public class BpmnParser extends AbstractModelParser {

 private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  public BpmnParser() {
    this.schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA);
    addSchema(BPMN20_NS, createSchema(BPMN_20_SCHEMA_LOCATION, BpmnParser.class.getClassLoader()));
  }

  @Override
  protected void configureFactory(DocumentBuilderFactory dbf) {
    dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
    dbf.setAttribute(JAXP_SCHEMA_SOURCE, ReflectUtil.getResource(BPMN_20_SCHEMA_LOCATION, BpmnParser.class.getClassLoader()).toString());
    super.configureFactory(dbf);
  }

  @Override
  protected BpmnModelInstanceImpl createModelInstance(DomDocument document) {
    return new BpmnModelInstanceImpl((ModelImpl) Bpmn.INSTANCE.getBpmnModel(), Bpmn.INSTANCE.getBpmnModelBuilder(), document);
  }

  @Override
  public BpmnModelInstanceImpl parseModelFromStream(InputStream inputStream) {
    return (BpmnModelInstanceImpl) super.parseModelFromStream(inputStream);
  }

  @Override
  public BpmnModelInstanceImpl getEmptyModel() {
    return (BpmnModelInstanceImpl) super.getEmptyModel();
  }

}
