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

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  annotationEdit = require('./annotation/cam-annotation-edit'),
  inlineField = require('./inline-field/cam-widget-inline-field'),
  searchPill = require('./search-pill/cam-widget-search-pill'),
  camQueryComponent = require('./search-pill/cam-query-component'),
  header = require('./header/cam-widget-header'),
  footer = require('./footer/cam-widget-footer'),
  loader = require('./loader/cam-widget-loader'),
  chartLine = require('./chart-line/cam-widget-chart-line'),
  debug = require('./debug/cam-widget-debug'),
  clipboard = require('./clipboard/cam-widget-clipboard'),
  variable = require('./variable/cam-widget-variable'),
  variablesTable = require('./variables-table/cam-widget-variables-table'),
  camRenderVarTemplate = require('./variables-table/cam-render-var-template'),
  search = require('./search/cam-widget-search'),
  bpmnViewer = require('./bpmn-viewer/cam-widget-bpmn-viewer'),
  cmmnViewer = require('./cmmn-viewer/cam-widget-cmmn-viewer'),
  dmnViewer = require('./dmn-viewer/cam-widget-dmn-viewer'),
  filtersModule = require('../filter/date/index'),
  directivesModule = require('../directives/index'),
  searchModule = require('../search/index'),
  variableValidator = require('./variable/cam-variable-validator'),
  localConf = require('./../services/cam-local-configuration'),
  camShareLink = require('./cam-share-link/cam-share-link'),
  password = require('./password/cam-widget-password'),
  selectionType = require('./selection-type/cam-widget-selection-type');

require('angular-ui-bootstrap');

var widgetModule = angular.module('camunda.common.widgets', [
  filtersModule.name,
  directivesModule.name,
  searchModule.name,
  'ui.bootstrap'
]);

widgetModule.factory('widgetLocalConf', localConf);
widgetModule.directive('camWidgetInlineField', inlineField);
widgetModule.directive('camWidgetSearchPill', searchPill);
widgetModule.directive('camWidgetHeader', header);
widgetModule.directive('camWidgetFooter', footer);
widgetModule.directive('camWidgetLoader', loader);
widgetModule.directive('camWidgetChartLine', chartLine);
widgetModule.directive('camWidgetDebug', debug);
widgetModule.directive('camWidgetClipboard', clipboard);
widgetModule.directive('camWidgetVariable', variable);
widgetModule.directive('camWidgetVariablesTable', variablesTable);
widgetModule.directive('camRenderVarTemplate', camRenderVarTemplate);
widgetModule.directive('camWidgetSearch', search);
widgetModule.directive('camWidgetBpmnViewer', bpmnViewer);
widgetModule.directive('camWidgetCmmnViewer', cmmnViewer);
widgetModule.directive('camWidgetDmnViewer', dmnViewer);
widgetModule.directive('camShareLink', camShareLink);
widgetModule.directive('camWidgetPassword', password);

widgetModule.directive('camVariableValidator', variableValidator);
widgetModule.directive('camAnnotationEdit', annotationEdit);

widgetModule.directive('camWidgetSelectionType', selectionType);

widgetModule.filter('camQueryComponent', camQueryComponent);

module.exports = widgetModule;
