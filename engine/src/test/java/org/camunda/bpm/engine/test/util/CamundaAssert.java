package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.MissingAuthorization;

import static junit.framework.TestCase.assertEquals;

/**
 * Common Assert class for the Camunda tests.
 *
 * @author Filip Hrisafov
 */
public class CamundaAssert {
  /**
   * Checks if the info has the expected parameters.
   *
   * @param expectedPermissionName to use
   * @param expectedResourceName to use
   * @param expectedResourceId to use
   * @param info to check
   */
  public static void assertExceptionInfo(String expectedPermissionName, String expectedResourceName, String expectedResourceId,
      MissingAuthorization info) {
    assertEquals(expectedPermissionName, info.getViolatedPermissionName());
    assertEquals(expectedResourceName, info.getResourceType());
    assertEquals(expectedResourceId, info.getResourceId());
  }
}
