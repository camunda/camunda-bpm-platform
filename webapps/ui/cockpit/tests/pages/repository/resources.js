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
    return element(by.css('[cam-resources]'));
  },

  resourceList: function() {
    return this.formElement().all(
      by.repeater('(delta, resource) in resources')
    );
  },

  getResourceIndex: function(resourceName) {
    return this.findElementIndexInRepeater(
      '(delta, resource) in resources',
      by.css('.name .resource'),
      resourceName
    ).then(function(idx) {
      return idx;
    });
  },

  selectResource: function(idxOrName) {
    var self = this;
    function callPageObject(idx) {
      self
        .resourceList()
        .get(idx)
        .element(by.css('a'))
        .click();
      self.waitForElementToBeVisible(
        element(by.css('[cam-resource-meta] .name'))
      );
    }

    if (typeof idxOrName === 'number') {
      callPageObject.call(this, idxOrName);
    } else {
      this.getResourceIndex(idxOrName).then(callPageObject.bind(this));
    }
  },

  resourceName: function(idx) {
    return this.resourceList()
      .get(idx)
      .element(by.css('a'))
      .getText();
  }
});
