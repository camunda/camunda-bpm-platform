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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

/**
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupContext {

  private boolean immediatelyDue;
  private int minuteFrom;
  private int minuteTo;

  public HistoryCleanupContext(boolean immediatelyDue, int minuteFrom, int minuteTo) {
    this.immediatelyDue = immediatelyDue;
    this.minuteFrom = minuteFrom;
    this.minuteTo = minuteTo;
  }

  public HistoryCleanupContext(int minuteFrom, int minuteTo) {
    this.minuteFrom = minuteFrom;
    this.minuteTo = minuteTo;
  }

  public boolean isImmediatelyDue() {
    return immediatelyDue;
  }

  public void setImmediatelyDue(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  public int getMinuteFrom() {
    return minuteFrom;
  }

  public void setMinuteFrom(int minuteFrom) {
    this.minuteFrom = minuteFrom;
  }

  public int getMinuteTo() {
    return minuteTo;
  }

  public void setMinuteTo(int minuteTo) {
    this.minuteTo = minuteTo;
  }
}
