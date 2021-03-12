package org.camunda.bpm.client.spring.helper;

/**
 * Dirty and ugly helper for handling null values with annotations and the task
 * client builders
 */
public final class AnnotationNullValueHelper {

  public static final String NULL_VALUE_STRING = "org.camunda.bpm.client.spring.helper.AnnotationNullValueHelper_NULL_VALUE_STRING";
  public static final long NULL_VALUE_LONG = Long.MIN_VALUE;

  private AnnotationNullValueHelper() {
    // helper
  }

  public static <T> T respectNullValue(T value, Object nullValueConstant) {
    if (value == nullValueConstant || nullValueConstant.equals(value)) {
      return null;
    }
    return value;
  }

  public static <T> T[] respectNullValue(T[] values, Object nullValueConstant) {
    if (values == null) {
      return null;
    }
    if (values.length == 1 && (values[0] == nullValueConstant || nullValueConstant.equals(values[0]))) {
      return null;
    }
    return values;
  }

  public static String respectNullValue(String value) {
    return respectNullValue(value, NULL_VALUE_STRING);
  }

  public static String[] respectNullValue(String[] values) {
    return respectNullValue(values, NULL_VALUE_STRING);
  }

  public static Long respectNullValue(Long value) {
    return respectNullValue(value, NULL_VALUE_LONG);
  }

  public static Long[] respectNullValue(Long[] values) {
    return respectNullValue(values, NULL_VALUE_LONG);
  }
}
