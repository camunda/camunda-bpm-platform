/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;

public class DmnDecisionResultImpl implements DmnDecisionResult {

  private static final long serialVersionUID = 1L;

  public static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected final List<DmnDecisionOutput> decisionOutputs = new ArrayList<DmnDecisionOutput>();

  public void addOutput(DmnDecisionOutput decisionOutput) {
    decisionOutputs.add(decisionOutput);
  }

  public DmnDecisionOutput getFirstOutput() {
    if (size() > 0) {
      return get(0);
    } else {
      return null;
    }
  }

  public DmnDecisionOutput getSingleOutput() {
    if (size() == 1) {
      return get(0);
    } else if (isEmpty()) {
      return null;
    } else {
      throw LOG.decisionResultHasMoreThanOneOutput(this);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> collectOutputValues(String outputName) {
    List<T> outputValues = new ArrayList<T>();

    for (DmnDecisionOutput decisionOutput : decisionOutputs) {
      if (decisionOutput.containsKey(outputName)) {
        Object value = decisionOutput.get(outputName);
        outputValues.add((T) value);
      }
    }

    return outputValues;
  }

  @Override
  public List<Map<String, Object>> getOutputList() {
    List<Map<String, Object>> outputList = new ArrayList<Map<String, Object>>();

    for (DmnDecisionOutput decisionOutput : decisionOutputs) {
      Map<String, Object> valueMap = decisionOutput.getValueMap();
      outputList.add(valueMap);
    }

    return outputList;
  }

  @Override
  public Iterator<DmnDecisionOutput> iterator() {
    return asUnmodifiableList().iterator();
  }

  @Override
  public int size() {
    return decisionOutputs.size();
  }

  @Override
  public boolean isEmpty() {
    return decisionOutputs.isEmpty();
  }

  @Override
  public DmnDecisionOutput get(int index) {
    return decisionOutputs.get(index);
  }

  @Override
  public boolean contains(Object o) {
    return decisionOutputs.contains(o);
  }

  @Override
  public Object[] toArray() {
    return decisionOutputs.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return decisionOutputs.toArray(a);
  }

  @Override
  public boolean add(DmnDecisionOutput e) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return decisionOutputs.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends DmnDecisionOutput> c) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public boolean addAll(int index, Collection<? extends DmnDecisionOutput> c) {
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
  public DmnDecisionOutput set(int index, DmnDecisionOutput element) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public void add(int index, DmnDecisionOutput element) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public DmnDecisionOutput remove(int index) {
    throw new UnsupportedOperationException("decision result is immutable");
  }

  @Override
  public int indexOf(Object o) {
    return decisionOutputs.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return decisionOutputs.lastIndexOf(o);
  }

  @Override
  public ListIterator<DmnDecisionOutput> listIterator() {
    return asUnmodifiableList().listIterator();
  }

  @Override
  public ListIterator<DmnDecisionOutput> listIterator(int index) {
    return asUnmodifiableList().listIterator(index);
  }

  @Override
  public List<DmnDecisionOutput> subList(int fromIndex, int toIndex) {
    return asUnmodifiableList().subList(fromIndex, toIndex);
  }

  @Override
  public String toString() {
    return decisionOutputs.toString();
  }

  protected List<DmnDecisionOutput> asUnmodifiableList() {
    return Collections.unmodifiableList(decisionOutputs);
  }

}
