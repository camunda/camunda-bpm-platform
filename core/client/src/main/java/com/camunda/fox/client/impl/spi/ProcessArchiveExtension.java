package com.camunda.fox.client.impl.spi;

import com.camunda.fox.client.impl.ProcessArchiveSupport;

/**
 * <p>SPI interface for implementing process archive extensions</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessArchiveExtension {
  
  public void beforeProcessArchiveStart(ProcessArchiveSupport processArchiveSupport);
  public void afterProcessArchiveStart(ProcessArchiveSupport processArchiveSupport);
  
  public void beforeProcessArchiveStop(ProcessArchiveSupport processArchiveSupport);
  public void afterProcessArchiveStop(ProcessArchiveSupport processArchiveSupport);

}
