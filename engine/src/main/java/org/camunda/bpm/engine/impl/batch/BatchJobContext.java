package org.camunda.bpm.engine.impl.batch;

import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;

/**
 * @author Askar Akhmerov
 */
public class BatchJobContext {
  public BatchJobContext(BatchEntity batchEntity, ByteArrayEntity configuration) {
    this.batch = batchEntity;
    this.configuration = configuration;
  }

  protected BatchEntity batch;
  protected ByteArrayEntity configuration;

  public BatchEntity getBatch() {
    return batch;
  }

  public void setBatch(BatchEntity batch) {
    this.batch = batch;
  }

  public ByteArrayEntity getConfiguration() {
    return configuration;
  }

  public void setConfiguration(ByteArrayEntity configuration) {
    this.configuration = configuration;
  }
}
