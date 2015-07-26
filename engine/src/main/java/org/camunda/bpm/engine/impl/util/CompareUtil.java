package org.camunda.bpm.engine.impl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Util class for comparisons.
 *
 * @author Filip Hrisafov
 */
public class CompareUtil {

  /**
   * Compares that all not null values are are in an ascending order. The check is done based on the {@link Comparable#compareTo(Object)} method
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code true} if the not null values are in an ascending order or all the values are null, {@code false} otherwise
   */
  public static <T extends Comparable<T>> boolean validateOrder(T... values) {
    boolean valid = true;
    if (values.length > 0) {
      T min = values[0];
      int i = 1;
      while (i < values.length) {
        T value = values[i];
        if (min == null) {
          min = value;
        } else if (value != null) {
          if (min.compareTo(value) <= 0) {
            min = value;
          } else {
            valid = false;
            break;
          }
        }
        i++;
      }
    }
    return valid;
  }

  public static <T extends Comparable<T>> boolean validateOrder(List<T> unsorted) {
    boolean valid = true;
    if (!unsorted.isEmpty()) {
      T min = unsorted.get(0);
      for (T next : unsorted) {
        if (min.compareTo(next) <= 0) {
          min = next;
        } else {
          valid = false;
          break;
        }
      }
    }
    return valid;
  }

  /**
   * Checks if the element is contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code false} if the element and values are not {@code null} and the values does not contain the element, {@code true} otherwise
   */
  public static <T> boolean validateContains(T element, Collection<T> values) {
    boolean valid = true;
    if (element != null && values != null) {
      valid = values.contains(element);
    }
    return valid;
  }

  /**
   * Checks if the element is contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code false} if the element and values are not {@code null} and the values does not contain the element, {@code true} otherwise
   */
  public static <T> boolean validateContains(T element, T... values) {
    boolean valid = true;
    if (element != null && values != null) {
      valid = validateContains(element, Arrays.asList(values));
    }
    return valid;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code false} if the element and values are not {@code null} and the values contain the element, {@code true} otherwise
   */
  public static <T> boolean validateNotContains(T element, Collection<T> values) {
    boolean valid = true;
    if (element != null && values != null) {
      valid = !values.contains(element);
    }

    return valid;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code false} if the element and values are not {@code null} and the values contain the element, {@code true} otherwise
   */
  public static <T> boolean validateNotContains(T element, T... values) {
    boolean valid = true;
    if (element != null && values != null) {
      valid = validateNotContains(element, Arrays.asList(values));
    }

    return valid;
  }
}
