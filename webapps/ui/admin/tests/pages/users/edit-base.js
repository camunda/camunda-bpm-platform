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

var Page = require('./../base');

var userSection = element(by.css('section'));

module.exports = Page.extend({
  /**
  Select Profile in users side navbar
  @memberof cam.test.e2e.admin.pages.editUser

  @param {string} navbarItem
  @return {!webdriver.promise.Promise}  - A promise of the selected element
  */
  selectUserNavbarItem: function(navbarItem) {
    var index = ['Profile', 'Account', 'Groups', 'Tenants'];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex)
      item = userSection.element(
        by.css('aside ul li:nth-child(' + itemIndex + ')')
      );
    else item = userSection.element(by.css('aside ul li:nth-child(1)'));

    item.click();
    return item;
  }
});
