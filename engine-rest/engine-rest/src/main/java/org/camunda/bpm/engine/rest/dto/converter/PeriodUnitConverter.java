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

import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;

/**
 * @author Roman Smirnov
 *
 */
public class PeriodUnitConverter extends JacksonAwareStringToTypeConverter<PeriodUnit> {

  public PeriodUnit convertQueryParameterToType(String value) {
    return mapToEnum(value, PeriodUnit.class);
  }

  protected <T extends Enum<T>> T mapToEnum(String value, Class<T> type) {
    try {
      return Enum.valueOf(type, value.toUpperCase());
    }
    catch (IllegalArgumentException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, String.format("Cannot convert value %s to java enum type %s",
          value, type.getName()));
    }
    catch (NullPointerException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, String.format("Cannot convert value %s to java enum type %s",
          value, type.getName()));
    }
  }

}
