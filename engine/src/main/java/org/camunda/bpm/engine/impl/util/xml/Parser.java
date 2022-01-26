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
package org.camunda.bpm.engine.impl.util.xml;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * @author Tom Baeyens
 */
public abstract class Parser {

  protected static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  protected static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
  protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
  protected static final String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";

  protected static ThreadLocal<SAXParserFactory> SAX_PARSER_FACTORY_INSTANCE = ThreadLocal.withInitial(SAXParserFactory::newInstance);

  public abstract Parse createParse();

  protected SAXParser getSaxParser() throws Exception {
    SAXParserFactory saxParserFactory = getSaxParserFactoryLazily();
    setXxeProcessing(saxParserFactory);
    return saxParserFactory.newSAXParser();
  }

  protected SAXParserFactory getSaxParserFactoryLazily() {
    return Parser.SAX_PARSER_FACTORY_INSTANCE.get();
  }

  protected void enableSchemaValidation(boolean enableSchemaValidation) {
    SAXParserFactory saxParserFactory = getSaxParserFactoryLazily();
    saxParserFactory.setNamespaceAware(enableSchemaValidation);
    saxParserFactory.setValidating(enableSchemaValidation);

    try {
      saxParserFactory.setFeature(NAMESPACE_PREFIXES, true);
    } catch (Exception e) {
      LOG.unableToSetSchemaResource(e);
    }
  }

  protected void setXxeProcessing(SAXParserFactory saxParserFactory) {
    boolean enableXxeProcessing = isEnableXxeProcessing();
    saxParserFactory.setXIncludeAware(enableXxeProcessing);
    try {
      saxParserFactory.setFeature(EXTERNAL_GENERAL_ENTITIES, enableXxeProcessing);
      saxParserFactory.setFeature(DISALLOW_DOCTYPE_DECL, !enableXxeProcessing);
      saxParserFactory.setFeature(LOAD_EXTERNAL_DTD, enableXxeProcessing);
      saxParserFactory.setFeature(EXTERNAL_PARAMETER_ENTITIES, enableXxeProcessing);
      saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

    } catch (Exception e) {
      throw LOG.exceptionWhileSettingXxeProcessing(e);

    }
  }

  public Boolean isEnableXxeProcessing() {
    ProcessEngineConfigurationImpl engineConfig = Context.getProcessEngineConfiguration();
    if (engineConfig != null) {
      return engineConfig.isEnableXxeProcessing();
    } else {  // can be null if implementation is called outside command context
      return false;
    }
  }

}
