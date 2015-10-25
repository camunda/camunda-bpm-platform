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
   * Compares that all not null values are are in an ascending order. The check is done based on the {@link Comparable#compareTo(Object)} method.
   *
   * E.g. if we have {@code minPriority = 10}, {@code priority = 13} and {@code maxPriority = 5} and
   * {@code Integer[] values = {minPriority, priority, maxPriority}}. Then a call to {@link CompareUtil#hasExcludingOrder(Comparable[] values)}
   * will return {@code true}
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code false} if the not null values are in an ascending order or all the values are null, {@code true} otherwise
   */
  public static <T extends Comparable<T>> boolean hasExcludingOrder(T... values) {
    boolean excluding = false;
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
            excluding = true;
            break;
          }
        }
        i++;
      }
    }
    return excluding;
  }

  /**
   * Compares that all not null values are are in an ascending order. The check is done based on the {@link Comparable#compareTo(Object)} method.
   *
   * E.g. if we have {@code minPriority = 10}, {@code priority = 13} and {@code maxPriority = 5} and
   * {@code IList<Integer> values = {minPriority, priority, maxPriority}}. Then a call to {@link CompareUtil#hasExcludingOrder(List values)}
   * will return {@code true}
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code false} if the not null values are in an ascending order or all the values are null, {@code true} otherwise
   */
  public static <T extends Comparable<T>> boolean hasExcludingOrder(List<T> values) {
    boolean excluding = false;
    if (!values.isEmpty()) {
      T min = values.get(0);
      for (T next : values) {
        if (min.compareTo(next) <= 0) {
          min = next;
        } else {
          excluding = true;
          break;
        }
      }
    }
    return excluding;
  }

  /**
   * Checks if the element is contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values does not contain the element, {@code false} otherwise
   */
  public static <T> boolean hasExcludingContains(T element, Collection<T> values) {
    boolean excluding = false;
    if (element != null && values != null) {
      excluding = !values.contains(element);
    }
    return excluding;
  }

  /**
   * Checks if the element is contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values does not contain the element, {@code false} otherwise
   */
  public static <T> boolean hasExcludingContains(T element, T... values) {
    boolean excluding = false;
    if (element != null && values != null) {
      excluding = hasExcludingContains(element, Arrays.asList(values));
    }
    return excluding;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values contain the element, {@code false} otherwise
   */
  public static <T> boolean hasExcludingNotContains(T element, Collection<T> values) {
    boolean excluding = false;
    if (element != null && values != null) {
      excluding = values.contains(element);
    }

    return excluding;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values contain the element, {@code false} otherwise
   */
  public static <T> boolean hasExcludingNotContains(T element, T... values) {
    boolean excluding = false;
    if (element != null && values != null) {
      excluding = hasExcludingNotContains(element, Arrays.asList(values));
    }

    return excluding;
  }
}
