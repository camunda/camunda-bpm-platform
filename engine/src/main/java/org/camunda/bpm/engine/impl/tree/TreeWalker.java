/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class TreeWalker<T> {

  protected T currentElement;

  protected List<Collector<T>> preCollectors = new ArrayList<Collector<T>>();

  protected List<Collector<T>> postCollectors = new ArrayList<Collector<T>>();

  protected abstract T nextElement();

  public TreeWalker(T initialElement) {
    currentElement = initialElement;
  }

  public TreeWalker<T> addPreCollector(Collector<T> collector) {
    this.preCollectors.add(collector);
    return this;
  }

  public TreeWalker<T> addPostCollector(Collector<T> collector) {
    this.postCollectors.add(collector);
    return this;
  }

  public void walkWhile() {
    walkWhile(new NullCondition<T>());
  }

  public void walkUntil() {
    walkUntil(new NullCondition<T>());
  }

  public T walkWhile(WalkCondition<T> condition) {
    while (!condition.isFulfilled(currentElement)) {
      for (Collector<T> collector : preCollectors) {
        collector.collect(currentElement);
      }

      currentElement = nextElement();

      for (Collector<T> collector : postCollectors) {
        collector.collect(currentElement);
      }
    }
    return getCurrentElement();
  }

  public T walkUntil(WalkCondition<T> condition) {
    do {
      for (Collector<T> collector : preCollectors) {
        collector.collect(currentElement);
      }

      currentElement = nextElement();

      for (Collector<T> collector : postCollectors) {
        collector.collect(currentElement);
      }
    } while (!condition.isFulfilled(currentElement));
    return getCurrentElement();
  }

  public T getCurrentElement() {
    return currentElement;
  }

  public interface WalkCondition<S> {
    boolean isFulfilled(S element);
  }

  public class NullCondition<S> implements WalkCondition<S> {

    public boolean isFulfilled(S element) {
      return element == null;
    }

  }
}
