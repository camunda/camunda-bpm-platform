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

var Page = require('./repository-view');

module.exports = Page.extend({
  formElement: function() {
    return element(by.css('[cam-resource-wrapper]'));
  },

  noResourceInfoText: function() {
    return this.formElement()
      .element(by.css('.no-resource'))
      .getText();
  },

  waitForResourceDetailView: function() {
    var elementToWaitFor = this.resourceName();
    this.waitForElementToBeVisible(elementToWaitFor);
  },

  resourceName: function() {
    return this.formElement()
      .element(by.css('h2.name'))
      .getText();
  },

  resourceVersionElement: function() {
    return this.formElement().element(by.css('header .version'));
  },

  resourceVersion: function() {
    return this.resourceVersionElement().getText();
  },

  downloadButton: function() {
    return element(by.css('.download-resource'));
  },

  bpmnDiagramFormElement: function() {
    return element(by.css('[cam-widget-bpmn-viewer]'));
  },

  dmnDiagramFormElement: function() {
    return element(by.css('[cam-widget-dmn-viewer]'));
  },

  cmmnDiagramFormElement: function() {
    return element(by.css('[cam-widget-cmmn-viewer]'));
  },

  imageFormElement: function() {
    return element(by.css('.image-resource'));
  },

  unkownResourceFormElement: function() {
    return element(by.css('.unkown-resource'));
  }
});
