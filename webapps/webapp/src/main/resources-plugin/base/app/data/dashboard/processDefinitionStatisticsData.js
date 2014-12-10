define(['angular'], function(angular) {

  var Controller = [ '$scope', 'processData', 'ProcessDefinitionResource',
      function ($scope, processData, ProcessDefinitionResource) {

        processData.provide('processDefinitions', function () {
          return ProcessDefinitionResource.queryStatistics({ incidents: true }).$promise;
        });

        processData.provide('processDefinitionStatistics', ['processDefinitions', function(processDefinitions) {
          return aggregateStatistics(processDefinitions);
        }]);

        /**
         * Returns an aggregated list over the statistics.
         *
         * Summarize the statistics of process definitions
         * which have the same process definition key to
         * one item. This item contains the latest process
         * definition name (if there does not exist a name
         * the process definition key will be used as the
         * process definition name).
         *
         * Furthermore, the number of instances, failed jobs
         * and incidents will be aggregated over all versions
         * of a process definition key.
         *
         */
        var aggregateStatistics = function(statistics) {
          // represents a map which contains to definition key
          // the corresponding statistics.
          var statisticsResult = [];
          // the result to return
          var result = [];

          // iterate over assigned statistics
          angular.forEach(statistics, function (currentStatistic) {
            // get the statistic to the definition key of the current item
            var statistic = statisticsResult[currentStatistic.definition.key];

            if (!statistic) {
              // if there does not exists a statistic to the definition key
              // then create a copy of the current item (currentStatistic).
              statistic = angular.copy(currentStatistic);

              if (!statistic.definition.name) {
                // if there does not exist a name then set definition key
                // as the name of the definition.
                statistic.definition.name = statistic.definition.key;
              }

              // put the statistic into the map of statistics
              statisticsResult[statistic.definition.key] = statistic;

              // add the statistic to the result set
              result.push(statistic);

            } else {
              // First save the values of instances, failedJobs and incidents.
              var currentInstances = statistic.instances;
              var currentFailedJobs = statistics.failedJobs;
              var currentIncidents = angular.copy(statistic.incidents);

              if (currentStatistic.definition.version > statistic.definition.version) {
                // if the version of the current statistic, then create copy from them.
                angular.copy(currentStatistic, statistic);

                if (!statistic.definition.name) {
                  // if there does not exist a name then set definition key
                  // as the name of the definition.
                  statistic.definition.name = statistic.definition.key;
                }
              }

              // Add the saved values to the corresponding values of the current statistic
              statistic.instances = currentInstances + currentStatistic.instances;
              statistic.failedJobs = currentFailedJobs + currentStatistic.failedJobs;

              angular.forEach(currentIncidents, function (incident) {
                var incidentType = incident.incidentType;
                var incidentCount = incident.incidentCount;

                var newIncident = true;
                for(var i = 0; i < statistic.incidents.length; i++) {
                  var statisticIncident = statistic.incidents[i];
                  if (statisticIncident.incidentType == incidentType) {
                    statisticIncident.incidentCount = incidentCount + statisticIncident.incidentCount;
                    newIncident = false;
                  }
                }

                if (!!newIncident) {
                  // merge the incidents
                  statistic.incidents.push(incident);
                }

              });

            }
          });

          return result;
        };

  }];

  var Configuration = function PluginConfiguration(DataProvider) {

    DataProvider.registerData('cockpit.dashboard.data', {
      id: 'process-definition-statistics-data',
      controller: Controller
    });
  };

  Configuration.$inject = ['DataProvider'];

  return Configuration;
});
