package org.activiti.engine.management;

import org.activiti.engine.query.Query;

public interface ActivityStatisticsQuery extends Query<ActivityStatisticsQuery, ActivityStatistics> {

  ActivityStatisticsQuery includeFailedJobs();
}
