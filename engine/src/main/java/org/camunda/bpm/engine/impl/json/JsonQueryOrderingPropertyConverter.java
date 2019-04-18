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
package org.camunda.bpm.engine.impl.json;

import java.util.List;

import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.VariableOrderProperty;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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


  public JsonObject toJsonObject(QueryOrderingProperty property) {
    JsonObject jsonObject = JsonUtil.createObject();

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
      JsonArray relationConditionsJson = JsonQueryFilteringPropertyConverter.ARRAY_CONVERTER
        .toJsonArray(property.getRelationConditions());
      JsonUtil.addField(jsonObject, RELATION_CONDITIONS, relationConditionsJson);
    }

    return jsonObject;
  }

  public QueryOrderingProperty toObject(JsonObject jsonObject) {
    String relation = null;
    if (jsonObject.has(RELATION)) {
      relation = JsonUtil.getString(jsonObject, RELATION);
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
      String propertyName = JsonUtil.getString(jsonObject, QUERY_PROPERTY);
      String propertyFunction = null;
      if (jsonObject.has(QUERY_PROPERTY_FUNCTION)) {
        propertyFunction = JsonUtil.getString(jsonObject, QUERY_PROPERTY_FUNCTION);
      }

      QueryProperty queryProperty = new QueryPropertyImpl(propertyName, propertyFunction);
      property.setQueryProperty(queryProperty);
    }

    if (jsonObject.has(DIRECTION)) {
      String direction = JsonUtil.getString(jsonObject, DIRECTION);
      property.setDirection(Direction.findByName(direction));
    }

    if (jsonObject.has(RELATION_CONDITIONS)) {
      List<QueryEntityRelationCondition> relationConditions =
          JsonQueryFilteringPropertyConverter.ARRAY_CONVERTER.toObject(JsonUtil.getArray(jsonObject, RELATION_CONDITIONS));
      property.setRelationConditions(relationConditions);
    }

    return property;
  }
}
