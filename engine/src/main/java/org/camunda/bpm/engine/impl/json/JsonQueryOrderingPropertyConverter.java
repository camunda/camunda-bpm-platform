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

import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.VariableInstanceQueryProperty;
import org.camunda.bpm.engine.impl.VariableOrderProperty;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.impl.variable.ValueTypeResolverImpl;
import org.camunda.bpm.engine.query.QueryProperty;


/**
 * @author Thorben Lindhauer
 *
 */
public class JsonQueryOrderingPropertyConverter extends JsonObjectConverter<QueryOrderingProperty> {


  protected static JsonQueryOrderingPropertyConverter INSTANCE =
      new JsonQueryOrderingPropertyConverter();

  protected static JsonArrayConverter<List<QueryOrderingProperty>> ARRAY_CONVERTER =
      new JsonArrayOfObjectsConverter<QueryOrderingProperty>(INSTANCE);

  public static final String RELATION = "relation";
  public static final String QUERY_PROPERTY = "queryProperty";
  public static final String QUERY_PROPERTY_FUNCTION = "queryPropertyFunction";
  public static final String DIRECTION = "direction";
  public static final String RELATION_CONDITIONS = "relationProperties";


  public JSONObject toJsonObject(QueryOrderingProperty property) {
    JSONObject jsonObject = new JSONObject();

    JsonUtil.addField(jsonObject, RELATION, property.getRelation());

    QueryProperty queryProperty = property.getQueryProperty();
    if (queryProperty != null) {
      JsonUtil.addField(jsonObject, QUERY_PROPERTY, queryProperty.getName());
      JsonUtil.addField(jsonObject, QUERY_PROPERTY_FUNCTION, queryProperty.getFunction());
    }

    Direction direction = property.getDirection();
    if (direction != null) {
      JsonUtil.addField(jsonObject, DIRECTION, direction.getName());
    }

    if (property.hasRelationConditions()) {
      JSONArray relationConditionsJson = JsonQueryFilteringPropertyConverter.ARRAY_CONVERTER
        .toJsonArray(property.getRelationConditions());
      JsonUtil.addField(jsonObject, RELATION_CONDITIONS, relationConditionsJson);
    }

    return jsonObject;
  }

  public QueryOrderingProperty toObject(JSONObject jsonObject) {
    String relation = null;
    if (jsonObject.has(RELATION)) {
      relation = jsonObject.getString(RELATION);
    }

    QueryOrderingProperty property = null;
    if (QueryOrderingProperty.RELATION_VARIABLE.equals(relation)) {
      property = new VariableOrderProperty();
    }
    else {
      property = new QueryOrderingProperty();
    }

    property.setRelation(relation);

    if (jsonObject.has(QUERY_PROPERTY)) {
      String propertyName = jsonObject.getString(QUERY_PROPERTY);
      String propertyFunction = null;
      if (jsonObject.has(QUERY_PROPERTY_FUNCTION)) {
        propertyFunction = jsonObject.getString(QUERY_PROPERTY_FUNCTION);
      }

      QueryProperty queryProperty = new QueryPropertyImpl(propertyName, propertyFunction);
      property.setQueryProperty(queryProperty);
    }

    if (jsonObject.has(DIRECTION)) {
      String direction = jsonObject.getString(DIRECTION);
      property.setDirection(Direction.findByName(direction));
    }

    if (jsonObject.has(RELATION_CONDITIONS)) {
      List<QueryEntityRelationCondition> relationConditions =
          JsonQueryFilteringPropertyConverter.ARRAY_CONVERTER.toObject(jsonObject.getJSONArray(RELATION_CONDITIONS));
      property.setRelationConditions(relationConditions);
    }

    return property;
  }
}
