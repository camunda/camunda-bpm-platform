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
package org.camunda.bpm.dmn.engine.impl.type;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnTypeDefinition;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnTypeDefinitionImpl implements DmnTypeDefinition {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected String typeName;
  protected DmnDataTypeTransformer transformer;

  public DmnTypeDefinitionImpl(String typeName, DmnDataTypeTransformer transformer) {
    this.typeName = typeName;
    this.transformer = transformer;
  }

  @Override
  public TypedValue transform(Object value) {
    if (value == null) {
      return Variables.untypedNullValue();
    } else {
      return transformNotNullValue(value);
    }
  }

  protected TypedValue transformNotNullValue(Object value) {
    ensureNotNull("transformer", transformer);

    try {

      return transformer.transform(value);

    } catch (IllegalArgumentException e) {
      throw LOG.invalidValueForTypeDefinition(typeName, value);
    }
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public void setTransformer(DmnDataTypeTransformer transformer) {
    this.transformer = transformer;
  }

  @Override
  public String toString() {
    return "DmnTypeDefinitionImpl{" +
      "typeName='" + typeName + '\'' +
      '}';
  }

}
