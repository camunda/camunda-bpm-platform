package org.camunda.bpm.cycle.util;

import java.util.Date;

/**
 * 
 * @author christian.lipphardt@camunda.com
 */
public class DateUtil {

  private static final int SECOND = 1000;

  /**
   * Returns date normalized to seconds accuracy.
   */
  public static Date getNormalizedDate(Date datetime) {
    return getNormalizedDate(datetime.getTime());
  }
  
  /**
   * Returns date normalized to seconds accuracy.
   */
  public static Date getNormalizedDate(Long datetime) {
    return new Date((datetime / SECOND) * SECOND);
  }
  
}
