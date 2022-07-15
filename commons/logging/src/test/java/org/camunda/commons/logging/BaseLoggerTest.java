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
package org.camunda.commons.logging;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.camunda.commons.logging.ExampleLogger.*;

/**
 * @author Daniel Meyer
 *
 */
public class BaseLoggerTest {

  public static final String ID = "01";
  public static final String SOME_MESSAGE = "Some message";

  @Test
  public void shouldFormatMessage() {
    ExampleLogger logger = LOG;

    String messageTemplate = "Some message '{}'";

    String formattedMessage = logger.formatMessageTemplate(ID, messageTemplate);
    String expectedMessageTemplate = String.format("%s-%s%s %s", PROJECT_CODE, COMPONENT_ID, ID, messageTemplate);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldFormatExceptionMessageWithParam() {
    ExampleLogger logger = LOG;

    String messageTemplate = "Some message '{}'";
    String parameter = "someParameter";

    String formattedMessage = logger.exceptionMessage(ID, messageTemplate, parameter);
    String expectedMessageTemplate = String.format("%s-%s%s Some message 'someParameter'", PROJECT_CODE, COMPONENT_ID, ID);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldFormatExceptionMessageWithParams() {
    ExampleLogger logger = LOG;

    String messageTemplate = "Some message '{}' '{}'";
    String p1 = "someParameter";
    String p2 = "someOtherParameter";

    String formattedMessage = logger.exceptionMessage(ID, messageTemplate, p1, p2);
    String expectedMessageTemplate = String.format("%s-%s%s Some message 'someParameter' 'someOtherParameter'", PROJECT_CODE, COMPONENT_ID, ID);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldFormatExceptionMessageWithoutParam() {
    ExampleLogger logger = LOG;

    String formattedMessage = logger.exceptionMessage(ID, SOME_MESSAGE);
    String expectedMessageTemplate = String.format("%s-%s%s %s", PROJECT_CODE, COMPONENT_ID, ID, SOME_MESSAGE);

    assertThat(formattedMessage).isEqualTo(expectedMessageTemplate);
  }

  @Test
  public void shouldCallLogTrace() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log("TRACE", ID, SOME_MESSAGE);
    Mockito.verify(logger).logTrace(ID, SOME_MESSAGE);
  }

  @Test
  public void shouldCallLogInfo() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log("INFO", ID, SOME_MESSAGE);
    Mockito.verify(logger).logInfo(ID, SOME_MESSAGE);
  }

  @Test
  public void shouldCallLogDebug() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log("DEBUG", ID, SOME_MESSAGE);
    Mockito.verify(logger).logDebug(ID, SOME_MESSAGE);
  }

  @Test
  public void shouldCallLogError() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log("ERROR", ID, SOME_MESSAGE);
    Mockito.verify(logger).logError(ID, SOME_MESSAGE);
  }

  @Test
  public void shouldCallLogWarn() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log(" warn ", ID, SOME_MESSAGE);
    Mockito.verify(logger).logWarn(ID, SOME_MESSAGE);
  }

  @Test
  public void shouldCallLogDebugWhenNotMatched() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log("FATAL", ID, SOME_MESSAGE);
    Mockito.verify(logger).logDebug(ID, SOME_MESSAGE);
  }

  @Test
  public void shouldCallLogWarnWhenNotMatched() {
    final ExampleLogger logger = Mockito.spy(LOG);
    logger.log("FATAL", Level.WARN, ID, SOME_MESSAGE);
    Mockito.verify(logger).logWarn(ID, SOME_MESSAGE);
  }


}
