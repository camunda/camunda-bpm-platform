define([
  'text!camunda-tasklist-ui/process/modals/cam-tasklist-process-start-modal.html'

], function(
  template
) {

  'use strict';

  return [
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

      ProcessDefinition.list(processDefinitionQuery, function (err, res) {
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

    processData.provide('startForm', ['currentProcessDefinitionId', function (currentProcessDefinitionId) {
      var deferred = $q.defer();

      if (!currentProcessDefinitionId.id) {
        deferred.resolve(null);
      }
      else {
        ProcessDefinition.startForm(currentProcessDefinitionId, function (err, res) {
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
      $modal.open({
        size: 'lg',
        controller: 'camProcessStartModalCtrl',
        template: template,
        resolve: {
          processData: function () { return processData; }
        }
      }).result.then(function(result) {
        if ($scope.tasklistApp && $scope.tasklistApp.refreshProvider) {
          $scope.tasklistApp.refreshProvider.refreshTaskList();
        }
      });
    };



  }];

});
