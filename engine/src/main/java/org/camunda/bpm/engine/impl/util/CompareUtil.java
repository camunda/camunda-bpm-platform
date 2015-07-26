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
   * Checks if any of the values are not in an ascending order. The check is done based on the {@link Comparable#compareTo(Object)} method.
   *
   * E.g. if we have {@code minPriority = 10}, {@code priority = 13} and {@code maxPriority = 5} and
   * {@code Integer[] values = {minPriority, priority, maxPriority}}. Then a call to {@link CompareUtil#areNotInAscendingOrder(Comparable[] values)}
   * will return {@code true}
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code false} if the not null values are in an ascending order or all the values are null, {@code true} otherwise
   */
  public static <T extends Comparable<T>> boolean areNotInAscendingOrder(T... values) {
    boolean excluding = false;
    if (values != null) {
      excluding = areNotInAscendingOrder(Arrays.asList(values));
    }
    return excluding;
  }

  /**
   * Checks if any of the values are not in an ascending order. The check is done based on the {@link Comparable#compareTo(Object)} method.
   *
   * E.g. if we have {@code minPriority = 10}, {@code priority = 13} and {@code maxPriority = 5} and
   * {@code List<Integer> values = {minPriority, priority, maxPriority}}. Then a call to {@link CompareUtil#areNotInAscendingOrder(List values)}
   * will return {@code true}
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code false} if the not null values are in an ascending order or all the values are null, {@code true} otherwise
   */
  public static <T extends Comparable<T>> boolean areNotInAscendingOrder(List<T> values) {

    int lastNotNull = -1;
    for (int i = 0; i < values.size(); i++) {
      T value = values.get(i);

      if (value != null) {
        if (lastNotNull != -1 && values.get(lastNotNull).compareTo(value) > 0) {
          return true;
        }

        lastNotNull = i;
      }
    }

    return false;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values does not contain the element, {@code false} otherwise
   */
  public static <T> boolean elementIsNotContainedInList(T element, Collection<T> values) {
    if (element != null && values != null) {
      return !values.contains(element);
    }
    else {
      return false;
    }
  }

  /**
   * Checks if the element is contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values does not contain the element, {@code false} otherwise
   */
  public static <T> boolean elementIsNotContainedInArray(T element, T... values) {
    if (element != null && values != null) {
      return elementIsNotContainedInList(element, Arrays.asList(values));
    }
    else {
      return false;
    }
  }

  /**
   * Checks if the element is contained within the list of values.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values contain the element,
   *   {@code false} otherwise
   */
  public static <T> boolean elementIsContainedInList(T element, Collection<T> values) {
    if (element != null && values != null) {
      return values.contains(element);
    }
    else {
      return false;
    }
  }

  /**
   * Checks if the element is contained within the list of values.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values contain the element,
   *   {@code false} otherwise
   */
  public static <T> boolean elementIsContainedInArray(T element, T... values) {
    if (element != null && values != null) {
      return elementIsContainedInList(element, Arrays.asList(values));
    }
    else {
      return false;
    }
  }

  /**
   * Returns any element if obj1.compareTo(obj2) == 0
   */
  public static <T extends Comparable<T>> T min(T obj1, T obj2) {
    return obj1.compareTo(obj2) <= 0 ? obj1 : obj2;
  }

  /**
   * Returns any element if obj1.compareTo(obj2) == 0
   */
  public static <T extends Comparable<T>> T max(T obj1, T obj2) {
    return obj1.compareTo(obj2) >= 0 ? obj1 : obj2;
  }
}
