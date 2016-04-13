'use strict';

var angular = require('camunda-commons-ui/vendor/angular');
var each = angular.forEach;

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/processes.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'processes',
    label: 'Processes',
    template: template,
    pagePath: '#/processes',
    checkActive: function (path) {
      // matches "#/process/", "#/processes" or "#/migration"
      return path.indexOf('#/process') > -1 || path.indexOf('#/migration') > -1;
    },
    controller: [
      '$scope',
      'Data',
      'dataDepend',
      'camAPI',
    function(
      $scope,
      Data,
      dataDepend,
      camAPI
    ) {
      var processData = $scope.processData = dataDepend.create($scope);

      Data.instantiateProviders('cockpit.dashboard.data', {
        $scope: $scope,
        processData: processData
      });

      var procStats = $scope.procDefStats = {
        definitions: {
          label: [
            'process definition',
            'process definitions'
          ]
        },
        instances: {
          label: [
            'running instance',
            'running instances'
          ]
        },
        incidents: {
          label: [
            'incident',
            'incidents'
          ]
        },
        failedJobs: {
          label: [
            'failed job',
            'failed jobs'
          ]
        }
      };

      $scope.gimmeDaLabel = function (prop) {
        return prop.label[(prop.value === 1) ? 0 : 1];
      };
      $scope.gimmeDaValue = function (count) {
        return count === 0 ? 'No' : count;
      };
      $scope.loadingState = 'LOADING';
      // should I mention how much I love AngularJS?
      $scope.procDefStatsKeys = Object.keys($scope.procDefStats);

      var processDefinitionService = camAPI.resource('process-definition');
      processDefinitionService.list({
        latest: true
      }, function(err, data) {
        $scope.processDefinitionData = data.items;

        procStats.definitions.value = data.items.length;
      });      
      
      processData.observe('processDefinitionStatistics', function (defStats) {
        $scope.loadingState = 'LOADED';

        procStats.incidents.value = 0;
        procStats.instances.value = 0;
        procStats.failedJobs.value = 0;

        each(defStats, function (stats) {
          procStats.instances.value += stats.instances || 0;
          procStats.failedJobs.value += stats.failedJobs || 0;
          procStats.incidents.value += stats.incidents.length;
        });
      });
    }],

    priority: 0
  });
}];
