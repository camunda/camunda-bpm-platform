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
 * Abstract class that walks through a tree hierarchy. The subclass define the
 * type of tree and provide the concrete walking behavior.
 *
 * @author Thorben Lindhauer
 *
 */
public abstract class TreeWalker<T> {

  protected T currentElement;

  protected List<TreeVisitor<T>> preVisitor = new ArrayList<TreeVisitor<T>>();

  protected List<TreeVisitor<T>> postVisitor = new ArrayList<TreeVisitor<T>>();

  protected abstract T nextElement();

  public TreeWalker(T initialElement) {
    currentElement = initialElement;
  }

  public TreeWalker<T> addPreVisitor(TreeVisitor<T> collector) {
    this.preVisitor.add(collector);
    return this;
  }

  public TreeWalker<T> addPostVisitor(TreeVisitor<T> collector) {
    this.postVisitor.add(collector);
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
      for (TreeVisitor<T> collector : preVisitor) {
        collector.visit(currentElement);
      }

      currentElement = nextElement();

      for (TreeVisitor<T> collector : postVisitor) {
        collector.visit(currentElement);
      }
    }
    return getCurrentElement();
  }

  public T walkUntil(WalkCondition<T> condition) {
    do {
      for (TreeVisitor<T> collector : preVisitor) {
        collector.visit(currentElement);
      }

      currentElement = nextElement();

      for (TreeVisitor<T> collector : postVisitor) {
        collector.visit(currentElement);
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

  public static class NullCondition<S> implements WalkCondition<S> {

    public boolean isFulfilled(S element) {
      return element == null;
    }

    public static <S> WalkCondition<S> notNull() {
      return new NullCondition<S>();
    }

  }
}
