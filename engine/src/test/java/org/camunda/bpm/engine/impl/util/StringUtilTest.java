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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Tobias Metzke
 *
 */
public class StringUtilTest {

  @Test
  public void testTrimToMaximumLengthAllowed() {
    String fittingThreeByteMessage = repeatCharacter("\u9faf", StringUtil.DB_MAX_STRING_LENGTH);
    String exceedingMessage = repeatCharacter("a", StringUtil.DB_MAX_STRING_LENGTH * 2);
    
    assertEquals(fittingThreeByteMessage.substring(0, StringUtil.DB_MAX_STRING_LENGTH), StringUtil.trimToMaximumLengthAllowed(fittingThreeByteMessage));
    assertEquals(exceedingMessage.substring(0, StringUtil.DB_MAX_STRING_LENGTH), StringUtil.trimToMaximumLengthAllowed(exceedingMessage));
  }
  
  protected static String repeatCharacter(String encodedCharacter, int numCharacters) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numCharacters; i++) {
      sb.append(encodedCharacter);
    }
    return sb.toString();
  }
}
