'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-form.html', 'utf8');

var EMBEDDED_KEY = 'embedded:',
    APP_KEY = 'app:',
    ENGINE_KEY = 'engine:',
    DEPLOYMENT_KEY = 'deployment:';

function compact(arr) {
  var a = [];
  for (var ay in arr) {
    if (arr[ay]) {
      a.push(arr[ay]);
    }
  }
  return a;
}

var noop = function() {};

module.exports = [function() {

  return {

    restrict: 'A',

    scope: {
      tasklistForm : '=',

        /*
         * current options are:
         * - hideCompleteButton: to hide the complete button inside the form directive
         * - disableCompleteButton: to disable or enable the complete button inside
         *   the form directive
         * - disableForm: to disable or enable the form
         * - disableAddVariableButton: to disable or enable the 'Add Variable' button
         *   inside a generic form
         */
      options: '=',

        /*
         * contains parameter like taskId, processDefinitionId, processDefinitionKey etc.
         */
      params: '=',

        /* will be used to make a callback when the form will be completed */
      onFormCompletionCallback: '&',

        /*
         * will be used to register a completion handler, when the completion
         * will be trigger from the outside of a form
         */
      onFormCompletion: '&',

        /*
         * is a callback which will called when the validation state of the
         * form changes (pass the flag '$invalid').
         */
      onFormValidation: '&'
    },

    template: template,

    controller: [
      '$scope',
      'Uri',
      'camAPI',
      function(
        $scope,
        Uri,
        camAPI
      ) {
        $scope.taskRemoved = false;
        $scope.$on('taskremoved', function() {
          $scope.taskRemoved = true;
        });

        var processDefinitionResource = camAPI.resource('process-definition');
        var caseDefinitionResource = camAPI.resource('case-definition');
        var deploymentResource = camAPI.resource('deployment');

        // setup //////////////////////////////////////////////////////////////////

        $scope.onFormCompletionCallback = $scope.onFormCompletionCallback() || noop;
        $scope.onFormCompletion = $scope.onFormCompletion() || noop;
        $scope.onFormValidation = $scope.onFormValidation() || noop;
        $scope.completionHandler = noop;
        $scope.saveHandler = noop;

        $scope.$loaded = false;
        $scope.completeInProgress = false;

        // handle tasklist form ///////////////////////////////////////////////////

        $scope.$watch('tasklistForm', function(value) {
          $scope.$loaded = false;
          if (value) {
            parseForm(value);
            $scope.taskRemoved = false;
          }
        });

        $scope.asynchronousFormKey = {
          loaded: false,
          failure: false
        };

        function setAsynchronousFormKeyFailure(err) {
          $scope.asynchronousFormKey.failure = true;
          $scope.asynchronousFormKey.error = err;
        }

        function setAsynchronousFormKey(formKey) {
          $scope.asynchronousFormKey.key = formKey;
          $scope.asynchronousFormKey.loaded = true;
        }

        function parseForm(form) {
          var key = form.key,
              applicationContextPath = form.contextPath;

          // structure may be [embedded:][app:]formKey
          // structure may be [embedded:][deployment:]formKey

          // structure may be [app:]formKey
          // structure may be [deployment:]formKey

          if (!key) {
            form.type = 'generic';
            return;
          }

          if (key.indexOf(EMBEDDED_KEY) === 0) {
            key = key.substring(EMBEDDED_KEY.length);
            form.type = 'embedded';
          } else {
            form.type = 'external';
          }

          if (key.indexOf(APP_KEY) === 0) {
            if (applicationContextPath) {
              key = compact([applicationContextPath, key.substring(APP_KEY.length)])
                .join('/')
                // prevents multiple "/" in the URI
                .replace(/\/([\/]+)/, '/');
              setAsynchronousFormKey(key);
            }
          }

          else if (key.indexOf(DEPLOYMENT_KEY) === 0) {
            var resourceName = key.substring(DEPLOYMENT_KEY.length);

            var loadResourceInDeployment = function(deploymentId)  {
              deploymentResource.getResources(deploymentId, function(err, resourcesData) {
                if (err) {
                  setAsynchronousFormKeyFailure(err);
                } else {
                  var resourceFound = false;
                  // Find the resource with the given name from the list of all resources of a deployment
                  for (var index = 0; index < resourcesData.length; ++index) {
                    if (resourcesData[index].name === resourceName) {
                      key = Uri.appUri('engine://engine/:engine/deployment/' + deploymentId + '/resources/' + resourcesData[index].id + '/data');
                      setAsynchronousFormKey(key);
                      resourceFound = true;
                      break;
                    }
                  }
                  if (!resourceFound) {
                    setAsynchronousFormKeyFailure(new Error('Resource ' + resourceName + ' not found in deployment'));
                  }
                }
              });
            };

            if ($scope.params.processDefinitionId) {
              processDefinitionResource.get($scope.params.processDefinitionId, function(err, deploymentData) {
                if (err) {
                  setAsynchronousFormKeyFailure(err);
                } else {
                  loadResourceInDeployment(deploymentData.deploymentId);
                }
              });
            } else if ($scope.params.caseDefinitionId) {
              caseDefinitionResource.get($scope.params.caseDefinitionId, function(err, deploymentData) {
                if (err) {
                  setAsynchronousFormKeyFailure(err);
                } else {
                  loadResourceInDeployment(deploymentData.deploymentId);
                }
              });
            }
          }

          else if(key.indexOf(ENGINE_KEY) === 0) {
            // resolve relative prefix
            key = Uri.appUri(key);
            setAsynchronousFormKey(key);
          }

          else {
            setAsynchronousFormKey(key);
          }

          form.key = key;
        }

        // completion /////////////////////////////////////////////

        var completionCallback = function(err, result)  {
          $scope.onFormCompletionCallback(err, result);
          $scope.completeInProgress = false;
        };

        var complete = $scope.complete = function() {
          $scope.completeInProgress = true;
          $scope.completionHandler(completionCallback);
        };

        $scope.onFormCompletion(complete);

        $scope.showCompleteButton = function() {
          return $scope.options &&
                 !$scope.options.hideCompleteButton &&
                 $scope.$loaded;
        };

        $scope.disableCompleteButton = function() {
          return $scope.taskRemoved || $scope.completeInProgress || $scope.$invalid ||
            ($scope.options && $scope.options.disableCompleteButton);
        };

        // save ///////////////////////////////////////////////////

        $scope.save = function(evt) {
          $scope.saveHandler(evt);
        };

        // API ////////////////////////////////////////////////////

        this.notifyFormInitialized = function() {
          $scope.$loaded = true;
        };

        this.notifyFormInitializationFailed = function(error) {
          $scope.tasklistForm.$error = error;
          // mark the form as initialized
          this.notifyFormInitialized();
          // set the '$invalid' flag to true to
          // not be able to complete a task (or start
          // a process)
          this.notifyFormValidated(true);
        };

        this.notifyFormCompleted = function(err) {
          $scope.onFormCompletion(err);
        };

        this.notifyFormValidated = function(invalid) {
          $scope.$invalid = invalid;
          $scope.onFormValidation(invalid);
        };

        this.notifyFormDirty = function(dirty) {
          $scope.$dirty = dirty;
        };


        this.getOptions = function() {
          return $scope.options || {};
        };

        this.getTasklistForm = function() {
          return $scope.tasklistForm;
        };

        this.getParams = function() {
          return $scope.params || {};
        };

        this.registerCompletionHandler = function(fn) {
          $scope.completionHandler = fn ||  noop;
        };

        this.registerSaveHandler = function(fn) {
          $scope.saveHandler = fn ||  noop;
        };


      }]
  };
}];
