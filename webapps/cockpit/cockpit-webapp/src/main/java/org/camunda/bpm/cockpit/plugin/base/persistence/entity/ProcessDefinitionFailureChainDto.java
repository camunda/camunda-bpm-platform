package org.camunda.bpm.cockpit.plugin.base.persistence.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nico.rehwaldt
 */
public class ProcessDefinitionFailureChainDto {

  private String procDefId_0;
  private String procDefKey_0;
  private String procDefName_0;
  private long procDefVersion_0;

  private String procDefId_1;
  private String procDefKey_1;
  private String procDefName_1;
  private long procDefVersion_1;

  private String procDefId_2;
  private String procDefKey_2;
  private String procDefName_2;
  private long procDefVersion_2;

  private String procDefId_3;
  private String procDefKey_3;
  private String procDefName_3;
  private long procDefVersion_3;

  private long count;

  public ProcessDefinitionFailureChainDto() {

  }

  public ProcessDefinitionFailureChainDto(long count, ProcessDefinitionFailureDto ... failures) {

    this.count = count;

    if (failures.length > 0) {
      ProcessDefinitionFailureDto f0 = failures[0];
      procDefId_0 = f0.id;
      procDefKey_0 = f0.key;
      procDefName_0 = f0.name;
      procDefVersion_0 = f0.version;
    }

    if (failures.length > 1) {
      ProcessDefinitionFailureDto f1 = failures[1];
      procDefId_1 = f1.id;
      procDefKey_1 = f1.key;
      procDefName_1 = f1.name;
      procDefVersion_1 = f1.version;
    }

    if (failures.length > 2) {
      ProcessDefinitionFailureDto f2 = failures[2];
      procDefId_2 = f2.id;
      procDefKey_2 = f2.key;
      procDefName_2 = f2.name;
      procDefVersion_2 = f2.version;
    }

    if (failures.length > 3) {
      ProcessDefinitionFailureDto f3 = failures[3];
      procDefId_3 = f3.id;
      procDefKey_3 = f3.key;
      procDefName_3 = f3.name;
      procDefVersion_3 = f3.version;
    }

    if (failures.length > 4) {
      throw new UnsupportedOperationException("May only store 4 failures");
    }
  }

  public List<ProcessDefinitionFailureDto> getFailureChain() {
    List<ProcessDefinitionFailureDto> failures = new ArrayList<ProcessDefinitionFailureDto>();

    failures.add(createFailure(procDefId_0, procDefKey_0, procDefName_0, procDefVersion_0));

    if (procDefId_1 != null) {
      failures.add(createFailure(procDefId_1, procDefKey_1, procDefName_1, procDefVersion_1));
    }

    if (procDefId_2 != null) {
      failures.add(createFailure(procDefId_2, procDefKey_2, procDefName_2, procDefVersion_2));
    }

    if (procDefId_3 != null) {
      failures.add(createFailure(procDefId_3, procDefKey_3, procDefName_3, procDefVersion_3));
    }

    return failures;
  }

  public long getCount() {
    return count;
  }

  @Override
  public String toString() {
    return String.format("ProcessDefinitionFailureChainDto(chain=%s, count=%s)", getFailureChain(), count);
  }

  private ProcessDefinitionFailureDto createFailure(String id, String key, String name, long version) {
    return new ProcessDefinitionFailureDto(id, key, name, version);
  }

  public static class ProcessDefinitionFailureDto {

    private final long version;
    private final String name;
    private final String key;
    private final String id;

    public ProcessDefinitionFailureDto(String id, String key, String name, long version) {
      this.id = id;
      this.key = key;
      this.name = name;
      this.version = version;
    }

    @Override
    public String toString() {
      return String.format("ProcessDefinitionFailure(id=%s, name=%s, key=%s, version=%s)", id, key, name, version);
    }
  }
}
