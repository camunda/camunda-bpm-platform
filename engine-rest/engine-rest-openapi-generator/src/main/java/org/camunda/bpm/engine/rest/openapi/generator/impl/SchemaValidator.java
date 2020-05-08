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
package org.camunda.bpm.engine.rest.openapi.generator.impl;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public class SchemaValidator {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new RuntimeException("Must provide two arguments: <json schema> <file to validate>");
    }

    String jsonSchemaPath = args[0];
    String inputFile = args[1];

    ObjectMapper mapper = new ObjectMapper();
    JsonNode schemaNode = mapper.readTree(new File(jsonSchemaPath));
    JsonNode inputNode = mapper.readTree(new File(inputFile));

    JsonSchemaFactory factory = JsonSchemaFactory.getInstance();
    JsonSchema schema = factory.getSchema(schemaNode);

    Set<ValidationMessage> errors = schema.validate(inputNode);

    if (errors.size() > 0) {
      String messages = errors.stream()
                              .map(ValidationMessage::getMessage)
                              .collect(Collectors.joining("\n"));

      throw new RuntimeException("Schema validation errors\n" + messages);
    }
  }
}
