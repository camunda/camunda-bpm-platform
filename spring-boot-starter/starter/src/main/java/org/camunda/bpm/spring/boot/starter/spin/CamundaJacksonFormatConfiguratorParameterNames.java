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
package org.camunda.bpm.spring.boot.starter.spin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.spi.DataFormatConfigurator;


public class CamundaJacksonFormatConfiguratorParameterNames implements DataFormatConfigurator<JacksonJsonDataFormat> {

  @Override
  public Class<JacksonJsonDataFormat> getDataFormatClass() {
    return JacksonJsonDataFormat.class;
  }

  @Override
  public void configure(JacksonJsonDataFormat dataFormat) {
    ObjectMapper mapper = dataFormat.getObjectMapper();
    final ParameterNamesModule parameterNamesModule = new ParameterNamesModule();

    mapper.registerModule(parameterNamesModule);
  }
}
