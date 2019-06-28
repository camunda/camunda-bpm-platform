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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformerRegistry;

/**
 * {@link DmnDataTypeTransformerRegistry} for the built-in {@link DmnDataTypeTransformer}s.
 *
 * @author Philipp Ossler
 */
public class DefaultDataTypeTransformerRegistry implements DmnDataTypeTransformerRegistry {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected static final Map<String, DmnDataTypeTransformer> transformers = getDefaultTransformers();

  protected static Map<String, DmnDataTypeTransformer> getDefaultTransformers() {
    Map<String, DmnDataTypeTransformer> transformers = new HashMap<String, DmnDataTypeTransformer>();

    transformers.put("string", new StringDataTypeTransformer());
    transformers.put("boolean", new BooleanDataTypeTransformer());
    transformers.put("integer", new IntegerDataTypeTransformer());
    transformers.put("long", new LongDataTypeTransformer());
    transformers.put("double", new DoubleDataTypeTransformer());
    transformers.put("date", new DateDataTypeTransformer());

    return transformers;
  }

  public void addTransformer(String typeName, DmnDataTypeTransformer transformer) {
    transformers.put(typeName, transformer);
  }

  public DmnDataTypeTransformer getTransformer(String typeName) {
    if(typeName != null && transformers.containsKey(typeName.toLowerCase())) {
      return transformers.get(typeName.toLowerCase());
    } else {
      LOG.unsupportedTypeDefinitionForClause(typeName);
    }
    return new IdentityDataTypeTransformer();
  }

}
