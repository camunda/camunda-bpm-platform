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
package org.camunda.spin.xml;

public class XmlTestUtil {
  /**
   * Example input <ns2:date>2015-04-04T00:00:00+02:00</ns2:date> -> <ns2:date>2015-04-04</ns2:date>
   *
   * @param input a string containing timezone offset
   * @return same as input string without the timezone offset
   */
  public static String removeTimeZone(String input) {
    if (input == null) {
      return null;
    }

    final String TIMEZONE = "T00:00:00";
    final String CLOSING_BRACKET = "<";

    int indexOfTimezone = input.indexOf(TIMEZONE);
    if (indexOfTimezone == -1) {
      return input;
    }

    int indexOfClosingBracket = input.indexOf(CLOSING_BRACKET, indexOfTimezone);
    if (indexOfClosingBracket == -1) {
      return input;
    }

    return input.substring(0, indexOfTimezone) + input.substring(indexOfClosingBracket);
  }
}
