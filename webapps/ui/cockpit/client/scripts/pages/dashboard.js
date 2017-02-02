'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/dashboard.html', 'utf8');
var series = require('camunda-bpm-sdk-js').utils.series;

function prioritySort(a, b) {
  return a.priority > b.priority ? 1 : (a.priority < b.priority ? -1 : 0);
}

function valuesSort(a, b) {
  return a.value > b.value ? 1 : (a.value < b.value ? -1 : 0);
}

function replaceAll(str, obj) {
  Object.keys(obj).forEach(function(searched) {
    var replaced = obj[searched];
    str = str.split('{{' + searched + '}}').join(replaced);
  });
  return str;
}

function color(i, t) {
  var hue = (360 / t) * i;
  hue += 230;
  hue = hue > 360 ? hue-360 : hue;
  return 'hsl(' + hue + ', 70%, 41%)';
}

function valuesTreshold(l, t) {
  return l > 12 ? (Math.floor(t / l) * 0.5) : 0;
}

var Controller = [
  '$scope',
  'camAPI',
  'localConf',
  'Views',
  'hasPlugin',
  'page',
  'Data',
  'dataDepend',
  function(
  $scope,
  camAPI,
  localConf,
  Views,
  hasPlugin,
  page,
  Data,
  dataDepend
) {
    $scope.hasMetricsPlugin = hasPlugin('cockpit.dashboard.metrics', 'executed-activity-instances');
    $scope.hasProcessSearch = hasPlugin('cockpit.processes.dashboard', 'search-process-instances');
    $scope.hasCaseSearch = hasPlugin('cockpit.cases.dashboard', 'case-instances-search');
    $scope.hasTaskSearch = hasPlugin('cockpit.tasks.dashboard', 'search-tasks');

    $scope.mainPlugins = [];
    $scope.miscPlugins = [];

  // old plugins are still shown on the dashboard
    $scope.dashboardVars = { read: [ 'processData' ] };
    $scope.deprecateDashboardProviders = Views.getProviders({ component: 'cockpit.dashboard'});



  // reset breadcrumbs
    page.breadcrumbsClear();
    $scope.$root.showBreadcrumbs = false;
    page.titleSet('Dashboard');

  // ----------------------------------------------------------------------------------------

    $scope.values = {
      procInst: [],
      procIncid: []
    };

    $scope.data = {
      actual: {}
    };



    $scope.linkBase = {
      processInstances: '/process-definition/{{id}}',
      processIncidents: '/process-definition/{{id}}',
      tasks: '/process-instance/{{processInstanceId}}/runtime?tab=user-tasks-tab'
    };

    if ($scope.hasProcessSearch) {
      $scope.linkBase.processInstances = '/processes?searchQuery=%5B%7B%22type%22:%22PIunfinished%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22PIprocessDefinitionKey%22,%22operator%22:%22eq%22,%22value%22:%22{{key}}%22,%22name%22:%22%22%7D%5D';
      $scope.linkBase.processIncidents = '/processes?searchQuery=%5B%7B%22type%22:%22PIwithIncidents%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22PIincidentStatus%22,%22operator%22:%22eq%22,%22value%22:%22open%22,%22name%22:%22%22%7D,%7B%22type%22:%22PIprocessDefinitionKey%22,%22operator%22:%22eq%22,%22value%22:%22{{key}}%22,%22name%22:%22%22%7D%5D';
    }

    function prepareValues(values, total, url) {
      var treshold = valuesTreshold(values.length, total);

      var belowTreshold = {
        value: 0,
        label: 'Others',
        names: [],
        url: url
      };

      values.forEach(function(item) {
        if (item.value && item.value < treshold) {
          belowTreshold.value += item.value;
          belowTreshold.names.push(item.label);
        }
      });

      values = values
        .filter(function(item) {
          return item.value && item.value >= treshold;
        })
        .sort(valuesSort);

      if (treshold) {
        values.unshift(belowTreshold);
      }

      return values.map(function(item, i) {
        item.color = color(i, values.length);
        return item;
      });
    }



    var caseDefResource = camAPI.resource('case-definition');
    var decisionDefResource = camAPI.resource('decision-definition');
    var deploymentResource = camAPI.resource('deployment');
    var processDefinitionService = camAPI.resource('process-definition');


    $scope.processData = dataDepend.create($scope);
    var processData = $scope.processData.newChild($scope);
    Data.instantiateProviders('cockpit.dashboard.data', {$scope: $scope, processData : processData});


    function aggregateInstances(processDefinitionStatistics) {
      var values = [];
      var totalInstances = 0;

      processDefinitionStatistics.forEach(function(statistic) {
        var processDefId = statistic.definition.id;
        var foundIds = $scope.processDefinitionData.filter(function(pd) {
          return pd.id === processDefId;
        });

        var foundObject = foundIds[0];
        if(foundObject && statistic.instances) {
          values.push({
            value: statistic.instances,
            label: foundObject.name || foundObject.id,
            url: replaceAll($scope.linkBase.processInstances, foundObject)
          });

          totalInstances += statistic.instances;
        }
      });

      $scope.values.procInst = prepareValues(values, totalInstances, '/processes');

      $scope.data.actual.runningProcessInstances = totalInstances;
    }


    function aggregateIncidents(processDefinitionStatistics) {
      var values = [];
      var totalIncidents = 0;

      processDefinitionStatistics.forEach(function(statistic) {
        var definitionIncidents = 0;
        statistic.incidents.forEach(function(info) {
          definitionIncidents += info.incidentCount;
        });

        values.push({
          value: definitionIncidents,
          label: statistic.definition.name || statistic.definition.id,
          url: replaceAll($scope.linkBase.processIncidents, statistic.definition)
        });

        totalIncidents += definitionIncidents;
      });

      $scope.values.procIncid = prepareValues(values, totalIncidents, '/processes');

      $scope.data.actual.openIncidents = totalIncidents;
    }


    var taskResource = camAPI.resource('task');
    $scope.$watch('actualActive', function() {
      if (!$scope.actualActive) { return; }

      $scope.loadingActual = true;
      series({
        processes: function(next) {
          processDefinitionService.list({
            latest: true
          }, function(err, data) {
            if (err) {
              return next(err);
            }

            $scope.processDefinitionData = data.items;

            processData.observe('processDefinitionStatistics', function(processDefinitionStatistics) {
              aggregateInstances(processDefinitionStatistics);
              aggregateIncidents(processDefinitionStatistics);
            });

            next();
          });
        },
        tasks: function(next) {
          taskResource.count({}, function(err, total) {
            if (err) { return next(); }

            $scope.data.actual.tasks = total;

            series({
              assignedToUser: function(done) {
                taskResource.count({
                  unfinished: true,
                  assigned: true
                }, function(err, value) {
                  done(err, {
                    label: 'Assigned to a user',
                    url: '/tasks?searchQuery=%5B%7B%22type%22:%22unfinished%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22assigned%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D%5D',
                    value: value
                  });
                });
              },
              assignedToGroup: function(done) {
                taskResource.count({
                  unfinished: true,
                  unassigned: true,
                  withCandidateGroups: true
                }, function(err, value) {
                  done(err, {
                    label: 'Assigned to 1 or more groups',
                    url: '/tasks?searchQuery=%5B%7B%22type%22:%22unfinished%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22withCandidateGroups%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22unassigned%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D%5D',
                    value: value
                  });
                });
              },
              unassigned: function(done) {
                taskResource.count({
                  unfinished: true,
                  unassigned: true,
                  withoutCandidateGroups: true
                }, function(err, value) {
                  done(err, {
                    label: 'Unassigned',
                    url: '/tasks?searchQuery=%5B%7B%22type%22:%22unfinished%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22withoutCandidateGroups%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D,%7B%22type%22:%22unassigned%22,%22operator%22:%22eq%22,%22value%22:%22%22,%22name%22:%22%22%7D%5D',
                    value: value
                  });
                });
              }
            }, function(err, results) {
              if (err) { return next(err); }

              var values = [];
              Object.keys(results).forEach(function(key) {
                values.push(results[key]);
              });

              $scope.values.tasks = prepareValues(values, total, '/tasks');

              next(null, total);
            });
          });
        }
      }, function() {
        $scope.loadingActual = false;
      });
    });



    function fetchDeployed(cb) {
      // 5: GET /process-definition/count?latestVersion=true
      // 6: GET /decision-definition/count?latestVersion=true
      // 7: GET /case-definition/count?latestVersion=true
      // 8: GET /deployment/count
      series({
        processDefinitions: function(next) {
          processDefinitionService.count({
            latestVersion: true
          }, next);
        },
        decisionDefinitions: function(next) {
          decisionDefResource.count({
            latestVersion: true
          }, next);
        },
        caseDefinitions: function(next) {
          caseDefResource.count({
            latestVersion: true
          }, next);
        },
        deploymentDefinitions: function(next) {
          deploymentResource.count({}, next);
        }
      }, function(err, results) {
        if (err) { throw err; }
        cb(err, results);
      });
    }

    $scope.$watch('deployedActive', function() {
      if (!$scope.deployedActive || $scope.data.deployed) { return; }

      $scope.loadingDeployed = true;
      fetchDeployed(function(err, results) {
        $scope.loadingDeployed = false;
        if (err) { throw err; }

        $scope.data.deployed = results;
      });
    });

  // ----------------------------------------------------------------------------------------
    [
      'actual',
      'metrics',
      'deployed',
      'deprecate'
    ].forEach(function(name) {
      $scope[name + 'Active'] = localConf.get('dashboardSection:' + name, true);
    });
    $scope.toggleSection = function(name) {
      $scope[name + 'Active'] = !$scope[name + 'Active'];
      localConf.set('dashboardSection:' + name, $scope[name + 'Active']);
    };

    $scope.metricsPeriod = localConf.get('dashboardMetricsPeriod', 'day');
    $scope.setMetricsPeriod = function(period) {
      $scope.metricsPeriod = period;
      localConf.set('dashboardMetricsPeriod', period);
    };

    if ($scope.hasMetricsPlugin) {
      $scope.metricsVars = { read: [ 'metricsPeriod' ] };
      $scope.metricsPlugins = Views.getProviders({
        component: 'cockpit.dashboard.metrics'
      }).sort(prioritySort);
    }
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
