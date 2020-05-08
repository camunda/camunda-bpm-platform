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
package org.camunda.bpm.engine.impl.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tobias Metzke
 *
 */
public class StringUtilTest {
  
  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Test
  public void shouldAllowTrimToMaximumLength() {
    // given
    String fittingThreeByteMessage = repeatCharacter("\u9faf", StringUtil.DB_MAX_STRING_LENGTH);
    String exceedingMessage = repeatCharacter("a", StringUtil.DB_MAX_STRING_LENGTH * 2);

    // then
    assertThat(fittingThreeByteMessage.substring(0, StringUtil.DB_MAX_STRING_LENGTH)).isEqualTo(StringUtil.trimToMaximumLengthAllowed(fittingThreeByteMessage));
    assertThat(exceedingMessage.substring(0, StringUtil.DB_MAX_STRING_LENGTH)).isEqualTo(StringUtil.trimToMaximumLengthAllowed(exceedingMessage));
  }

  @Test
  public void shouldConvertByteArrayToString() {
    // given
    String message = "This is a message string";
    byte[] bytes = message.getBytes();

    // when
    String stringFromBytes = StringUtil.fromBytes(bytes, engineRule.getProcessEngine());

    // then
    assertThat(stringFromBytes).isEqualTo(message);
  }

  @Test
  public void shouldConvertNullByteArrayToEmptyString() {
    // given
    byte[] bytes = null;

    // when
    String stringFromBytes = StringUtil.fromBytes(bytes, engineRule.getProcessEngine());

    // then
    assertThat(stringFromBytes).isEmpty();
  }

  protected static String repeatCharacter(String encodedCharacter, int numCharacters) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numCharacters; i++) {
      sb.append(encodedCharacter);
    }
    return sb.toString();
  }
}
