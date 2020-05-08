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
package org.camunda.bpm.engine.test.util;

import java.util.Comparator;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogEventComparator implements Comparator<ILoggingEvent>{

  @Override
  public int compare(ILoggingEvent o1, ILoggingEvent o2) {
    // cast should be safe as MAX_INT miliseconds are ~25 days
    return (int) (o1.getTimeStamp() - o2.getTimeStamp());
  }

}
