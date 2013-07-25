ngDefine('cockpit.pages', function(module, $) {

  function CancelProcessInstanceController ($scope, $q, $location, Uri, Notifications, ProcessInstanceResource) {

    var BEFORE_CANCEL = 'beforeCancellation',
        PERFORM_CANCEL = 'performCancellation',
        CANCEL_SUCCESS = 'cancellationSuccess',
        CANCEL_FAILED = 'cancellationFailed',
        LOADING_FAILED = 'loadingFailed';

    $scope.$on('$routeChangeStart', function () {
      $scope.cancelProcessInstanceDialog.close();
    });

    function loadSubProcessInstances () {
      var deferred = $q.defer();

      ProcessInstanceResource.query({'firstResult': 0, 'maxResults': 5}, {'superProcessInstance': $scope.processInstance.id}).$then(function (response) {
        deferred.resolve(response.data);
      }, function (error) {
        deferred.reject(error.data);
      });

      return deferred.promise;
    }

    function countSubProcessInstances () {
      var deferred = $q.defer();

        ProcessInstanceResource.count({'superProcessInstance': $scope.processInstance.id}).$then(function (response) {
          deferred.resolve(response.data);
        }, function (error) {
          deferred.reject(error.data);
        });

      return deferred.promise;
    }

    $q.all([ loadSubProcessInstances(), countSubProcessInstances() ]).then(function(results) {
      $scope.subProcessInstances = results[0];
      $scope.subProcessInstancesCount = results[1].count;

      $scope.status = BEFORE_CANCEL;
    }, function (error) {
      $scope.status = LOADING_FAILED;
      Notifications.addError({'status': 'Failed', 'message': 'Loading of further process instance information failed: ' + error.message, 'exclusive': ['type']});
    });

    $scope.cancelProcessInstance = function () {
      $scope.status = PERFORM_CANCEL;

      $scope.processInstance.$delete(function (response) {
        //success
        $scope.status = CANCEL_SUCCESS;
        Notifications.addMessage({'status': 'Canceled', 'message': 'The cancellation of the process instance was successful.'});

      }, function (error) {
        // failure
        $scope.status = CANCEL_FAILED;
        Notifications.addError({'status': 'Failed', 'message': 'The cancellation of the process instance failed.', 'exclusive': ['type']});
      });
    };

    $scope.close = function (status) {
      $scope.cancelProcessInstanceDialog.close();

      // if the cancellation of the process instance was successfull,
      // then redirect to the corresponding process definition overview.
      if (status === CANCEL_SUCCESS) {
        $location.url('/process-definition/' + $scope.processInstance.definitionId);
        $location.replace();
      }

    };
  };

  module.controller('CancelProcessInstanceController', [ '$scope',
                                                         '$q',
                                                         '$location',
                                                         'Uri',
                                                         'Notifications',
                                                         'ProcessInstanceResource',
                                                         CancelProcessInstanceController ]);

});