package org.camunda.test.results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum PrintOutput {
  FILE, CONSOLE;

  public void execute(String content) {

    if (this == FILE) {
      try {
        writeToFile(content, "test_results.txt");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return;
    }

    if (this == CONSOLE) {
      System.out.println(content);
      return;
    }

    throw new UnsupportedOperationException(this.name() + " is not supported");
  }

  private void writeToFile(String content, String filename) throws IOException {
    Path path = Paths.get(filename);
    Files.write(path, content.getBytes());
  }
}
