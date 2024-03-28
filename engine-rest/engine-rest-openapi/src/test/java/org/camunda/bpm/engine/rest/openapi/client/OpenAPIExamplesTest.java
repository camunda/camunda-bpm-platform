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
package org.camunda.bpm.engine.rest.openapi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.jsonoverlay.Overlay;
import com.networknt.oas.model.Example;
import com.networknt.oas.model.Schema;
import com.networknt.oas.model.impl.SchemaImpl;
import com.networknt.openapi.OpenApiHelper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class OpenAPIExamplesTest {

  // TODO fix these tests
  private static final Set<String> SKIP_TESTS = new HashSet<>(Arrays.asList(
      "[get] /history/decision-instance - Response:200 - example-1",
      "[get] /history/decision-instance/{id} - Response:200 - example-1",
      "[get] /history/detail - Response:200 - example-1",
      "[post] /history/detail - Response:200 - example-1",
      "[get] /history/detail/{id} - Response:200 - example-1",
      "[post] /history/variable-instance - Request - example-1",
      "[post] /history/variable-instance/count - Request - example-1"
  ));

  private static final JsonSchemaFactory FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
  private static final SchemaValidatorsConfig SCHEMA_VALIDATORS_CONFIG = new SchemaValidatorsConfig();
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String APPLICATION_JSON = "application/json";
  private static JsonNode componentSchemasNode;

  private final String key;
  private final Schema schema;
  private final Example example;

  public OpenAPIExamplesTest(String key, Schema schema, Example example) {
    this.key = key;
    this.schema = schema;
    this.example = example;
  }

  @BeforeClass
  public static void setUp() {
    SCHEMA_VALIDATORS_CONFIG.setOpenAPI3StyleDiscriminators(true);
    SCHEMA_VALIDATORS_CONFIG.setHandleNullableField(true);
  }

  private static String getOpenApiJsonString() {
    try (var is = OpenAPIExamplesTest.class.getClassLoader().getResourceAsStream("openapi.json")) {
      return new BufferedReader(new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8)).lines()
          .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<Object[]> initTestParameters() throws Exception {
    var spec = getOpenApiJsonString();
    componentSchemasNode = MAPPER.readTree(spec).get("components");

    var openApiHelper = new OpenApiHelper(spec);
    openApiHelper.setBasePath("/");

    var parametersList = new ArrayList<Object[]>();
    openApiHelper.openApi3.getPaths().forEach((path, openApiPath) -> {
      openApiPath.getOperations().forEach((operation, openApiOperation) -> {

        var key = String.format("[%s] %s", operation, path);

        // request
        if (openApiOperation.getRequestBody().hasContentMediaType(APPLICATION_JSON)) {
          var contentMediaType = openApiOperation.getRequestBody().getContentMediaType(APPLICATION_JSON);

          if (contentMediaType.hasExamples()) {
            var openApiSchema = contentMediaType.getSchema(true);
            contentMediaType.getExamples(true).forEach((example, openApiExample) -> {
              var testKey = String.format("%s - Request - %s", key, example);
              parametersList.add(new Object[] { testKey, openApiSchema, openApiExample });
            });
          }
        }

        // responses
        openApiOperation.getResponses().forEach((response, openApiResponse) -> {
          if (openApiResponse.hasContentMediaType(APPLICATION_JSON)) {
            var contentMediaType = openApiResponse.getContentMediaType(APPLICATION_JSON);

            if (contentMediaType.hasExamples()) {
              var openApiSchema = contentMediaType.getSchema(true);
              contentMediaType.getExamples(true).forEach((example, openApiExample) -> {
                var testKey = String.format("%s - Response:%s - %s", key, response, example);
                parametersList.add(new Object[] { testKey, openApiSchema, openApiExample });
              });
            }
          }
        });
      });
    });

    return parametersList;
  }

  @Parameterized.Parameters(name = "{index} - {0}")
  public static Collection<Object[]> data() throws Exception {
    var parametersList = initTestParameters();
    parametersList.removeIf(parameters -> SKIP_TESTS.contains(parameters[0]));
    return parametersList;
  }

  @Test
  public void validateOpenAPIExample() {
    // given
    var schemaNode = Overlay.toJson((SchemaImpl) schema);
    var jsonNode = MAPPER.valueToTree(example.getValue());
    // append 'components' to schema so $ref can be resolved
    ((ObjectNode) schemaNode).set("components", componentSchemasNode);
    var jsonSchema = FACTORY.getSchema(schemaNode, SCHEMA_VALIDATORS_CONFIG);

    // when
    var validationMessages = jsonSchema.validate(jsonNode);

    // then
    var errorMessage = validationMessages.stream()
        .map(ValidationMessage::getMessage)
        .collect(Collectors.joining(System.lineSeparator()));
    var fullErrorMessage = "Schema check failed for: '" + key + "'" + System.lineSeparator() + errorMessage;
    assertThat(validationMessages).overridingErrorMessage(fullErrorMessage).isEmpty();
  }

}
