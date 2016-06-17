'use strict';

module.exports = [
  '$scope',
  '$q',
  'camAPI',
  'Notifications',
  'deploymentData',
  'deployment',
  function(
    $scope,
    $q,
    camAPI,
    Notifications,
    deploymentData,
    deployment
  ) {

    var Deployment = camAPI.resource('deployment');
    var ProcessInstance = camAPI.resource('process-instance');
    var CaseInstance = camAPI.resource('case-instance');

    var deleteDeploymentData = deploymentData.newChild($scope);

    var options = $scope.options = {
      cascade: false,
      skipCustomListeners: true
    };

    $scope.deployment = deployment;
    $scope.status;

    $scope.$on('$routeChangeStart', function() {
      $scope.$dismiss();
    });

    // provide /////////////////////////////////////////////////////////

    deleteDeploymentData.provide('processInstanceCount', function() {
      var deferred = $q.defer();

      ProcessInstance.count({
        deploymentId: deployment.id
      }, function(err, res) {

        if (err) {
          // reject error but do not handle the error
          return deferred.reject(err);
        }

        deferred.resolve(res);

      });

      return deferred.promise;
    });

    deleteDeploymentData.provide('caseInstanceCount', function() {
      var deferred = $q.defer();

      CaseInstance.count({
        deploymentId: deployment.id
      }, function(err, res) {

        if (err) {
          // reject error but do not handle the error
          // it can happen that the case engine is disabled,
          // so that an error should be received. In that
          // case it should still be possible to delete
          // the deployment.
          return deferred.reject(err);
        }

        deferred.resolve(res.count);

      });

      return deferred.promise;
    });


    // observe /////////////////////////////////////////////////////////

    $scope.processInstanceCountState = deleteDeploymentData.observe('processInstanceCount', function(count) {
      $scope.processInstanceCount = count;
    });

    $scope.caseInstanceCountState = deleteDeploymentData.observe('caseInstanceCount', function(count) {
      $scope.caseInstanceCount = count;
    });

    // delete deployment ///////////////////////////////////////////////

    $scope.countsLoaded = function() {
      return ($scope.processInstanceCountState && ($scope.processInstanceCountState.$loaded || $scope.processInstanceCountState.$error))
          && ($scope.caseInstanceCountState && ($scope.caseInstanceCountState.$loaded || $scope.caseInstanceCountState.$error));
    };

    var hasInstances = $scope.hasInstances = function() {
      return ($scope.processInstanceCount && $scope.processInstanceCount.count > 0) || $scope.caseInstanceCount > 0;
    };

    $scope.canDeleteDeployment = function() {
      return !options.cascade && hasInstances() ? false : true;
    };

    $scope.getInfoSnippet = function() {
      var info = [ 'There are' ];

      if ($scope.processInstanceCount && $scope.processInstanceCount.count > 0) {
        info.push($scope.processInstanceCount.count);
        info.push('running process');
        ($scope.processInstanceCount && $scope.processInstanceCount.count > 1) ? info.push('instances') : info.push('instance');
      }

      if ($scope.processInstanceCount && $scope.processInstanceCount.count > 0 && $scope.caseInstanceCount > 0) {
        info.push('and');
      }

      if ($scope.caseInstanceCount > 0) {
        info.push($scope.caseInstanceCount);
        info.push('open case');
        $scope.caseInstanceCount > 1 ? info.push('instances') : info.push('instance');
      }

      info.push('which belong to this deployment.');
      info = info.join(' ');

      return info;
    };

    $scope.deleteDeployment = function() {
      $scope.status = 'PERFORM_DELETE';

      Deployment.delete(deployment.id, options, function(err) {

        $scope.status = null;

        if (err) {
          return Notifications.addError({
            status: 'Finished',
            message: 'Could not delete deployment: ' + err.message,
            exclusive: true
          });
        }

        $scope.$close();

      });
    };

  }];
