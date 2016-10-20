'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/dashboard.html', 'utf8');
var series = require('camunda-commons-ui/node_modules/camunda-bpm-sdk-js').utils.series;

function prioritySort(a, b) {
  return a.priority > b.priority ? 1 : (a.priority < b.priority ? -1 : 0);
}

function resultsCb(cb) {
  return function(err, data) {
    if (err) { return cb(err); }
    cb(null, data.count);
  };
}

var Controller = [
  '$scope',
  'camAPI',
  '$injector',
  'Views',
  'hasPlugin',
  'page',
  function(
  $scope,
  camAPI,
  $injector,
  Views,
  hasPlugin,
  page
) {
    $scope.hasProcessSearch = hasPlugin('cockpit.processes.dashboard', 'search-process-instances');
    $scope.hasCaseSearch = hasPlugin('cockpit.cases.dashboard', 'case-instances-search');
    $scope.hasTaskSearch = hasPlugin('cockpit.tasks.dashboard', 'search-tasks');

    $scope.mainPlugins = [];
    $scope.miscPlugins = [];

    Views.getProviders({
      component: 'cockpit.dashboard.section'
    }).forEach(function(plugin) {
      (plugin.priority >= 0 ? $scope.mainPlugins : $scope.miscPlugins).push(plugin);
      if (plugin.getSparklineData) {
        if (typeof plugin.getSparklineData === 'function') {
          plugin.sparklineData = plugin.getSparklineData();
        }
        else if (Array.isArray(plugin.getSparklineData)) {
          plugin.sparklineData = $injector.invoke(plugin.getSparklineData);
        }
      }
    });

  // old plugins are still shown on the dashboard
    $scope.dashboardVars = { read: [ 'processData' ] };
    $scope.deprecateDashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});



  // reset breadcrumbs
    page.breadcrumbsClear();

    page.titleSet('Dashboard');

  // ----------------------------------------------------------------------------------------

    var caseDefResource = camAPI.resource('case-definition');
    var caseInstResource = camAPI.resource('case-instance');
    var decisionDefResource = camAPI.resource('decision-definition');
    var deploymentResource = camAPI.resource('deployment');
    var historyResource = camAPI.resource('history');
    var procDefResource = camAPI.resource('process-definition');
    var procInstResource = camAPI.resource('process-instance');
    var taskResource = camAPI.resource('task');

    function fetchActual(cb) {
      // 1: GET /process-instance/count (query param for link is "unfinished")
      // 2: GET /history/process-instance/count?withIncidents=true&incidentStatus=open
      // 3: GET /case-instance/count
      // 4: GET: /task/count
      series({
        runningProcessInstances: function(next) {
          procInstResource.count({}, resultsCb(next));
        },
        openIncidents: function(next) {
          historyResource.processInstanceCount({
            withIncidents: true,
            incidentStatus: 'open'
          }, resultsCb(next));
        },
        caseInstances: function(next) {
          caseInstResource.count({}, next);
        },
        tasks: function(next) {
          taskResource.count({}, next);
        }
      }, function(err, results) {
        if (err) { throw err; }
        cb(err, results);
      });
    }

    function fetchDeployed(cb) {
      // 5: GET /process-definition/count
      // 6: GET /decision-definition/count
      // 7: GET /case-definition/count
      // 8: GET /deployment/count
      series({
        processDefinitions: function(next) {
          procDefResource.count({}, next);
        },
        decisionDefinitions: function(next) {
          decisionDefResource.count({}, next);
        },
        caseDefinitions: function(next) {
          caseDefResource.count({}, next);
        },
        deploymentDefinitions: function(next) {
          deploymentResource.count({}, next);
        }
      }, function(err, results) {
        if (err) { throw err; }
        cb(err, results);
      });
    }

    $scope.loading = false;
    function fetchData() {
      $scope.loading = true;
      series({
        actual: fetchActual,
        deployed: fetchDeployed
      }, function(err, results) {
        $scope.loading = false;
        if (err) { throw err; }
        $scope.data = results;
      });
    }

    fetchData();

  // ----------------------------------------------------------------------------------------

    $scope.metricsPeriod = 'day';
    $scope.setMetricsPeriod = function(period) {
      $scope.metricsPeriod = period;
    };

    $scope.metricsVars = { read: [ 'metricsPeriod' ] };
    $scope.metricsPlugins = Views.getProviders({
      component: 'cockpit.dashboard.metrics'
    }).sort(prioritySort);
  }];

var RouteConfig = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/dashboard', {
    template: template,
    controller: Controller,
    authentication: 'required',
    reloadOnSearch: false
  });
}];

module.exports = RouteConfig;
