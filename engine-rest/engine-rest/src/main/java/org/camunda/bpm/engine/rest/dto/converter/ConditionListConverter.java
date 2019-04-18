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
package org.camunda.bpm.engine.rest.dto.converter;

import org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

public class ConditionListConverter extends JacksonAwareStringToTypeConverter<List<ConditionQueryParameterDto>> {

  private static final String EXPRESSION_DELIMITER = ",";
  private static final String ATTRIBUTE_DELIMITER = "_";

  @Override
  public List<ConditionQueryParameterDto> convertQueryParameterToType(String value) {
    String[] expressions = value.split(EXPRESSION_DELIMITER);

    List<ConditionQueryParameterDto> queryConditions = new ArrayList<ConditionQueryParameterDto>();

    for (String expression : expressions) {
      String[] valueTuple = expression.split(ATTRIBUTE_DELIMITER);
      if (valueTuple.length != 2) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "condition query parameter has to have format OPERATOR_VALUE.");
      }

      ConditionQueryParameterDto queryCondition = new ConditionQueryParameterDto();
      queryCondition.setOperator(valueTuple[0]);
      queryCondition.setValue(valueTuple[1]);

      queryConditions.add(queryCondition);
    }

    return queryConditions;
  }

}
