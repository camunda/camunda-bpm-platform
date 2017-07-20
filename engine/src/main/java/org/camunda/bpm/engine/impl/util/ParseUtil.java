package org.camunda.bpm.engine.impl.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;

public class ParseUtil {

  protected static final Pattern REGEX_TTL_ISO = Pattern.compile("^P(\\d+)D$");

  /**
   * Parse History Time To Live in ISO-8601 format to integer and set into the given entity
   * @param historyTimeToLive
   * @param entity
   */
  public static void parseHistoryTimeToLive(String historyTimeToLive, ResourceDefinitionEntity<?> entity) {
    Integer historyTTL = null;

    if (historyTimeToLive != null && !historyTimeToLive.isEmpty()) {
      Matcher matISO = REGEX_TTL_ISO.matcher(historyTimeToLive);
      if (matISO.find()) {
        historyTimeToLive = matISO.group(1);
      }
      historyTTL = parseIntegerAttribute("historyTimeToLive", historyTimeToLive, false);
    }

    if (historyTTL == null || historyTTL >= 0) {
      entity.setHistoryTimeToLive(historyTTL);
    } else {
      throw new NotValidException("Cannot parse historyTimeToLive: negative value is not allowed");
    }
  }

  public static Integer parseIntegerAttribute(String attributeName, String integerText, boolean required) {
    if (required && (integerText == null || integerText.isEmpty())) {
      throw new ProcessEngineException(attributeName + " is required");
    } else {
      if (integerText != null && !integerText.isEmpty()) {
        try {
          return Integer.parseInt(integerText);
        } catch (NumberFormatException e) {
          throw new ProcessEngineException("Cannot parse " + attributeName + ": " + e.getMessage());
        }
      }
    }
    return null;
  }
}
