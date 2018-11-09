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
package org.camunda.bpm.engine.impl.util;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;


/**
 * @author Joram Barrez
 */
public class ClockUtil {

  private static Clock clock = Clock.system(ZoneId.systemDefault());

  public static void setCurrentTime(Date currentTime) {
    clock = Clock.fixed(currentTime.toInstant(), ZoneId.systemDefault());
  }

  public static void reset() {
    resetClock();
  }

  public static Date getCurrentTime() {
    return now();
  }

  public static Date now() {
    return Date.from(clock.instant());
  }

  public static Date offset(Duration duration) {
    clock = Clock.offset(clock, duration);
    return Date.from(clock.instant());
  }

  public static Date resetClock() {
    clock = Clock.system(ZoneId.of("CET"));
    return Date.from(clock.instant());
  }
}
