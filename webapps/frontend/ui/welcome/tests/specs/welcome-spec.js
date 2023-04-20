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

var page = require('../pages/welcome');

describe('Welcome Welcome page Spec', function() {
  before(function() {
    return testHelper([], function() {
      page.navigateToWebapp('Cockpit');
      page.authentication.userLogin('admin', 'admin');
      page.navigateToWebapp('Welcome');
    });
  });

  describe('webapp links', function() {
    it('displays links to the web applications', function() {
      expect(page.adminWebappLink().isDisplayed()).to.eventually.eql(true);
      expect(page.cockpitWebappLink().isDisplayed()).to.eventually.eql(true);
      expect(page.tasklistWebappLink().isDisplayed()).to.eventually.eql(true);
    });

    it('link should lead to current engine', function(done) {
      element(by.css('.engine-select'))
        .getText()
        .then(function(currentEngine) {
          currentEngine = currentEngine.trim();

          expect(
            page
              .adminWebappLink()
              .element(by.css('a'))
              .getAttribute('href')
          ).to.eventually.contain(currentEngine);
          expect(
            page
              .cockpitWebappLink()
              .element(by.css('a'))
              .getAttribute('href')
          ).to.eventually.contain(currentEngine);
          expect(
            page
              .tasklistWebappLink()
              .element(by.css('a'))
              .getAttribute('href')
          ).to.eventually.contain(currentEngine);

          done();
        });
    });
  });

  describe('custom links', function() {
    it('displays customizable links to external websites or apps');
  });

  describe('user profile', function() {
    it('displays the user profile', function() {
      expect(page.userProfile().isPresent()).to.eventually.eql(true);
      expect(page.userProfile().isDisplayed()).to.eventually.eql(true);
    });

    it('allows the user to change its first name, last name and email address', function() {
      page.userProfileLink().click();

      expect(page.userProfileFirstNameField().isDisplayed()).to.eventually.eql(
        true
      );
      expect(page.userProfileLastNameField().isDisplayed()).to.eventually.eql(
        true
      );
      expect(page.userProfileEmailField().isDisplayed()).to.eventually.eql(
        true
      );

      page
        .userProfileFirstNameField()
        .clear()
        .sendKeys('test');
      page
        .userProfileLastNameField()
        .clear()
        .sendKeys('test');
      page
        .userProfileEmailField()
        .clear()
        .sendKeys('test.test@test.net');

      page.userProfileFormSubmit();
      browser.refresh();

      expect(page.userProfileFullName().getText()).to.eventually.eql(
        'test test'
      );
      expect(page.userProfileEmail().getText()).to.eventually.eql(
        'test.test@test.net'
      );
    });

    it('allows the user to change its password', function() {
      page.changePasswordLink().click();

      expect(page.changePasswordCurrentField().isDisplayed()).to.eventually.eql(
        true
      );
      expect(page.changePasswordNewField().isDisplayed()).to.eventually.eql(
        true
      );
      expect(
        page.changePasswordConfirmationField().isDisplayed()
      ).to.eventually.eql(true);

      page
        .changePasswordCurrentField()
        .clear()
        .sendKeys('admin');
      page
        .changePasswordNewField()
        .clear()
        .sendKeys('papipapo');
      page
        .changePasswordConfirmationField()
        .clear()
        .sendKeys('papipapo');

      page.changePasswordFormSubmit();
      page.logout();

      page.navigateToWebapp('Cockpit');
      page.authentication.userLogin('admin', 'papipapo');
    });
  });
});
