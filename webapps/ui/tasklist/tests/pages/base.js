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

var Page = require('../../../common/tests/pages/page');

var navigationSection = element(by.css('[cam-widget-header]'));

module.exports = Page.extend({
  selectNavbarItem: function(navbarItem) {
    var index = ['Task', 'Process', 'Engine', 'Account', 'Webapps'];
    var cssElement;
    var item;

    switch (index.indexOf(navbarItem)) {
      case 0:
        cssElement = '.create-task-action';
        break;
      case 1:
        cssElement = '.start-process-action';
        break;
      case 2:
        cssElement = '.engine-select';
        break;
      case 3:
        cssElement = '.account';
        break;
      case 4:
        cssElement = '.app-switch';
        break;
      default:
        cssElement = '';
        console.log('cannot find navbar item');
    }
    item = navigationSection.element(by.css(cssElement));
    item.click();

    return item;
  },

  logout: function() {
    this.selectNavbarItem('Account');
    element(by.css('[ng-click="logout()"]')).click();
  },

  navigateLogout: function() {
    browser.get(this.url + 'logout');
  }
});
