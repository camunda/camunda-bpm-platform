package org.camunda.spin.json.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Svetlana Dorokhova.
 */
@JsonDeserialize(contentAs = RegularCustomer.class)
public class CustomerList<T> implements List<Customer> {

  protected List<Customer> innerList = new ArrayList<Customer>();

  @Override
  public int size() {
    return innerList.size();
  }

  @Override
  public boolean isEmpty() {
    return innerList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return innerList.contains(o);
  }

  @Override
  public Iterator<Customer> iterator() {
    return innerList.iterator();
  }

  @Override
  public Object[] toArray() {
    return innerList.toArray();
  }

  @Override
  public <Customer> Customer[] toArray(Customer[] a) {
    return innerList.toArray(a);
  }

  @Override
  public boolean add(Customer customer) {
    return innerList.add(customer);
  }

  @Override
  public boolean remove(Object o) {
    return innerList.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return innerList.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Customer> c) {
    return innerList.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends Customer> c) {
    return innerList.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return innerList.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return innerList.retainAll(c);
  }

  @Override
  public void clear() {
    innerList.clear();
  }

  @Override
  public boolean equals(Object o) {
    return innerList.equals(o);
  }

  @Override
  public int hashCode() {
    return innerList.hashCode();
  }

  @Override
  public Customer get(int index) {
    return innerList.get(index);
  }

  @Override
  public Customer set(int index, Customer element) {
    return innerList.set(index, element);
  }

  @Override
  public void add(int index, Customer element) {
    innerList.add(index, element);
  }

  @Override
  public Customer remove(int index) {
    return innerList.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return innerList.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return innerList.lastIndexOf(o);
  }

  @Override
  public ListIterator<Customer> listIterator() {
    return innerList.listIterator();
  }

  @Override
  public ListIterator<Customer> listIterator(int index) {
    return innerList.listIterator(index);
  }

  @Override
  public List<Customer> subList(int fromIndex, int toIndex) {
    return innerList.subList(fromIndex, toIndex);
  }
}
