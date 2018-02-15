/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateTimeUtils {

  public static final SimpleDateFormat DATE_FORMAT_WITHOUT_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  public static final SimpleDateFormat DATE_FORMAT_WITH_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  /**
   * Converts date string without timezone to the one with timezone.
   * @param dateString
   * @return
   */
  public static String withTimezone(String dateString) {
    final Date parse;
    try {
      parse = DATE_FORMAT_WITHOUT_TIMEZONE.parse(dateString);
      return DATE_FORMAT_WITH_TIMEZONE.format(parse);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
