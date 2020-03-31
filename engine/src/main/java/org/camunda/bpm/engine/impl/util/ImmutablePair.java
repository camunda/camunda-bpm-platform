/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util;

import java.io.Serializable;
import java.util.Map.Entry;

import java.util.Objects;

/**
 * Immutable representation of a 2-tuple of elements.
 *
 * Although the implementation is immutable, there is no restriction on the
 * objects that may be stored. If mutable objects are stored in the pair, then
 * the pair itself effectively becomes mutable.
 *
 * ThreadSafe if both paired objects are thread-safe
 *
 * @param <L>
 *          the type of the left element
 * @param <R>
 *          the type of the right element
 */
public class ImmutablePair<L, R> implements Entry<L, R>, Serializable, Comparable<ImmutablePair<L, R>> {

  /** Serialization version */
  private static final long serialVersionUID = -7043970803192830955L;

  protected L left;
  protected R right;

  /**
   * @return the left element
   */
  public L getLeft() {
    return left;
  }

  /**
   * @return the right element
   */
  public R getRight() {
    return right;
  }

  /**
   * Create a pair of elements.
   *
   * @param left
   *          the left element
   * @param right
   *          the right element
   */
  public ImmutablePair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public final L getKey() {
    return this.getLeft();
  }

  @Override
  public R getValue() {
    return this.getRight();
  }

  /**
   * This is not allowed since the pair itself is immutable.
   *
   * @return never
   * @throws UnsupportedOperationException
   */
  @Override
  public R setValue(R value) {
    throw new UnsupportedOperationException("setValue not allowed for an ImmutablePair");
  }

  /**
   * Compares the pair based on the left element followed by the right element.
   * The types must be {@code Comparable}.
   *
   * @param other
   *          the other pair, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(ImmutablePair<L, R> o) {
    if (o == null) {
      throw new IllegalArgumentException("Pair to compare to must not be null");
    }
    try {
      int leftComparison = compare((Comparable<L>) getLeft(), (Comparable<L>) o.getLeft());
      return leftComparison == 0 ? compare((Comparable<R>) getRight(), (Comparable<R>) o.getRight()) : leftComparison;
    } catch (ClassCastException cce) {
      throw new IllegalArgumentException("Please provide comparable elements", cce);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected int compare(Comparable original, Comparable other) {
    if (original == other) {
      return 0;
    }

    if (original == null) {
      return -1;
    }

    if (other == null) {
      return 1;
    }

    return original.compareTo(other);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof Entry)) {
      return false;
    } else {
      Entry<?, ?> other = (Entry<?, ?>) obj;
      return Objects.equals(this.getKey(), other.getKey()) &&
          Objects.equals(this.getValue(), other.getValue());
    }
  }

  @Override
  public int hashCode() {
    return (this.getKey() == null ? 0 : this.getKey().hashCode()) ^
        (this.getValue() == null ? 0 : this.getValue().hashCode());
  }

  @Override
  public String toString() {
    return "(" + this.getLeft() + ',' + this.getRight() + ')';
  }
}
