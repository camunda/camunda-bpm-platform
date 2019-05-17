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
var setupFile = require('./dashboard-setup');

var dashboardPage = require('../pages/dashboard');

describe('Admin Dashboard Spec', function() {
  before(function() {
    return testHelper(setupFile.setup1, function() {});
  });

  describe('access', function() {
    describe('as an admin', function() {
      before(function() {
        dashboardPage.navigateToWebapp('Admin');
        dashboardPage.authentication.userLogin('admin', 'admin');
      });

      after(function() {
        dashboardPage.authentication.ensureUserLogout();
      });

      it('should present the user section', function() {
        expect(dashboardPage.section('user').isPresent()).to.eventually.eql(
          true
        );
      });

      it('should present the create user link', function() {
        expect(
          dashboardPage.sectionLink('user', 'Create New User').isPresent()
        ).to.eventually.eql(true);
      });

      it('should present the group section', function() {
        expect(dashboardPage.section('group').isPresent()).to.eventually.eql(
          true
        );
      });

      it('should present the create group link', function() {
        expect(
          dashboardPage.sectionLink('group', 'Create New Group').isPresent()
        ).to.eventually.eql(true);
      });

      it('should present the system section', function() {
        expect(dashboardPage.section('system').isPresent()).to.eventually.eql(
          true
        );
      });

      it('should present the authorizations section', function() {
        expect(
          dashboardPage.section('authorization').isPresent()
        ).to.eventually.eql(true);
      });
    });

    describe('as a conventional user', function() {
      before(function() {
        dashboardPage.navigateToWebapp('Admin');
        dashboardPage.authentication.userLogin('john', 'MobyDick');
      });

      after(function() {
        dashboardPage.authentication.ensureUserLogout();
      });

      it('shows an error', function() {
        expect(
          element(
            by.cssContainingText(
              '.notifications',
              'missing access rights to application'
            )
          ).isPresent()
        ).to.eventually.eql(true);
      });
    });

    describe('as a conventional user with admin access', function() {
      before(function() {
        dashboardPage.navigateToWebapp('Admin');
        dashboardPage.authentication.userLogin('mm', 'SweetDreams');
      });

      after(function() {
        dashboardPage.authentication.ensureUserLogout();
      });

      it('shows an error', function() {
        expect(
          element(
            by.cssContainingText(
              '.notifications',
              'missing access rights to application'
            )
          ).isPresent()
        ).to.eventually.eql(false);
      });

      it('should present the user section', function() {
        expect(dashboardPage.section('user').isPresent()).to.eventually.eql(
          true
        );
      });

      it('should not present the create user link', function() {
        expect(
          dashboardPage.sectionLink('user', 'Create New User').isPresent()
        ).to.eventually.eql(false);
      });

      it('should present the group section', function() {
        expect(dashboardPage.section('group').isPresent()).to.eventually.eql(
          true
        );
      });

      it('should not present the create group link', function() {
        expect(
          dashboardPage.sectionLink('group', 'Create New Group').isPresent()
        ).to.eventually.eql(false);
      });

      it('should not present the system section', function() {
        expect(dashboardPage.section('system').isPresent()).to.eventually.eql(
          false
        );
      });

      it('should not present the authorizations section', function() {
        expect(
          dashboardPage.section('authorization').isPresent()
        ).to.eventually.eql(false);
      });
    });
  });

  describe('logout', function() {
    before(function() {
      dashboardPage.authentication.ensureUserLogout();
      dashboardPage.navigateToWebapp('Admin');
      dashboardPage.authentication.userLogin('admin', 'admin');
    });

    it('should show the login form when loging out', function() {
      // when
      dashboardPage.authentication.userLogout();

      expect(element(by.css('form.form-signin')).isPresent()).to.eventually.eql(
        true
      );
    });
  });
});
