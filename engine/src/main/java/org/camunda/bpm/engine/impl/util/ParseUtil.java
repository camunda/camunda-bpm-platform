package org.camunda.bpm.engine.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;

public class ParseUtil {

  protected static final Pattern REGEX_TTL_ISO = Pattern.compile("^P(\\d+)D$");

  /**
   * Parse History Time To Live in ISO-8601 format to integer and set into the given entity
   * @param historyTimeToLive
   */
  public static Integer parseHistoryTimeToLive(String historyTimeToLive) {
    Integer timeToLive = null;

    if (historyTimeToLive != null && !historyTimeToLive.isEmpty()) {
      Matcher matISO = REGEX_TTL_ISO.matcher(historyTimeToLive);
      if (matISO.find()) {
        historyTimeToLive = matISO.group(1);
      }
      timeToLive = parseIntegerAttribute("historyTimeToLive", historyTimeToLive);
    }

    if (timeToLive != null && timeToLive < 0) {
      throw new NotValidException("Cannot parse historyTimeToLive: negative value is not allowed");
    }

    return timeToLive;
  }

  protected static Integer parseIntegerAttribute(String attributeName, String text) {
    Integer result = null;

    if (text != null && !text.isEmpty()) {
      try {
        result = Integer.parseInt(text);
      }
      catch (NumberFormatException e) {
        throw new ProcessEngineException("Cannot parse " + attributeName + ": " + e.getMessage());
      }
    }

    return result;
  }

  public static ArrayList<String> parseRetryIntervals(String failedJobRetryIntervals) {
    return new ArrayList<String>(Arrays.asList(failedJobRetryIntervals.trim().split("\\s*,\\s*")));
  }
}
