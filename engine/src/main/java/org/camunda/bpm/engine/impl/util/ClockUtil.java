/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.util;

import org.joda.time.DateTimeUtils;

import java.util.Date;


/**
 * @author Joram Barrez
 */
public class ClockUtil {

  public static void setCurrentTime(Date currentTime) {
    DateTimeUtils.setCurrentMillisFixed(currentTime.getTime());
  }

  public static void reset() {
    resetClock();
  }

  public static Date getCurrentTime() {
    return now();
  }

  public static Date now() {
    return new Date(DateTimeUtils.currentTimeMillis());
  }

  public static Date offset(Long offsetInMillis) {
    DateTimeUtils.setCurrentMillisOffset(offsetInMillis);
    return new Date(DateTimeUtils.currentTimeMillis());
  }

  public static Date resetClock() {
    DateTimeUtils.setCurrentMillisSystem();
    return new Date(DateTimeUtils.currentTimeMillis());
  }

}
