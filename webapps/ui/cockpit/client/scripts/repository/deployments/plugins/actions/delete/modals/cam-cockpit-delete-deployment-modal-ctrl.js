'use strict';

module.exports = [
  '$scope',
  '$q',
  'camAPI',
  'Notifications',
  'deploymentData',
  'deployment',
  '$translate',
  'configuration',
  function(
    $scope,
    $q,
    camAPI,
    Notifications,
    deploymentData,
    deployment,
    $translate,
    configuration
  ) {

    var Deployment = camAPI.resource('deployment');
    var ProcessInstance = camAPI.resource('process-instance');
    var CaseInstance = camAPI.resource('case-instance');

    var deleteDeploymentData = deploymentData.newChild($scope);

    var SKIP_CUSTOM_LISTENERS = configuration.getSkipCustomListeners();

    var options = $scope.options = {
      cascade: false,
      skipCustomListeners: SKIP_CUSTOM_LISTENERS.default,
      skipIoMappings: true
    };

    $scope.hideSkipCustomListeners = SKIP_CUSTOM_LISTENERS.hidden;

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
        deferred.resolve(res);

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
      var info = [ $translate.instant('REPOSITORY_DEPLOYMENTS_INFO_THERE_ARE') ];

      if ($scope.processInstanceCount && $scope.processInstanceCount.count > 0) {
        info.push($scope.processInstanceCount.count);
        ($scope.processInstanceCount && $scope.processInstanceCount.count > 1) ? info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_RUNNING_PLURAL')) : info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_RUNNING_SINGULAR'));
      }

      if ($scope.processInstanceCount && $scope.processInstanceCount.count > 0 && $scope.caseInstanceCount > 0) {
        info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_AND'));
      }

      if ($scope.caseInstanceCount > 0) {
        info.push($scope.caseInstanceCount);
        $scope.caseInstanceCount > 1 ? info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_OPEN_PLURAL')) : info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_OPEN_SINGULAR'));
      }

      info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_WHICH_BELONG'));
      info = info.join(' ');

      return info;
    };

    $scope.deleteDeployment = function() {
      $scope.status = 'PERFORM_DELETE';

      Deployment.delete(deployment.id, options, function(err) {

        $scope.status = null;

        if (err) {
          return Notifications.addError({
            status: $translate.instant('REPOSITORY_DEPLOYMENTS_INFO_MSN_STATUS'),
            message: $translate.instant('REPOSITORY_DEPLOYMENTS_INFO_MSN_MSN', { message: err.message }),
            exclusive: true
          });
        }

        $scope.$close();

      });
    };

  }];
