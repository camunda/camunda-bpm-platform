package org.camunda.test.results;

import org.camunda.bpm.TestCase;

public class CSVPrinter {

  private final TestResults testResults;

  public CSVPrinter(TestResults testResults) {
    this.testResults = testResults;
  }

  public String toString() {
    var result = "";
    var testCases = testResults.getTestCases();

    result += "Name;Status;Duration";
    result += "\n";

    for (TestCase testCase : testCases) {
      result += testCase.getName() + ";" + testCase.getStatus().name() + ";" + testCase.getDuration();
      result += "\n";
    }

    return result;
  }
}
