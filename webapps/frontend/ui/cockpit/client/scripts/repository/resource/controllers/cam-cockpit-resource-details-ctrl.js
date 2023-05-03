/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$scope',
  '$q',
  'Uri',
  'camAPI',
  'Views',
  '$translate',
  function($scope, $q, Uri, camAPI, Views, $translate) {
    // utilities ///////////////////////////////////////////////////

    var isObject = angular.isObject;

    var checkResource = function(name, pattern) {
      return name && pattern.test(name.toLowerCase());
    };

    // fields //////////////////////////////////////////////////////

    var resourceDetailsData = ($scope.resourceDetailsData = $scope.repositoryData.newChild(
      $scope
    ));

    var control = ($scope.control = {});

    var BPMN_PATTERN = /\.(bpmn\d*.xml|bpmn)$/;
    var CMMN_PATTERN = /\.(cmmn\d*.xml|cmmn)$/;
    var DMN_PATTERN = /\.(dmn\d*.xml|dmn)$/;
    var IMAGE_PATTERN = /\.(gif|jpg|jpeg|jpe|png|svg|tif|tiff)$/;
    var HTML_PATTERN = /\.html$/;
    var FORM_PATTERN = /\.(form)$/;

    var PLUGIN_ACTION_COMPONENT = 'cockpit.repository.resource.action';

    // type of a resource //////////////////////////////////////////

    var getResourceName = function(resource) {
      if (isObject(resource)) {
        return resource.name;
      }
      return resource;
    };

    var isBpmnResource = (control.isBpmnResource = $scope.isBpmnResource = function(
      resource
    ) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, BPMN_PATTERN);
    });

    var isCmmnResource = (control.isCmmnResource = $scope.isCmmnResource = function(
      resource
    ) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, CMMN_PATTERN);
    });

    var isDmnResource = (control.isDmnResource = $scope.isDmnResource = function(
      resource
    ) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, DMN_PATTERN);
    });

    var isImageResource = (control.isImageResource = $scope.isImageResource = function(
      resource
    ) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, IMAGE_PATTERN);
    });

    var isHtmlResource = (control.isHtmlResource = $scope.isHtmlResource = function(
      resource
    ) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, HTML_PATTERN);
    });

    var isFormResource = (control.isFormResource = $scope.isFormResource = function(
      resource
    ) {
      var resourceName = getResourceName(resource);
      return checkResource(resourceName, FORM_PATTERN);
    });

    control.isUnkownResource = $scope.isUnkownResource = function(resource) {
      return (
        !isBpmnResource(resource) &&
        !isCmmnResource(resource) &&
        !isDmnResource(resource) &&
        !isImageResource(resource) &&
        !isHtmlResource(resource) &&
        !isFormResource(resource)
      );
    };

    var ProcessDefinition = camAPI.resource('process-definition');
    var CaseDefinition = camAPI.resource('case-definition');
    var DecisionDefinition = camAPI.resource('decision-definition');

    // download link ////////////////////////////////////////////////

    control.downloadLink = $scope.downloadLink = function(
      deployment,
      resource
    ) {
      return (
        deployment &&
        resource &&
        Uri.appUri(
          'engine://engine/:engine/deployment/' +
            deployment.id +
            '/resources/' +
            resource.id +
            '/data'
        )
      );
    };

    // provide //////////////////////////////////////////////////////

    resourceDetailsData.provide('binary', [
      'resource',
      'currentDeployment',
      function(resource, deployment) {
        var deferred = $q.defer();

        if (!resource) {
          deferred.resolve(null);
        } else if (!deployment || deployment.id === null) {
          deferred.resolve(null);
        } else if (isImageResource(resource)) {
          // do not load image twice
          deferred.resolve(null);
        } else {
          fetch(
            Uri.appUri(
              'engine://engine/:engine/deployment/' +
                deployment.id +
                '/resources/' +
                resource.id +
                '/data'
            )
          )
            .then(async res => {
              const result = await res.text();
              deferred.resolve({data: result});
            })
            .catch(err => {
              deferred.reject(err);
            });
        }

        return deferred.promise;
      }
    ]);

    var pages = {current: 1, size: 50, total: 0};
    resourceDetailsData.provide('pages', function() {
      return pages;
    });

    resourceDetailsData.provide('definitions', [
      'currentDeployment',
      'resource',
      'pages',
      function(deployment, resource, pages) {
        var deferred = $q.defer();

        $scope.loadingState = 'LOADING';

        var Service = null;
        var bpmnResource = false;

        if (!deployment || !resource) {
          deferred.resolve([]);
        } else {
          if (isBpmnResource(resource)) {
            bpmnResource = true;
            Service = ProcessDefinition;
          } else if (isCmmnResource(resource)) {
            Service = CaseDefinition;
          } else if (isDmnResource(resource)) {
            Service = DecisionDefinition;
          }

          if (!Service) {
            deferred.resolve([]);
          } else {
            Service.count(
              {
                deploymentId: deployment.id,
                resourceName: resource.name
              },
              function(err, res) {
                function handleError(err) {
                  $scope.loadingState = 'ERROR';
                  $scope.textError =
                    err.message ||
                    $translate.instant(
                      'REPOSITORY_DEPLOYMENT_RESOURCE_CTRL_MSN'
                    );
                }

                if (err) {
                  handleError(err);
                  return deferred.reject(err);
                }

                pages.total = res;

                if (res === 0) {
                  // There are no definitions in this resource
                  return deferred.resolve([]);
                }

                Service.list(
                  {
                    deploymentId: deployment.id,
                    resourceName: resource.name,
                    maxResults: pages.size,
                    firstResult: pages.size * (pages.current - 1)
                  },
                  function(err, res) {
                    if (err) {
                      handleError(err);
                      return deferred.reject(err);
                    }

                    deferred.resolve(bpmnResource ? res.items : res);
                  }
                );
              }
            );
          }
        }

        return deferred.promise;
      }
    ]);

    // observe /////////////////////////////////////////////////

    resourceDetailsData.observe('resource', function(resource) {
      $scope.resource = resource;
    });

    resourceDetailsData.observe('currentDeployment', function(deployment) {
      $scope.deployment = deployment;
    });

    // plugins //////////////////////////////////////////////////

    $scope.resourceVars = {
      read: ['control', 'deployment', 'resource', 'resourceDetailsData']
    };
    $scope.resourceActions = Views.getProviders({
      component: PLUGIN_ACTION_COMPONENT
    });
  }
];
