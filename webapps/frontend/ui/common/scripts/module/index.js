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

// Services
var isModuleAvailable = require('./services/is-module-available');
var exposeScopeProperties = require('./services/expose-scope-properties');
var loaders = require('./services/loaders');
var integrateActivityInstanceFilter = require('./services/integrate-activity-instance-filter');
var params = require('./services/params');
var createListQueryFunction = require('./services/create-list-query-function');
var createIsSearchQueryChangedFunction = require('./services/create-is-search-query-changed-function');
var readFiles = require('./services/read-files');
var upload = require('./services/upload');
var getDeploymentUrl = require('./services/get-deployment-url');
var isFileUploadSupported = require('./services/is-file-upload-supported');
var get = require('./services/get');
var pluginApi = require('../services/plugins/getApiAttributes');

// Components
var camToolbar = require('./components/cam-toolbar');
var camPagination = require('./components/cam-pagination');
var camSearchableArea = require('./components/cam-searchable-area');
var camTabs = require('./components/cam-tabs');

// Directives
var camHoverArea = require('./directives/cam-hover-area');
var camHoverTrigger = require('./directives/cam-hover-trigger');
var camHoverableTitle = require('./directives/cam-hoverable-title');
var camFile = require('./directives/cam-file');
var camSortableTableHeader = require('./directives/cam-sortable-table-header');
var camSortableTableColumn = require('./directives/cam-sortable-table-column');

// Controllers
var HoverAreaController = require('./controllers/hover-area');
var CamPaginationController = require('./controllers/cam-pagination');
var CamTabsController = require('./controllers/cam-tabs');
var CamPaginationSearchIntegrationController = require('./controllers/cam-pagination-search-integration');

// Values
var routeUtil = require('../util/routeUtil');
var paginationUtils = require('../util/pagination-utils');
var searchWidgetUtils = require('../util/search-widget-utils');

// Optional Modules
var externalTasksCommon = require('./external-tasks-common');

var ngModule = angular.module('cam-common', [externalTasksCommon.name]);

// Services
ngModule.factory('isModuleAvailable', isModuleAvailable);
ngModule.factory('exposeScopeProperties', exposeScopeProperties);
ngModule.factory('Loaders', loaders);
ngModule.factory(
  'integrateActivityInstanceFilter',
  integrateActivityInstanceFilter
);
ngModule.factory('params', params);
ngModule.factory('createListQueryFunction', createListQueryFunction);
ngModule.factory(
  'createIsSearchQueryChangedFunction',
  createIsSearchQueryChangedFunction
);
ngModule.factory('readFiles', readFiles);
ngModule.factory('upload', upload);
ngModule.factory('getDeploymentUrl', getDeploymentUrl);
ngModule.factory('isFileUploadSupported', isFileUploadSupported);
ngModule.factory('get', get);
ngModule.factory('getPluginApiAttributes', () => pluginApi);

// Components
ngModule.directive('camToolbar', camToolbar);
ngModule.directive('camPagination', camPagination);
ngModule.directive('camSearchableArea', camSearchableArea);
ngModule.directive('camTabs', camTabs);

// Directives
ngModule.directive('camHoverArea', camHoverArea);
ngModule.directive('camHoverTrigger', camHoverTrigger);
ngModule.directive('camHoverableTitle', camHoverableTitle);
ngModule.directive('camFile', camFile);
ngModule.directive('camSortableTableHeader', camSortableTableHeader);
ngModule.directive('camSortableTableColumn', camSortableTableColumn);

// Controllers
ngModule.controller('HoverAreaController', HoverAreaController);
ngModule.controller('CamPaginationController', CamPaginationController);
ngModule.controller('CamTabsController', CamTabsController);
ngModule.controller(
  'CamPaginationSearchIntegrationController',
  CamPaginationSearchIntegrationController
);

// Values
ngModule.value('routeUtil', routeUtil);
ngModule.value('paginationUtils', paginationUtils);
ngModule.value('searchWidgetUtils', searchWidgetUtils);

module.exports = ngModule;
