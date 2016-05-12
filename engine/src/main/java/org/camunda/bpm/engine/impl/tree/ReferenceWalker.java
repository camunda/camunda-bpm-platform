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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>A walker for walking through an object reference structure (e.g. an execution tree).
 * Any visited element can have any number of following elements. The elements are visited
 * with a breadth-first approach: The walker maintains a list of next elements to which it adds
 * a new elements at the end whenever it has visited an element. The walker stops when it encounters
 * an element that fulfills the given {@link WalkCondition}.
 *
 * <p>Subclasses define the type of objects and provide the walking behavior.
 *
 * @author Thorben Lindhauer
 */
public abstract class ReferenceWalker<T> {

  protected List<T> currentElements;

  protected List<TreeVisitor<T>> preVisitor = new ArrayList<TreeVisitor<T>>();

  protected List<TreeVisitor<T>> postVisitor = new ArrayList<TreeVisitor<T>>();

  protected abstract Collection<T> nextElements();

  public ReferenceWalker(T initialElement) {
    currentElements = new LinkedList<T>();
    currentElements.add(initialElement);
  }

  public ReferenceWalker(List<T> initialElements) {
    currentElements = new LinkedList<T>(initialElements);
  }

  public ReferenceWalker<T> addPreVisitor(TreeVisitor<T> collector) {
    this.preVisitor.add(collector);
    return this;
  }

  public ReferenceWalker<T> addPostVisitor(TreeVisitor<T> collector) {
    this.postVisitor.add(collector);
    return this;
  }

  public T walkWhile() {
    return walkWhile(new ReferenceWalker.NullCondition<T>());
  }

  public T walkUntil() {
    return walkUntil(new ReferenceWalker.NullCondition<T>());
  }

  public T walkWhile(ReferenceWalker.WalkCondition<T> condition) {
    while (!condition.isFulfilled(getCurrentElement())) {
      for (TreeVisitor<T> collector : preVisitor) {
        collector.visit(getCurrentElement());
      }

      currentElements.addAll(nextElements());
      currentElements.remove(0);

      for (TreeVisitor<T> collector : postVisitor) {
        collector.visit(getCurrentElement());
      }
    }
    return getCurrentElement();
  }

  public T walkUntil(ReferenceWalker.WalkCondition<T> condition) {
    do {
      for (TreeVisitor<T> collector : preVisitor) {
        collector.visit(getCurrentElement());
      }

      currentElements.addAll(nextElements());
      currentElements.remove(0);

      for (TreeVisitor<T> collector : postVisitor) {
        collector.visit(getCurrentElement());
      }
    } while (!condition.isFulfilled(getCurrentElement()));
    return getCurrentElement();
  }

  public T getCurrentElement() {
    return currentElements.isEmpty() ? null : currentElements.get(0);
  }

  public interface WalkCondition<S> {
    boolean isFulfilled(S element);
  }

  public static class NullCondition<S> implements ReferenceWalker.WalkCondition<S> {

    public boolean isFulfilled(S element) {
      return element == null;
    }

    public static <S> ReferenceWalker.WalkCondition<S> notNull() {
      return new NullCondition<S>();
    }

  }


}
