package org.camunda.bpm.client.spring;

@FunctionalInterface
public interface BaseUrlSupplier {

  String get(String id);

}
