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
package org.camunda.bpm.engine.rest.dto.converter;

import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a list of {@link VariableQueryParameterDto}s from a single parameter. Expects a given format (see method comments).
 * @author Thorben Lindhauer
 *
 */
public class VariableListConverter extends
  JacksonAwareStringToTypeConverter<List<VariableQueryParameterDto>> {

  private static final String EXPRESSION_DELIMITER = ",";
  private static final String ATTRIBUTE_DELIMITER = "_";

  /**
   * Expects a query parameter of multiple variable expressions formatted as KEY_OPERATOR_VALUE, e.g. aVariable_eq_aValue.
   * Multiple values are expected to be comma-separated.
   */
  @Override
  public List<VariableQueryParameterDto> convertQueryParameterToType(String value) {
    String[] expressions = value.split(EXPRESSION_DELIMITER);

    List<VariableQueryParameterDto> queryVariables = new ArrayList<VariableQueryParameterDto>();

    for (String expression : expressions) {
      String[] valueTriple = expression.split(ATTRIBUTE_DELIMITER);
      if (valueTriple.length != 3) {
        throw new InvalidRequestException(Status.BAD_REQUEST, "variable query parameter has to have format KEY_OPERATOR_VALUE.");
      }

      VariableQueryParameterDto queryVariable = new VariableQueryParameterDto();
      queryVariable.setName(valueTriple[0]);
      queryVariable.setOperator(valueTriple[1]);
      queryVariable.setValue(valueTriple[2]);

      queryVariables.add(queryVariable);
    }

    return queryVariables;
  }
}
