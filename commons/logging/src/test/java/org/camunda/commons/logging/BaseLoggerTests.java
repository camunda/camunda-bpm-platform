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
package org.camunda.commons.logging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.camunda.commons.logging.ExampleLogger.*;

/**
 * @author Daniel Meyer
 *
 */
public class BaseLoggerTests {

  @Test
  public void shouldFormatMessage() {
    ExampleLogger logger = LOG;

    String id = "01";
    String messageTemplate = "Some message '{}'";

    String formattedMessage = logger.formatMessageTemplate(id, messageTemplate);
    String expectedMessageTemplate = String.format("%s-%s%s %s", PROJECT_CODE, COMPONENT_ID, id, messageTemplate);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldFormatExceptionMessageWithParam() {
    ExampleLogger logger = LOG;

    String id = "01";
    String messageTemplate = "Some message '{}'";
    String parameter = "someParameter";

    String formattedMessage = logger.exceptionMessage(id, messageTemplate, parameter);
    String expectedMessageTemplate = String.format("%s-%s%s Some message 'someParameter'", PROJECT_CODE, COMPONENT_ID, id);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldFormatExceptionMessageWithParams() {
    ExampleLogger logger = LOG;

    String id = "01";
    String messageTemplate = "Some message '{}' '{}'";
    String p1 = "someParameter";
    String p2 = "someOtherParameter";

    String formattedMessage = logger.exceptionMessage(id, messageTemplate, p1, p2);
    String expectedMessageTemplate = String.format("%s-%s%s Some message 'someParameter' 'someOtherParameter'", PROJECT_CODE, COMPONENT_ID, id);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldFormatExceptionMessageWithoutParam() {
    ExampleLogger logger = LOG;

    String id = "01";
    String messageTemplate = "Some message";

    String formattedMessage = logger.exceptionMessage(id, messageTemplate);
    String expectedMessageTemplate = String.format("%s-%s%s %s", PROJECT_CODE, COMPONENT_ID, id, messageTemplate);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }


}
