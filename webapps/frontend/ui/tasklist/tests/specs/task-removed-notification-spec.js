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

var testHelper = require('../../../common/tests/test-helper');
var setupFile = require('./task-removed-notification-setup');
var dashboardPage = require('../pages/dashboard');

describe.skip('Task Removal Notification Spec', function() {
  describe('a task that has been removed or completed', function() {
    var tasklistItems;

    before(function() {
      return testHelper(setupFile.setup1, function() {
        dashboardPage.navigateToWebapp('Tasklist');
        dashboardPage.authentication.userLogin('test', 'test');

        tasklistItems = element.all(by.css('.tasks-list > li'));
        tasklistItems
          .first()
          .element(by.css('.clickable'))
          .click();
      });
    });

    it('disapears from list and shows a notification', function() {
      expect(tasklistItems.count()).to.eventually.eql(1);
      expect(
        element(by.css('.task-details .names > h2')).getText()
      ).to.eventually.eql('Task 1');

      // simulates a background resolution
      browser
        .executeAsyncScript(function() {
          var callback = arguments[arguments.length - 1];
          var xhr = new XMLHttpRequest();
          xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
              callback();
            }
          };
          xhr.open(
            'POST',
            '/camunda/api/engine/engine/default/task/1/submit-form',
            true
          );
          xhr.setRequestHeader('Content-Type', 'application/json');
          xhr.send('{"variables":{}}');
        })
        .then(function() {
          browser.sleep(10500);

          // refresh!
          tasklistItems = element.all(by.css('.tasks-list > li'));
          expect(tasklistItems.count()).to.eventually.eql(0);

          var completeButton = element(
            by.css(
              '.task-details .tabbed-content .form-actions button[ng-click="complete()"]'
            )
          );
          expect(completeButton.isEnabled()).to.eventually.eql(false);

          expect(
            element(by.css('.task-details .names > h2')).isPresent()
          ).to.eventually.eql(true);

          var alertMessage = element(by.css('.task-removed-alert .alert'));
          expect(alertMessage.isDisplayed()).to.eventually.eql(true);
          var dismissButton = alertMessage.element(by.css('button'));
          dismissButton.click();

          expect(
            element(by.css('.task-details .names > h2')).isPresent()
          ).to.eventually.eql(false);
        });
    });
  });
});
