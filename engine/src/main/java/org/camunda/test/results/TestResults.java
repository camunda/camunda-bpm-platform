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
package org.camunda.test.results;

import static org.camunda.bpm.TestCase.Status.ERROR;
import static org.camunda.bpm.TestCase.Status.FAILED;
import static org.camunda.bpm.TestCase.Status.IGNORED;
import static org.camunda.bpm.TestCase.Status.PASSED;
import static org.camunda.test.results.PrintFormat.CSV;
import static org.camunda.test.results.PrintFormat.JSON;
import static org.camunda.test.results.PrintOutput.CONSOLE;
import static org.camunda.test.results.PrintOutput.FILE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.camunda.bpm.TestCase;
import org.camunda.bpm.TestCase.Status;

public class TestResults {

  private final List<TestCase> testCases;
  private final long total;

  private TestResults(List<TestCase> testCases) {
    this.testCases = testCases;
    this.total = testCases.size();
  }

  public static TestResults of(String filename) {
    var document = DocumentFactory.create(filename);
    var testElements = Elements.getTestElements(document);

    var testResults = testElements.stream()
        .map(TestCase::of)
        .collect(Collectors.toList());

    return new TestResults(testResults);
  }

  public void print() {
    print(JSON, CONSOLE);
  }

  public void printToCSV() {
    print(CSV, FILE);
  }

  public void print(PrintFormat format, PrintOutput output) {
    var content = format.toString(this);
    output.execute(content);
  }

  public TestResults lessThan(long duration) {
    return new TestResults(testCases.stream()
        .filter(testResult -> testResult.getDuration() < duration)
        .collect(Collectors.toList())
    );
  }

  public TestResults greaterThan(long duration) {
    return new TestResults(testCases.stream()
        .filter(testResult -> testResult.getDuration() > duration)
        .collect(Collectors.toList())
    );
  }

  public TestResults naturalOrder() {
    return new TestResults(testCases.stream()
        .sorted(Comparator.comparingLong(TestCase::getDuration))
        .collect(Collectors.toList())
    );
  }

  public TestResults reverseOrder() {
    return new TestResults(testCases.stream()
        .sorted(Comparator.comparingLong(TestCase::getDuration).reversed())
        .collect(Collectors.toList())
    );
  }

  public TestResults error() {
    return status(ERROR);
  }

  public TestResults ignored() {
    return status(IGNORED);
  }

  public TestResults failed() {
    return status(FAILED);
  }

  public TestResults passed() {
    return status(PASSED);
  }

  public long count() {
    return testCases.size();
  }

  public TestResults limit(long limit) {
    return new TestResults(testCases.stream()
        .limit(limit)
        .collect(Collectors.toList()));
  }

  private TestResults status(Status status) {
    return new TestResults(testCases.stream()
        .filter(testResult -> testResult.getStatus() == status)
        .collect(Collectors.toList())
    );
  }


  @Override
  public String toString() {
    var result = "{ ";
    result += "\n";
    result += "    \"testResults\" : [";
    result += "\n";

    for (int i = 0; i < testCases.size() ; i++) {
      var testResult = testCases.get(i);
      result += "      { \"name\": " + "\"" + testResult.getName() + "\"" +  ", \"status\" : " + "\"" + testResult.getStatus() + "\"" + ", \"duration\" : " + testResult.getDuration() + " }";

      if (i != testCases.size() - 1) {
        result += ",";
      }

      result += "\n";
    }

    result += "  ],";
    result += "\n";
    result += "  \"total\" : " + total + "\n";
    result += "}";

    return result;
  }

  public List<TestCase> getTestCases() {
    return testCases;
  }
}