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

var navigationSection = element(
  by.css('[ng-controller="NavigationController"]')
);

var index = ['Users', 'Groups', 'Tenants', 'Authorizations', 'System'];

module.exports = Page.extend({
  pageHeader: function() {
    return element(by.css('.breadcrumbs-panel li.active .text')).getText();
  },

  boxHeader: function() {
    return element(
      by.css('.section-content div.h3, .section-content h3')
    ).getText();
  },

  selectNavbarItem: function(navbarItem) {
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (!itemIndex) itemIndex = 1;

    item = navigationSection.element(
      by.css('[cam-widget-header] ul li:nth-child(' + itemIndex + ')')
    );
    item.click();

    return item;
  },

  checkNavbarItem: function(navbarItem) {
    var idx = index.indexOf(navbarItem) + 1;

    if (!idx) idx = 1;

    return navigationSection.element(
      by.css('[cam-widget-header] ul li:nth-child(' + idx + ')')
    );
  }
});
