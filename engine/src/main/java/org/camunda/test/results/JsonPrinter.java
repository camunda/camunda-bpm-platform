package org.camunda.test.results;

public class JsonPrinter {

  private final TestResults testResults;

  public JsonPrinter(TestResults testResults) {
    this.testResults = testResults;
  }

  public String toString() {
    var results = testResults.getTestCases();

    var result = "{ ";
    result += "\n";
    result += "    \"testResults\" : [";
    result += "\n";

    for (int i=0; i < results.size() ; i++) {
      var testResult = results.get(i);
      result += "      { \"name\": " + "\"" + testResult.getName() + "\"" +  ", \"status\" : " + "\"" + testResult.getStatus() + "\"" + ", \"duration\" : " + testResult.getDuration() + " }";

      if (i != results.size() - 1) {
        result += ",";
      }

      result += "\n";
    }

    result += "  ],";
    result += "\n";
    result += "  \"total\" : " + results.size() + "\n";
    result += "}";

    return result;
  }

}
