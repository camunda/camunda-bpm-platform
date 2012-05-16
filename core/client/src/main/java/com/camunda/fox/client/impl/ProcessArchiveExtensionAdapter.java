package com.camunda.fox.client.impl;

import com.camunda.fox.client.impl.spi.ProcessArchiveExtension;

/**
 * Adapter for implementing {@link ProcessArchiveExtension
 * ProcessArchiveExtensions}
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessArchiveExtensionAdapter implements ProcessArchiveExtension {

  public void beforeProcessArchiveStart(ProcessArchiveSupport processArchiveSupport) {
  }

  public void afterProcessArchiveStart(ProcessArchiveSupport processArchiveSupport) {
  }

  public void beforeProcessArchiveStop(ProcessArchiveSupport processArchiveSupport) {
  }

  public void afterProcessArchiveStop(ProcessArchiveSupport processArchiveSupport) {
  }

}
