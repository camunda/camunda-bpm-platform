'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-definitions.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processes.dashboard', {
    id: 'process-definition',
    label: 'Deployed Process Definitions',
    template: template,
    controller: [
      '$scope',
      'Views',
      'camAPI',
      'localConf',
      '$translate',
      function($scope, Views, camAPI, localConf, $translate) {

        $scope.headColumns = [
          { class: 'state',    request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_STATE')},
          { class: 'incidents',request: 'incidentCount', sortable: true,  content: $translate.instant('PLUGIN_PROCESS_DEF_INCIDENTS')},
          { class: 'instances',request: 'instances'    , sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_RUNNING_INSTANCES')},
          { class: 'name',     request: 'key'          , sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_NAME')},
          { class: 'tenantID', request: 'tenantId'     , sortable: true, content: $translate.instant('PLUGIN_PROCESS_DEF_TENANT_ID')},
          { class: 'history',  request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_HISTORY_VIEW')},
          { class: 'report',   request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_REPORT')},
          { class: 'action',   request: '', sortable: false, content: $translate.instant('PLUGIN_PROCESS_DEF_ACTION')}
        ];

        // Default sorting
        var defaultValue = { sortBy: 'key', sortOrder: 'asc', sortReverse: false};
        $scope.sortObj   = loadLocal(defaultValue);

        // Update Table
        $scope.onSortChange = function(sortObj) {
          sortObj = sortObj || $scope.sortObj;
          // transforms sortOrder in anqular required boolean;
          sortObj.sortReverse = sortObj.sortOrder !== 'asc';
          saveLocal(sortObj);
          $scope.sortObj = sortObj;
        };



        var getPDIncidentsCount = function(incidents) {
          if(!incidents) {
            return 0;
          }

          return incidents.reduce(function(sum, incident) {
            return sum + incident.incidentCount;
          }, 0);
        };

        var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });

        var processData = $scope.processData.newChild($scope);

        $scope.hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
          return plugin.id === 'history';
        }).length > 0;
        $scope.hasReportPlugin = Views.getProviders({ component: 'cockpit.report' }).length > 0;
        $scope.hasSearchPlugin = Views.getProviders( { component: 'cockpit.processes.dashboard', id: 'search-process-instances' }).length > 0;

        var processDefinitionService = camAPI.resource('process-definition');
        $scope.loadingState = 'LOADING';

        // only get count of process definitions
        var countProcessDefinitions =  function() {
          processDefinitionService.count({
            latest: true
          }, function(err, count) {
            if (err) {
              $scope.loadingState = 'ERROR';
            }
            $scope.processDefinitionsCount = count;
          });
        };

        // get full list of process definitions and related resources
        var listProcessDefinitions =  function() {
          processDefinitionService.list({
            latest: true
          }, function(err, data) {
            console.log(data);

            $scope.processDefinitionData = data.items;
            $scope.processDefinitionsCount = data.count;
            if (err) {
              $scope.loadingState = 'ERROR';
            }

            $scope.loadingState = 'LOADED';

            processData.observe('processDefinitionStatistics', function(processDefinitionStatistics) {
              $scope.statistics = processDefinitionStatistics;

              $scope.statistics.forEach(function(statistic) {
                var processDefId = statistic.definition.id;
                var foundIds = $scope.processDefinitionData.filter(function(pd) {
                  return pd.id === processDefId;
                });

                var foundObject = foundIds[0];
                if(foundObject) {
                  foundObject.incidents = statistic.incidents;
                  foundObject.incidentCount = getPDIncidentsCount(foundObject.incidents);
                  foundObject.instances = statistic.instances;
                }
              });
            });
          });
        };


        $scope.processesActions = Views.getProviders({ component: 'cockpit.processes.action'});
        $scope.hasActionPlugin = $scope.processesActions.length > 0;
        $scope.definitionVars = { read: [ 'pd' ] };

        var removeActionDeleteListener = $scope.$on('processes.action.delete', function(event, definitionId) {

          var definitions = $scope.processDefinitionData;

          for (var i = 0; i < definitions.length; i++) {
            if (definitions[i].id === definitionId) {
              definitions.splice(i, 1);
              break;
            }
          }

          $scope.processDefinitionsCount = definitions.length;
        });

        $scope.$on('$destroy', function() {
          removeActionDeleteListener();
        });

        $scope.activeTab = 'list';

        $scope.selectTab = function(tab) {
          $scope.activeTab = tab;
        };

        $scope.activeSection = localConf.get('processesDashboardActive', true);
        // if tab is not active, it's enough to only get the count of process definitions
        $scope.activeSection ? listProcessDefinitions() : countProcessDefinitions();

        $scope.toggleSection = function toggleSection() {
          // if tab is not active, it's enough to only get the count of process definitions
          ($scope.activeSection = !$scope.activeSection) ? listProcessDefinitions() : countProcessDefinitions();
          localConf.set('processesDashboardActive', $scope.activeSection);
        };

        function saveLocal(sortObj) {
          localConf.set('sortProcessDefTab', sortObj);

        }
        function loadLocal(defaultValue) {
          return localConf.get('sortProcessDefTab', defaultValue);
        }

    /*    function sortTable(data,sortBy,sortOrder) {
          // sort by value
          data.sort(function (a, b) {
            return a.value - b.value;
          });
          return data;
        }

        function sortByValue() {
          return function(a, b) {
            return a.value - b.value;
          };
        }

        function sortByName() {
          return function(a, b) {
            var nameA = a.name.toUpperCase(); // ignore upper and lowercase
            var nameB = b.name.toUpperCase(); // ignore upper and lowercase
            if (nameA < nameB) {
              return -1;
            }
            if (nameA > nameB) {
              return 1;
            }

            // names must be equal
            return 0;
          };
        }
*/






      }],

    priority: 0
  });
}];
