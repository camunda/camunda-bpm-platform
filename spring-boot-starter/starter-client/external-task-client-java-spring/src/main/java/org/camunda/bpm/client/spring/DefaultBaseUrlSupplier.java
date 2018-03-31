package org.camunda.bpm.client.spring;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class DefaultBaseUrlSupplier implements IdAwareBaseUrlSupplier {

  @Getter
  @Setter
  @NonNull
  private String id;

  @Getter
  @Setter
  @NonNull
  private String baseUrl;

  @Override
  public String get(String id) {
    if (!this.id.equals(id)) {
      throw new IllegalStateException("expected id is '" + this.id + "' but was '" + id + "'");
    }
    return baseUrl;
  }

}
