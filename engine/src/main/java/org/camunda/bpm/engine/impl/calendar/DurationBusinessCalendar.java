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
package org.camunda.bpm.engine.impl.calendar;

import java.util.Date;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;


/**
 * @author Tom Baeyens
 */
public class DurationBusinessCalendar implements BusinessCalendar {

  private final static EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  public static String NAME = "duration";

  public Date resolveDuedate(String duedate) {
    try {
      DurationHelper dh = new DurationHelper(duedate);
      return dh.getDateAfter();
    }
    catch (Exception e) {
      throw LOG.exceptionWhileResolvingDuedate(duedate, e);
    }
  }
}
