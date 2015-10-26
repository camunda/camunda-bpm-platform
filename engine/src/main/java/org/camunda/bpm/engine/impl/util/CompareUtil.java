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
   * {@code Integer[] values = {minPriority, priority, maxPriority}}. Then a call to {@link CompareUtil#areNotInAnAscendingOrder(Comparable[] values)}
   * will return {@code true}
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code false} if the not null values are in an ascending order or all the values are null, {@code true} otherwise
   */
  public static <T extends Comparable<T>> boolean areNotInAnAscendingOrder(T... values) {
    boolean excluding = false;
    if (values != null) {
      excluding = areNotInAnAscendingOrder(Arrays.asList(values));
    }
    return excluding;
  }

  /**
   * Compares that all not null values are are in an ascending order. The check is done based on the {@link Comparable#compareTo(Object)} method.
   *
   * E.g. if we have {@code minPriority = 10}, {@code priority = 13} and {@code maxPriority = 5} and
   * {@code List<Integer> values = {minPriority, priority, maxPriority}}. Then a call to {@link CompareUtil#areNotInAnAscendingOrder(List values)}
   * will return {@code true}
   *
   * @param values to validate
   * @param <T> the type of the comparable
   * @return {@code false} if the not null values are in an ascending order or all the values are null, {@code true} otherwise
   */
  public static <T extends Comparable<T>> boolean areNotInAnAscendingOrder(List<T> values) {
    boolean excluding = false;
    if (!values.isEmpty()) {
      int lastNotNul = -1;
      for (int i = 0; i< values.size(); i++) {
        T value = values.get(i);
        if (value != null) {
          if (lastNotNul != -1 && values.get(lastNotNul).compareTo(value) > 0) {
            excluding = true;
            break;
          }
          lastNotNul = i;
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
  public static <T> boolean elementIsNotContainedInList(T element, Collection<T> values) {
    boolean notCaontained = false;
    if (element != null && values != null) {
      notCaontained = !values.contains(element);
    }
    return notCaontained;
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
    boolean notContained = false;
    if (element != null && values != null) {
      notContained = elementIsNotContainedInList(element, Arrays.asList(values));
    }
    return notContained;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values contain the element, {@code false} otherwise
   */
  public static <T> boolean elementIsContainedInList(T element, Collection<T> values) {
    boolean contained = false;
    if (element != null && values != null) {
      contained = values.contains(element);
    }

    return contained;
  }

  /**
   * Checks if the element is not contained within the list of values. If the element, or the list are null then true is returned.
   *
   * @param element to check
   * @param values to check in
   * @param <T> the type of the element
   * @return {@code true} if the element and values are not {@code null} and the values contain the element, {@code false} otherwise
   */
  public static <T> boolean elementIsContainedInArray(T element, T... values) {
    boolean contained = false;
    if (element != null && values != null) {
      contained = elementIsContainedInList(element, Arrays.asList(values));
    }

    return contained;
  }
}
