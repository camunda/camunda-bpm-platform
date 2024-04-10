package org.camunda.test.results;

public enum PrintFormat {
  JSON, CSV;

  public String toString(TestResults results) {

    if (this == JSON) {
      return new JsonPrinter(results).toString();
    }

    if (this == CSV) {
      return new CSVPrinter(results).toString();
    }

    throw new UnsupportedOperationException("Format " + this.name() + " is not supported");
  }
}
