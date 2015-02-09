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
package org.camunda.bpm.engine.impl.json;

import java.util.List;

import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Thorben Lindhauer
 *
 */
public class JsonQueryFilteringPropertyConverter extends JsonObjectConverter<QueryEntityRelationCondition> {

  protected static JsonQueryFilteringPropertyConverter INSTANCE =
      new JsonQueryFilteringPropertyConverter();

  protected static JsonArrayConverter<List<QueryEntityRelationCondition>> ARRAY_CONVERTER =
      new JsonArrayOfObjectsConverter<QueryEntityRelationCondition>(INSTANCE);

  public static final String BASE_PROPERTY = "baseField";
  public static final String COMPARISON_PROPERTY = "comparisonField";
  public static final String SCALAR_VALUE = "value";

  public JSONObject toJsonObject(QueryEntityRelationCondition filteringProperty) {
    JSONObject jsonObject = new JSONObject();

    JsonUtil.addField(jsonObject, BASE_PROPERTY, filteringProperty.getProperty().getName());

    QueryProperty comparisonProperty = filteringProperty.getComparisonProperty();
    if (comparisonProperty != null) {
      JsonUtil.addField(jsonObject, COMPARISON_PROPERTY, comparisonProperty.getName());
    }

    Object scalarValue = filteringProperty.getScalarValue();
    if (scalarValue != null) {
      JsonUtil.addField(jsonObject, SCALAR_VALUE, scalarValue);
    }

    return jsonObject;
  }

  public QueryEntityRelationCondition toObject(JSONObject jsonObject) {
    // this is limited in that it allows only String values;
    // that is sufficient for current use case with task filters
    // but could be extended by a data type in the future
    Object scalarValue = null;
    if (jsonObject.has(SCALAR_VALUE)) {
      scalarValue = jsonObject.getString(SCALAR_VALUE);
    }

    QueryProperty baseProperty = null;
    if (jsonObject.has(BASE_PROPERTY)) {
      baseProperty = new QueryPropertyImpl(jsonObject.getString(BASE_PROPERTY));
    }

    QueryProperty comparisonProperty = null;
    if (jsonObject.has(COMPARISON_PROPERTY)) {
      comparisonProperty = new QueryPropertyImpl(jsonObject.getString(COMPARISON_PROPERTY));
    }

    return new QueryEntityRelationCondition(baseProperty, comparisonProperty, scalarValue);
  }

}
