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
var setupFile = require('./admin-user-setup');

testHelper.expectStringEqual = require('../../../common/tests/string-equal');

var usersPage = require('../pages/users');

describe('Admin admin-user Spec', function() {
  before(function() {
    return testHelper(setupFile.setup1);
  });

  it('should validate admin setup page', function() {
    // when
    usersPage.navigateToWebapp('Admin');

    // then
    testHelper.expectStringEqual(usersPage.adminUserSetup.boxHeader(), 'SETUP');
    expect(
      usersPage.adminUserSetup.createNewAdminButton().isEnabled()
    ).to.eventually.eql(false);
  });

  it('should enter new admin profile', function() {
    // when
    usersPage.adminUserSetup.userIdInput('Admin');
    usersPage.adminUserSetup.passwordInput('admin123');
    usersPage.adminUserSetup.passwordRepeatInput('admin123');
    usersPage.adminUserSetup.userFirstNameInput('Über');
    usersPage.adminUserSetup.userLastNameInput('Admin');
    usersPage.adminUserSetup.userEmailInput('uea@camundo.org');

    usersPage.adminUserSetup.createNewAdminButton().click();

    // then
    expect(usersPage.adminUserSetup.statusMessage()).to.eventually.eql(
      'User created You have created an initial user.'
    );
  });

  it('should login as new Admin', function() {
    // when
    usersPage.navigateToWebapp('Admin');
    usersPage.authentication.userLogin('Admin', 'admin123');
    browser.get(usersPage.url);

    // then
    expect(usersPage.userFirstNameAndLastName(0)).to.eventually.eql(
      'Über Admin'
    );
  });
});
