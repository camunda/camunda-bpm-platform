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
package org.camunda.bpm.engine.impl.calendar;

import java.util.Date;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;
import org.camunda.bpm.engine.task.Task;

public class CycleBusinessCalendar implements BusinessCalendar {

  private final static EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  public static String NAME = "cycle";

  public Date resolveDuedate(String duedateDescription, Task task) {
    return resolveDuedate(duedateDescription);
  }

  public Date resolveDuedate(String duedateDescription) {
    return resolveDuedate(duedateDescription, (Date)null);
  }

  public Date resolveDuedate(String duedateDescription, Date startDate) {
    return resolveDuedate(duedateDescription, startDate, 0L);
  }

  public Date resolveDuedate(String duedateDescription, Date startDate, long repeatOffset) {
    try {
      if (duedateDescription.startsWith("R")) {
        DurationHelper durationHelper = new DurationHelper(duedateDescription, startDate);
        durationHelper.setRepeatOffset(repeatOffset);
        return durationHelper.getDateAfter(startDate);
      } else {
        CronExpression ce = new CronExpression(duedateDescription);
        return ce.getTimeAfter(startDate == null ? ClockUtil.getCurrentTime() : startDate);
      }

    }
    catch (Exception e) {
      throw LOG.exceptionWhileParsingCronExpresison(duedateDescription, e);
    }

  }

}
