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

/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, before: false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';
var path = require('path');
var projectRoot = path.resolve(__dirname, '../../../../');
var pkg = require(path.join(projectRoot, 'package.json'));
var pageUrl = 'http://localhost:' + pkg.gruntConfig.connectPort +
              '/lib/widgets/header/test/cam-widget-header.spec.html';

var page = require('./cam-widget-header.page.js');

describe('Header', function() {
  var header;
  describe('With content', function() {
    before(function() {
      browser.get(pageUrl + '#with-content');
      header = page.header('#with-content');
    });

    it('uses the ng-transclude feature', function() {
      expect(header.transcludedText()).to.eventually.eql('Awesome\nSweet\nMarvellous');
    });
  });


  describe('Anonymous', function() {
    before(function() {
      browser.get(pageUrl + '#anonymous');
      header = page.header('#anonymous');
    });

    it('does not show the account dropdown', function() {
      expect(header.account().isPresent()).to.eventually.eql(false);
    });

    it('does not show the link to the current (admin) app', function() {
      expect(header.adminLink().isPresent()).to.eventually.eql(false);
    });

    it('shows the link to the welcome app', function() {
      expect(header.welcomeLink().isPresent()).to.eventually.eql(true);
    });

    it('shows the link to the cockpit app', function() {
      expect(header.cockpitLink().isPresent()).to.eventually.eql(true);
    });

    it('shows the link to the tasklist app', function() {
      expect(header.tasklistLink().isPresent()).to.eventually.eql(true);
    });
  });


  describe('Authenticated', function() {
    before(function() {
      browser.get(pageUrl + '#authenticated');
      header = page.header('#authenticated');
    });

    it('shows the account dropdown', function() {
      expect(header.account().isPresent()).to.eventually.eql(true);
    });

    it('shows the user name', function() {
      expect(header.accountText()).to.eventually.eql('Max Mustermann');
    });

    it('shows the link to admin app', function() {
      expect(header.adminLink().isPresent()).to.eventually.eql(true);
    });

    it('does not show the link to welcome app because user is logged in', function() {
      expect(header.welcomeLink().isPresent()).to.eventually.eql(false);
    });

    it('does not show the link to cockpit app because user has not access to it', function() {
      expect(header.cockpitLink().isPresent()).to.eventually.eql(false);
    });

    it('does not show the link to tasklist app because it is the current app', function() {
      expect(header.tasklistLink().isPresent()).to.eventually.eql(false);
    });

    it('does not show a hamburger menu button', function() {
      expect(header.hamburgerButton().isDisplayed()).to.eventually.eql(false);
    });

    it('does not show a small screen warning', function() {
      expect(header.smallScreenWarning().isDisplayed()).to.eventually.eql(false);
    });


    describe('on small devices', function() {
      var originalSize;

      before(function() {
        browser.manage().window().getSize().then(function(size) {
          originalSize = size;
          browser.manage().window().setSize(760, 480);
        });
      });

      after(function() {
        browser.manage().window().setSize(originalSize.width, originalSize.height);
      });

      it('does not show the account dropdown', function() {
        expect(header.account().isDisplayed()).to.eventually.eql(false);
      });

      it('does not show the transcluded content', function() {
        expect(header.transcludedElement().isDisplayed()).to.eventually.eql(false);
      });

      it('does not show the user name', function() {
        expect(header.accountText().isDisplayed()).to.eventually.eql(false);
      });

      it('shows a hamburger menu button', function() {
        expect(header.hamburgerButton().isDisplayed()).to.eventually.eql(true);
      });

      it('shows a small screen warning', function() {
        expect(header.smallScreenWarning().isDisplayed()).to.eventually.eql(true);
      });


      describe('when expanded', function() {
        before(function() {
          header.hamburgerButton().click();
        });

        it('shows the transcluded content', function() {
          expect(header.transcludedElement().isDisplayed()).to.eventually.eql(true);
          expect(header.transcludedText()).to.eventually.eql('Awesome\nSweet\nButton!\nMarvellous');
        });

        it('shows the account dropdown', function() {
          expect(header.account().isDisplayed()).to.eventually.eql(true);
        });

        it('shows the user name', function() {
          expect(header.accountText().isDisplayed()).to.eventually.eql(true);
          expect(header.accountText()).to.eventually.eql('Max Mustermann');
        });

        describe('when collapsed back', function() {
          before(function() {
            header.hamburgerButton().click();
          });

          it('does not show the transcluded content', function() {
            expect(header.transcludedElement().isDisplayed()).to.eventually.eql(false);
          });

          it('does not show the account dropdown', function() {
            expect(header.account().isDisplayed()).to.eventually.eql(false);
          });

          it('does not show the user name', function() {
            expect(header.accountText().isDisplayed()).to.eventually.eql(false);
          });

          it('shows a hamburger menu button', function() {
            expect(header.hamburgerButton().isDisplayed()).to.eventually.eql(true);
          });
        });
      });
    });
  });


  describe('Authenticated, 1 app', function() {
    describe('on auhorized app', function() {
      before(function() {
        browser.get(pageUrl + '#authenticated-single');
        header = page.header('#authenticated-single');
      });


      it('shows the account dropdown', function() {
        expect(header.account().isPresent()).to.eventually.eql(true);
      });

      it('does not show the app switch', function() {
        expect(header.appSwitch().isPresent()).to.eventually.eql(false);
      });
    });


    describe('on unauhorized app', function() {
      before(function() {
        browser.get(pageUrl + '#authenticated-single-unauthorized');
        header = page.header('#authenticated-single-unauthorized');
      });


      it('shows the account dropdown', function() {
        expect(header.account().isPresent()).to.eventually.eql(true);
      });

      it('shows the link to the tasklist app', function() {
        expect(header.tasklistLink().isPresent()).to.eventually.eql(true);
      });
    });
  });
});
