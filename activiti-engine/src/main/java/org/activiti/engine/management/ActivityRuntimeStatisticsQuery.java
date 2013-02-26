package org.activiti.engine.management;

import org.activiti.engine.query.Query;

public interface ActivityRuntimeStatisticsQuery extends Query<ActivityRuntimeStatisticsQuery, ActivityStatisticsResult> {

  ActivityRuntimeStatisticsQuery includeFailedJobs();
}
