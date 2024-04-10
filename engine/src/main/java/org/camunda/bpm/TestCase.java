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

package org.camunda.bpm;

import org.w3c.dom.Element;

public class TestCase {

  private final String name;
  private final Status status;
  private final long duration;

  private TestCase(String name, Status status, long duration) {
    this.name = name;
    this.status = status;
    this.duration = duration;
  }

  public static TestCase of(Element element) {

    if (!"test".equals(element.getTagName())) {
      throw new IllegalArgumentException("Cannot instantiate a test result out of an element that is not `test`: found " + element.getTagName());
    }

    long duration = Long.parseLong(element.getAttribute("duration"));
    var testName = element.getAttribute("name");
    var result = Status.parse(element.getAttribute("status"));

    return new TestCase(testName, result, duration);
  }

  public String getName() {
    return name;
  }

  public Status getStatus() {
    return status;
  }

  public long getDuration() {
    return duration;
  }

  public enum Status {
    PASSED, FAILED, ERROR, IGNORED, EMPTY;

    public static Status parse(String name) {
      if (name == null) {
        return EMPTY;
      }

      try {
        var upperCaseValue = name.toUpperCase();
        return Status.valueOf(upperCaseValue);
      } catch (Exception e) {
        throw new IllegalArgumentException("Result " + name + " is not a valid value");
      }
    }
  }
}