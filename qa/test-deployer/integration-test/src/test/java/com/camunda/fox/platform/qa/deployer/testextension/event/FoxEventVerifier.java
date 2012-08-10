package com.camunda.fox.platform.qa.deployer.testextension.event;

import com.camunda.fox.platform.qa.deployer.event.FoxEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.fest.assertions.Fail;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxEventVerifier {
  
  private List<Object> observedEvents = new ArrayList<Object>();
  
  public void reset() {
    observedEvents.clear();
  }
  
  public void catchFoxEvent(@Observes FoxEvent event) {
    track(event);
  }
  
  private void track(Object o) {
    observedEvents.add(o);
  }
  
  public void verifyOrder(Class<?> ... expectedClasses) {
    
    Iterator<Class<?>> expected = new ArrayIterator<Class<?>>(expectedClasses);
    Iterator<Object> observed = observedEvents.iterator();
    
    System.out.println(Arrays.asList(expectedClasses));
    System.out.println(observedEvents);
    
    while (expected.hasNext()) {
      Class<?> nextExpected = expected.next();
      
      if (scanForNext(observed, instanceOf(nextExpected)) == null) {
        Fail.fail("Did not find instance of expected class " + expected);
      }
    }
    
    Class<?> nextExpected = expected.next();
    while (observed.hasNext()) {
      Object o = observed.next();
      if (nextExpected.isAssignableFrom(o.getClass())) {
        break;
      }
    }
    
    while (expected.hasNext()) {
      nextExpected = expected.next();
      
      if (!observed.hasNext()) {
        Fail.fail("Expected " + nextExpected.getName() + " but did not found any more elements");
      }
      
      Object o = observed.next();
      if (nextExpected.isAssignableFrom(o.getClass())) {
        continue;
      } else {
        Fail.fail("Expected instance of " + nextExpected.getName() + " but found " + o);
      }
    }
  }
  
  private static class ArrayIterator<T> implements Iterator<T> {

    private int next = 0;
    private T[] array;
    
    private ArrayIterator(T[] array) {
      this.array = array;
    }

    public boolean hasNext() {
      return (array.length > next);
    }

    public T next() {
      return array[next++];
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  private abstract static class Matcher<T> {
    public abstract boolean matches(T t);
  }
  
  private <T> T scanForNext(Iterator<T> iterator, Matcher<T> matcher) {
    while (iterator.hasNext()) {
      T next = iterator.next();
      if (matcher.matches(next)) {
        return next;
      }
    }
    
    return null;
  }
  
  private <T> Matcher<T> instanceOf(final Class<?> cls) {
    return new Matcher() {
      public boolean matches(Object o) {
        return cls.isAssignableFrom(o.getClass());
      }
    };
  }
}
