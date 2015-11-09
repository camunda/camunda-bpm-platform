/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.model.cmmn.impl;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN10_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_10_SCHEMA_LOCATION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_11_SCHEMA_LOCATION;

import java.io.InputStream;

import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelException;
import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.bpm.model.xml.impl.ModelImpl;
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.impl.util.ReflectUtil;
import org.camunda.bpm.model.xml.instance.DomDocument;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnParser extends AbstractModelParser {

  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  public CmmnParser() {
    this.schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA);
    addSchema(CMMN10_NS, createSchema(CMMN_10_SCHEMA_LOCATION, CmmnParser.class.getClassLoader()));
    addSchema(CMMN11_NS, createSchema(CMMN_11_SCHEMA_LOCATION, CmmnParser.class.getClassLoader()));
  }

  @Override
  protected void configureFactory(DocumentBuilderFactory dbf) {
    dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

    ClassLoader classLoader = CmmnParser.class.getClassLoader();
    dbf.setAttribute(JAXP_SCHEMA_SOURCE, new String[] {
        ReflectUtil.getResource(CMMN_10_SCHEMA_LOCATION, classLoader).toString(),
        ReflectUtil.getResource(CMMN_11_SCHEMA_LOCATION, classLoader).toString() }
    );

    super.configureFactory(dbf);
  }

  @Override
  protected CmmnModelInstanceImpl createModelInstance(DomDocument document) {
    return new CmmnModelInstanceImpl((ModelImpl) Cmmn.INSTANCE.getCmmnModel(), Cmmn.INSTANCE.getCmmnModelBuilder(), document);
  }

  @Override
  public CmmnModelInstanceImpl parseModelFromStream(InputStream inputStream) {
    try {
      return (CmmnModelInstanceImpl) super.parseModelFromStream(inputStream);
    }
    catch (ModelParseException e) {
     throw new CmmnModelException("Unable to parse model", e);
    }
  }

  @Override
  public CmmnModelInstanceImpl getEmptyModel() {
    return (CmmnModelInstanceImpl) super.getEmptyModel();
  }

}
