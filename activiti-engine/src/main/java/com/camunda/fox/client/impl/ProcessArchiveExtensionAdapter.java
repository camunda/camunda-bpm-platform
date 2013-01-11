package com.camunda.fox.client.impl;

import com.camunda.fox.client.impl.spi.ProcessArchiveExtension;

/**
 * Adapter for implementing {@link ProcessArchiveExtension
 * ProcessArchiveExtensions}
 * 
 * @author Daniel Meyer
 * @deprecated use {@link ProcessApplicationExtensionAdapter}
 * 
 */
@Deprecated
public class ProcessArchiveExtensionAdapter implements ProcessArchiveExtension {

  public void beforeProcessArchiveStart(ProcessApplication processArchiveSupport) {
  }

  public void afterProcessArchiveStart(ProcessApplication processArchiveSupport) {
  }

  public void beforeProcessArchiveStop(ProcessApplication processArchiveSupport) {
  }

  public void afterProcessArchiveStop(ProcessApplication processArchiveSupport) {
  }

}
