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
package org.camunda.bpm.dmn.engine.impl.type;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformer;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformerFactory;

/**
 * {@link DataTypeTransformerFactory} for the built-in {@link DataTypeTransformer}s.
 *
 * @author Philipp Ossler
 */
public class DefaultDataTypeTransformerFactory implements DataTypeTransformerFactory {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected static final Map<String, DataTypeTransformer> transformers = getDefaultTransformers();

  @Override
  public DataTypeTransformer getTransformerForType(String typeName) {

    if(typeName != null && transformers.containsKey(typeName.toLowerCase())) {
      return transformers.get(typeName.toLowerCase());
    } else {
      LOG.unsupportedTypeDefinitionForClause(typeName);
    }
    return new IdentityDataTypeTransformer();
  }

  protected static Map<String, DataTypeTransformer> getDefaultTransformers() {
    Map<String, DataTypeTransformer> transformers = new HashMap<String, DataTypeTransformer>();

    transformers.put("string", new StringDataTypeTransformer());
    transformers.put("boolean", new BooleanDataTypeTransformer());
    transformers.put("integer", new IntegerDataTypeTransformer());
    transformers.put("long", new LongDataTypeTransformer());
    transformers.put("double", new DoubleDataTypeTransformer());
    transformers.put("date and time", new DateTimeDataTypeTransformer());
    transformers.put("date", new LocalDateDataTypeTransformer());
    transformers.put("time", new LocalTimeDataTypeTransformator());
    transformers.put("duration", new DurationDataTypeTransformer());

    return transformers;
  }

}
