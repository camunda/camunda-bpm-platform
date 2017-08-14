package org.camunda.bpm.spring.boot.starter.actuator;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

public class JobExecutorHealthIndicator extends AbstractHealthIndicator {

  private final JobExecutor jobExecutor;

  public JobExecutorHealthIndicator(final JobExecutor jobExecutor) {
    this.jobExecutor = requireNonNull(jobExecutor);
  }

  @Override
  protected void doHealthCheck(Builder builder) throws Exception {
    boolean active = jobExecutor.isActive();
    if (active) {
      builder = builder.up();
    } else {
      builder = builder.down();
    }
    builder.withDetail("jobExecutor", Details.from(jobExecutor));
  }

  public static class Details {

    private final String name;
    private final String lockOwner;
    private final int lockTimeInMillis;
    private final int maxJobsPerAcquisition;
    private final int waitTimeInMillis;
    private final Set<String> processEngineNames;
    
    
    private Details(DetailsBuilder builder) {
      name = builder.name;
      lockOwner = builder.lockOwner;
      lockTimeInMillis = builder.lockTimeInMillis;
      maxJobsPerAcquisition = builder.maxJobsPerAcquisition;
      waitTimeInMillis = builder.waitTimeInMillis;
      processEngineNames = java.util.Collections.unmodifiableSet(new HashSet<String>(builder.processEngineNames));
    }

    public static DetailsBuilder builder() {
      return new DetailsBuilder();
    }

    private static Details from(JobExecutor jobExecutor) {
      final DetailsBuilder builder = Details.builder()
        .name(jobExecutor.getName())
        .lockOwner(jobExecutor.getLockOwner())
        .lockTimeInMillis(jobExecutor.getLockTimeInMillis())
        .maxJobsPerAcquisition(jobExecutor.getMaxJobsPerAcquisition())
        .waitTimeInMillis(jobExecutor.getWaitTimeInMillis());

      for (ProcessEngineImpl processEngineImpl : jobExecutor.getProcessEngines()) {
        builder.processEngineName(processEngineImpl.getName());
      }

      return builder.build();
    }

    public static class DetailsBuilder {

      private String name;
      private String lockOwner;
      private int lockTimeInMillis;
      private int maxJobsPerAcquisition;
      private int waitTimeInMillis;
      private Set<String> processEngineNames;

      DetailsBuilder() {}

      public DetailsBuilder name(String name) {
        this.name = name;
        return this;
      }

      public DetailsBuilder lockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
        return this;
      }

      public DetailsBuilder lockTimeInMillis(int lockTimeInMillis) {
        this.lockTimeInMillis = lockTimeInMillis;
        return this;
      }

      public DetailsBuilder maxJobsPerAcquisition(int maxJobsPerAcquisition) {
        this.maxJobsPerAcquisition = maxJobsPerAcquisition;
        return this;
      }

      public DetailsBuilder waitTimeInMillis(int waitTimeInMillis) {
        this.waitTimeInMillis = waitTimeInMillis;
        return this;
      }

      public DetailsBuilder processEngineName(String processEngineName) {
        if (this.processEngineNames == null) {
          this.processEngineNames = new HashSet<String>();
        }
        this.processEngineNames.add(processEngineName);
        return this;
      }

      public DetailsBuilder processEngineNames(Set<? extends String> processEngineNames) {
        if (this.processEngineNames == null) {
          this.processEngineNames = new HashSet<String>();
        }
        this.processEngineNames.addAll(processEngineNames);
        return this;
      }

      public DetailsBuilder clearProcessEngineNames(Set<? extends String> processEngineNames) {
        if (this.processEngineNames != null) {
          this.processEngineNames.clear();
        }
        return this;
      }

      public Details build() {
        return new Details(this);
      }

    }

    public String getName() {
      return name;
    }

    public String getLockOwner() {
      return lockOwner;
    }

    public int getLockTimeInMillis() {
      return lockTimeInMillis;
    }

    public int getMaxJobsPerAcquisition() {
      return maxJobsPerAcquisition;
    }

    public int getWaitTimeInMillis() {
      return waitTimeInMillis;
    }

    public Set<String> getProcessEngineNames() {
      return processEngineNames;
    }

    @Override
    public String toString() {
      return "Details [name=" + name + ", lockOwner=" + lockOwner + ", lockTimeInMillis="
          + lockTimeInMillis + ", maxJobsPerAcquisition=" + maxJobsPerAcquisition
          + ", waitTimeInMillis=" + waitTimeInMillis + ", processEngineNames=" + processEngineNames
          + "]";
    }

  }

}
