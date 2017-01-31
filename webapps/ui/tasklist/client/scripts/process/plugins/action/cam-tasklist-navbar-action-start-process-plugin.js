'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

var fs = require('fs');

var startProcessActionTemplate = fs.readFileSync(__dirname + '/cam-tasklist-navbar-action-start-process-plugin.html', 'utf8');
var template = fs.readFileSync(__dirname + '/modals/cam-tasklist-process-start-modal.html', 'utf8');

var Controller = [
  '$scope',
  '$modal',
  '$q',
  'camAPI',
  'dataDepend',
  function(
    $scope,
    $modal,
    $q,
    camAPI,
    dataDepend
  ) {

    var ProcessDefinition = camAPI.resource('process-definition');

    var processData = $scope.processData = dataDepend.create($scope);

    var DEFAULT_PROCESS_DEFINITION_QUERY = {
      latest: true,
      active: true,
      firstResult: 0,
      maxResults: 15
    };

    processData.provide('processDefinitionQuery', DEFAULT_PROCESS_DEFINITION_QUERY);

    processData.provide('processDefinitions', ['processDefinitionQuery', function(processDefinitionQuery) {
      var deferred = $q.defer();

      ProcessDefinition.list(processDefinitionQuery, function(err, res) {
        if(err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;

    }]);

    processData.provide('currentProcessDefinitionId', { id: null });

    processData.provide('startForm', ['currentProcessDefinitionId', function(currentProcessDefinitionId) {
      var deferred = $q.defer();

      if (!currentProcessDefinitionId.id) {
        deferred.resolve(null);
      }
      else {
        ProcessDefinition.startForm(currentProcessDefinitionId, function(err, res) {
          if(err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(res);
          }
        });
      }

      return deferred.promise;
    }]);

    $scope.open = function() {
      processData.set('processDefinitionQuery', angular.copy(DEFAULT_PROCESS_DEFINITION_QUERY));
      var modalInstance = $modal.open({
        size: 'lg',
        controller: 'camProcessStartModalCtrl',
        template: template,
        resolve: {
          processData: function() { return processData; }
        }
      });

      modalInstance.result.then(function() {
        $scope.$root.$broadcast('refresh');
        document.querySelector('.start-process-action a').focus();
      }, function() {
        document.querySelector('.start-process-action a').focus();
      });
    };

    $scope.$on('shortcut:startProcess', $scope.open);

  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('tasklist.navbar.action', {
    id: 'start-process-action',
    template: startProcessActionTemplate,
    controller: Controller,
    priority: 100
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
