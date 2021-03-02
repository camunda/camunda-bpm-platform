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
package org.camunda.bpm.client.variable.impl.format.json;

import org.camunda.bpm.client.exception.DataFormatException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;

public class JacksonJsonLogger extends ExternalTaskClientLogger {

  public DataFormatException unableToReadValue(String value, Exception e) {
    return new DataFormatException(exceptionMessage("001", "Unable to read value '{}' to object", value), e);
  }

  public DataFormatException unableToConstructJavaType(String fromString, Exception cause) {
    return new DataFormatException(
        exceptionMessage("002", "Cannot construct java type from string '{}'", fromString), cause);
  }

  public DataFormatException unableToDetectCanonicalType(Object parameter) {
    return new DataFormatException(exceptionMessage("003", "Cannot detect canonical data type for parameter '{}'", parameter));
  }

  public DataFormatException unableToWriteValue(Object input, Exception cause) {
    return new DataFormatException(exceptionMessage("004", "Unable to write value '{}' to json", input), cause);
  }

}
