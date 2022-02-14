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
package org.camunda.bpm.engine.rest.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.impl.QueryOperator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author roman.smirnov
 */
public class ConditionQueryParameterDto {

  public ConditionQueryParameterDto() {

  }

  public static final String EQUALS_OPERATOR_NAME = "eq";
  public static final String NOT_EQUALS_OPERATOR_NAME = "neq";
  public static final String GREATER_THAN_OPERATOR_NAME = "gt";
  public static final String GREATER_THAN_OR_EQUALS_OPERATOR_NAME = "gteq";
  public static final String LESS_THAN_OPERATOR_NAME = "lt";
  public static final String LESS_THAN_OR_EQUALS_OPERATOR_NAME = "lteq";
  public static final String LIKE_OPERATOR_NAME = "like";
  public static final String NOT_LIKE_OPERATOR_NAME = "notLike";

  protected static final Map<String, QueryOperator> NAME_OPERATOR_MAP = new HashMap<>();

  static {
    NAME_OPERATOR_MAP.put(EQUALS_OPERATOR_NAME, QueryOperator.EQUALS);
    NAME_OPERATOR_MAP.put(NOT_EQUALS_OPERATOR_NAME, QueryOperator.NOT_EQUALS);
    NAME_OPERATOR_MAP.put(GREATER_THAN_OPERATOR_NAME, QueryOperator.GREATER_THAN);
    NAME_OPERATOR_MAP.put(GREATER_THAN_OR_EQUALS_OPERATOR_NAME, QueryOperator.GREATER_THAN_OR_EQUAL);
    NAME_OPERATOR_MAP.put(LESS_THAN_OPERATOR_NAME, QueryOperator.LESS_THAN);
    NAME_OPERATOR_MAP.put(LESS_THAN_OR_EQUALS_OPERATOR_NAME, QueryOperator.LESS_THAN_OR_EQUAL);
    NAME_OPERATOR_MAP.put(LIKE_OPERATOR_NAME, QueryOperator.LIKE);
    NAME_OPERATOR_MAP.put(NOT_LIKE_OPERATOR_NAME, QueryOperator.NOT_LIKE);
  };

  protected static final Map<QueryOperator, String> OPERATOR_NAME_MAP = new HashMap<>();

  static {
    OPERATOR_NAME_MAP.put(QueryOperator.EQUALS, EQUALS_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.NOT_EQUALS, NOT_EQUALS_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.GREATER_THAN, GREATER_THAN_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.GREATER_THAN_OR_EQUAL, GREATER_THAN_OR_EQUALS_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.LESS_THAN, LESS_THAN_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.LESS_THAN_OR_EQUAL, LESS_THAN_OR_EQUALS_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.LIKE, LIKE_OPERATOR_NAME);
    OPERATOR_NAME_MAP.put(QueryOperator.NOT_LIKE, NOT_LIKE_OPERATOR_NAME);
  };

  protected String operator;
  protected Object value;

  public String getOperator() {
    return operator;
  }
  public void setOperator(String operator) {
    this.operator = operator;
  }
  public Object resolveValue(ObjectMapper objectMapper) {
    if (value instanceof String && objectMapper != null) {
      try {
        return objectMapper.readValue("\"" + value + "\"", Date.class);
      } catch (Exception e) {
        // ignore the exception
      }
    }

    return value;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public static QueryOperator getQueryOperator(String name) {
    return NAME_OPERATOR_MAP.get(name);
  }
}
