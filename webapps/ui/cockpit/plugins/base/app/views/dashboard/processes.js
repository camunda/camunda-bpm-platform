'use strict';

var angular = require('angular');
var each = angular.forEach;

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/processes.html', 'utf8');

module.exports = [
  'ViewsProvider',
  function(
  ViewsProvider
) {
    ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
      id: 'processes',
      label: 'Processes',
      template: template,
      pagePath: '#/processes',
      checkActive: function(path) {
      // matches "#/process/", "#/processes" or "#/migration"
        return path.indexOf('#/process') > -1 || path.indexOf('#/migration') > -1;
      },
      controller: [
        '$scope',
        '$filter',
        'Data',
        'dataDepend',
        'Views',
        'camAPI',
        function(
      $scope,
      $filter,
      Data,
      dataDepend,
      Views,
      camAPI
    ) {
          var processData = $scope.processData = dataDepend.create($scope);
          var abbreviate = $filter('abbreviateNumber');

          Data.instantiateProviders('cockpit.dashboard.data', {
            $scope: $scope,
            processData: processData
          });

          var procStats = $scope.procDefStats = {
            definitions: {
              label: [
                'Process definition',
                'Process definitions'
              ],
              link: '#/processes?targetPlugin=process-definition'
            },
            instances: {
              label: [
                'Running instance',
                'Running instances'
              ],
              link: '#/processes?targetPlugin=search-process-instances&searchQuery=%5B%7B%22type%22:%22PIunfinished%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D%5D'
            },
            incidents: {
              label: [
                'Instance with incidents',
                'Instances with incidents'
              ],
              link: '#/processes?targetPlugin=search-process-instances&searchQuery=%5B%7B%22type%22:%22PIwithIncidents%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D%5D'
            }
          };

          $scope.isUndefined = function(v) {
            return typeof v === 'undefined';
          };

          $scope.gimmeDaLabel = function(prop) {
            return prop.label[(prop.value === 1) ? 0 : 1];
          };
          $scope.gimmeDaValue = function(count) {
            return abbreviate(count) || 0;
          };
          $scope.loadingState = 'LOADING';
      // should I mention how much I love AngularJS?
          $scope.procDefStatsKeys = Object.keys($scope.procDefStats);

          var processDefinitionService = camAPI.resource('process-definition');
          processDefinitionService.list({
            latestVersion: true
          }, function(err, data) {
            $scope.processDefinitionData = data.items;

            procStats.definitions.value = data.items.length;
          });

          processData.observe('processDefinitionStatistics', function(defStats) {
            $scope.loadingState = 'LOADED';

            procStats.instances.value = 0;

            each(defStats, function(stats) {
              procStats.instances.value += stats.instances || 0;
            });
          });

          var historyService = camAPI.resource('history');
          historyService.processInstanceCount({
            withIncidents: true
          }, function(err, data) {
            if (err) { throw err; }
            procStats.incidents.value = data.count;
          });

          $scope.hasMigrationPlugin = false;
          try {
            $scope.hasMigrationPlugin = !!angular.module('cockpit.plugin.migration');
          } catch (e) {
            // do nothing
          }
        }],

      priority: 100
    });
  }];
