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
package org.camunda.bpm.dmn.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionResultImpl implements DmnDecisionResult {

  private static final long serialVersionUID = 1L;

  public static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected final List<DmnDecisionResultEntries> ruleResults;

  public DmnDecisionResultImpl(List<DmnDecisionResultEntries> ruleResults) {
    this.ruleResults = ruleResults;
  }

  public DmnDecisionResultEntries getFirstResult() {
    if (size() > 0) {
      return get(0);
    } else {
      return null;
    }
  }

  public DmnDecisionResultEntries getSingleResult() {
    if (size() == 1) {
      return get(0);
    } else if (isEmpty()) {
      return null;
    } else {
      throw LOG.decisionResultHasMoreThanOneOutput(this);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> collectEntries(String outputName) {
    List<T> outputValues = new ArrayList<T>();

    for (DmnDecisionResultEntries ruleResult : ruleResults) {
      if (ruleResult.containsKey(outputName)) {
        Object value = ruleResult.get(outputName);
        outputValues.add((T) value);
      }
    }

    return outputValues;
  }

  @Override
  public List<Map<String, Object>> getResultList() {
    List<Map<String, Object>> entryMapList = new ArrayList<Map<String, Object>>();

    for (DmnDecisionResultEntries ruleResult : ruleResults) {
      Map<String, Object> entryMap = ruleResult.getEntryMap();
      entryMapList.add(entryMap);
    }

    return entryMapList;
  }

  @Override
  public <T> T getSingleEntry() {
    DmnDecisionResultEntries result = getSingleResult();
    if (result != null) {
      return result.getSingleEntry();
    } else {
      return null;
    }
  }

  @Override
  public <T extends TypedValue> T getSingleEntryTyped() {
    DmnDecisionResultEntries result = getSingleResult();
    if (result != null) {
      return result.getSingleEntryTyped();
    } else {
      return null;
    }
  }

  @Override
  public Iterator<DmnDecisionResultEntries> iterator() {
    return asUnmodifiableList().iterator();
  }

  @Override
  public int size() {
    return ruleResults.size();
  }

  @Override
  public boolean isEmpty() {
    return ruleResults.isEmpty();
  }

  @Override
  public DmnDecisionResultEntries get(int index) {
    return ruleResults.get(index);
  }

  @Override
  public boolean contains(Object o) {
    return ruleResults.contains(o);
  }

  @Override
  public Object[] toArray() {
    return ruleResults.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return ruleResults.toArray(a);
  }

  @Override
  public boolean add(DmnDecisionResultEntries e) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return ruleResults.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends DmnDecisionResultEntries> c) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean addAll(int index, Collection<? extends DmnDecisionResultEntries> c) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public DmnDecisionResultEntries set(int index, DmnDecisionResultEntries element) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public void add(int index, DmnDecisionResultEntries element) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public DmnDecisionResultEntries remove(int index) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public int indexOf(Object o) {
    return ruleResults.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return ruleResults.lastIndexOf(o);
  }

  @Override
  public ListIterator<DmnDecisionResultEntries> listIterator() {
    return asUnmodifiableList().listIterator();
  }

  @Override
  public ListIterator<DmnDecisionResultEntries> listIterator(int index) {
    return asUnmodifiableList().listIterator(index);
  }

  @Override
  public List<DmnDecisionResultEntries> subList(int fromIndex, int toIndex) {
    return asUnmodifiableList().subList(fromIndex, toIndex);
  }

  @Override
  public String toString() {
    return ruleResults.toString();
  }

  protected List<DmnDecisionResultEntries> asUnmodifiableList() {
    return Collections.unmodifiableList(ruleResults);
  }

}
