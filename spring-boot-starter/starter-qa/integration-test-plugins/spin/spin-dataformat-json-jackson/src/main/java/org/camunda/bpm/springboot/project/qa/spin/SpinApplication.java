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
package org.camunda.bpm.springboot.project.qa.spin;

import static org.camunda.spin.Spin.S;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpinApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringApplication.class, args);
  }

  @Bean
  public JavaDelegate spinJava8DeserializerDelegate() {
    return delegateExecution -> {
      SpinJsonNode jsonNode = S("{\"dateTime\": \"2019-12-12T22:22:22\"}");
      Object         key      = jsonNode.mapTo(SpinJava8Dto.class);
    };
  }

  @Bean
  public JavaDelegate spinDeserializerDelegate() {
    return delegateExecution -> {
      SpinJsonNode jsonNode = S("{\"dateTime\": \"2019-12-12T22:22:22\"}");
      Object key = jsonNode.mapTo(SpinDto.class);
      delegateExecution.setVariables(Variables.createVariables().putValueTyped("dateTime",
          Variables
           .objectValue(key)
           .serializationDataFormat(DataFormats.JSON_DATAFORMAT_NAME)
           .create()));
    };
  }
}
