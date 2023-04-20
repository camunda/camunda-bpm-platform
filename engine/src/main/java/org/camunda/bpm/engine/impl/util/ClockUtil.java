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

import java.util.Date;

import org.joda.time.DateTimeUtils;


/**
 * @author Joram Barrez
 */
public class ClockUtil {

  /**
   * Freezes the clock to a specified Date that will be returned by
   * {@link #now()} and {@link #getCurrentTime()}
   *
   * @param currentTime
   *          the Date to freeze the clock at
   */
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

  /**
   * Moves the clock by the given offset and keeps it running from that point
   * on.
   *
   * @param offsetInMillis
   *          the offset to move the clock by
   * @return the new 'now'
   */
  public static Date offset(Long offsetInMillis) {
    DateTimeUtils.setCurrentMillisOffset(offsetInMillis);
    return new Date(DateTimeUtils.currentTimeMillis());
  }

  public static Date resetClock() {
    DateTimeUtils.setCurrentMillisSystem();
    return new Date(DateTimeUtils.currentTimeMillis());
  }

}
