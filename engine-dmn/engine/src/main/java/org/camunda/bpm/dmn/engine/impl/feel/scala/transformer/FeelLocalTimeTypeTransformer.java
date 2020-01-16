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
package org.camunda.bpm.dmn.engine.impl.feel.scala.transformer;

import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.feel.datatype.ZonedTime;

import java.time.LocalTime;
import java.time.OffsetTime;

public class FeelLocalTimeTypeTransformer implements DmnDataTypeTransformer {

  public TypedValue transform(final Object value) {

    if (value instanceof LocalTime) {
      return Variables.untypedValue(value);

    } else if (value instanceof ZonedTime) {
      return Variables.untypedValue(((ZonedTime)value).toLocalTime());

    } else {
      if (!(value instanceof OffsetTime)) {
        throw new IllegalArgumentException("Cannot transform '" + value + "' to FEEL local-time.");
      }

      return Variables.untypedValue(((OffsetTime)value).toLocalTime());

    }
  }

}

