'use strict';


var AuthenticationPage = require('../pages/authentication');

var BasePage = require('../pages/page');

var FrontPage = BasePage.extend({});

var testHelper = require('./../test-helper');

var frontPage = new FrontPage();
frontPage.authentication = new AuthenticationPage();

module.exports = function (appName) {
  return function() {
    before([], function () {
      return testHelper(function () {
        frontPage.navigateToWebapp(appName);
      });
    });


    describe.skip('on large screens', function () {
      describe('for anonymous user', function () {
        it('does not show the account', function () {
          expect(frontPage.accountDropdown().isPresent()).to.eventually.eql(false);
          expect(frontPage.accountDropdownButton().isPresent()).to.eventually.eql(false);
        });

        it('shows the engine selection', function () {
          expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(true);
          expect(frontPage.engineSelectDropdownButton().isDisplayed()).to.eventually.eql(true);
        });

        it('shows the app switch', function () {
          expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(true);
          expect(frontPage.appSwitchDropdownButton().isDisplayed()).to.eventually.eql(true);
        });

        it('does not show a hamburger button', function () {
          expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(false);
        });
      });


      describe('for authenticated user', function () {
        before(function () {
          frontPage.authentication.userLogin('admin', 'admin');
        });

        after(function () {
          frontPage.logout();
        });


        it('shows the account', function () {
          expect(frontPage.accountDropdown().isDisplayed()).to.eventually.eql(true);
          expect(frontPage.accountDropdownButton().isDisplayed()).to.eventually.eql(true);
        });

        it('shows the full name of the logged in user', function () {
          expect(frontPage.accountDropdownButton().getText()).to.eventually.eql('Steve Hentschi');
        });

        it('shows the engine selection', function () {
          expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(true);
          expect(frontPage.engineSelectDropdownButton().isDisplayed()).to.eventually.eql(true);
        });

        it('shows the app switch', function () {
          expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(true);
          expect(frontPage.appSwitchDropdownButton().isDisplayed()).to.eventually.eql(true);
        });

        it('does not show a hamburger button', function () {
          expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(false);
        });
      });
    });


    describe('on small screens', function () {
      var originalSize;

      before(function () {
        browser.manage().window().getSize().then(function (size) {
          originalSize = size;
          browser.manage().window().setSize(760, 560);
          browser.sleep(500);
        });
      });

      after(function () {
        browser.manage().window().setSize(originalSize.width, originalSize.height);
      });


      describe('for anonymous user', function () {
        it('does not show the account', function () {
          expect(frontPage.accountDropdown().isPresent()).to.eventually.eql(false);
        });

        it('does not show the engine selection', function () {
          expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(false);
        });

        it('does not show the app switch', function () {
          expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(false);
        });

        it('shows a hamburger button', function () {
          expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(true);
        });


        describe('after beeing expanded', function () {
          before(function () {
            frontPage.hamburgerButton().click();
          });


          it('shows the engine selection', function () {
            expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(true);
            expect(frontPage.engineSelectDropdownButton().isDisplayed()).to.eventually.eql(true);
          });

          it('shows the app switch', function () {
            expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(true);
            expect(frontPage.appSwitchDropdownButton().isDisplayed()).to.eventually.eql(true);
          });

          it('shows a hamburger button', function () {
            expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(true);
          });


          describe('after beeing collapsed again', function () {
            before(function () {
              frontPage.hamburgerButton().click();
            });


            it('does not show the engine selection', function () {
              expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(false);
            });

            it('does not show the app switch', function () {
              expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(false);
            });

            it('shows a hamburger button', function () {
              expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(true);
            });
          });
        });
      });


      describe('for authenticated user', function () {
        before(function () {
          frontPage.authentication.userLogin('admin', 'admin');
        });

        after(function () {
          frontPage.accountDropdown().isDisplayed().then(function (displayed) {
            if (!displayed) {
              return frontPage.hamburgerButton().click().then(function () {
                frontPage.logout();
              });
            }
            frontPage.logout();
          });
        });


        it('does not show the engine selection', function () {
          expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(false);
        });

        it('does not show the app switch', function () {
          expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(false);
        });

        it('shows a hamburger button', function () {
          expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(true);
        });


        describe('after beeing expanded', function () {
          before(function () {
            frontPage.hamburgerButton().click();
          });


          it('shows the account', function () {
            expect(frontPage.accountDropdown().isDisplayed()).to.eventually.eql(true);
            expect(frontPage.accountDropdownButton().isDisplayed()).to.eventually.eql(true);
          });

          it('shows the engine selection', function () {
            expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(true);
            expect(frontPage.engineSelectDropdownButton().isDisplayed()).to.eventually.eql(true);
          });

          it('shows the app switch', function () {
            expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(true);
            expect(frontPage.appSwitchDropdownButton().isDisplayed()).to.eventually.eql(true);
          });

          it('shows a hamburger button', function () {
            expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(true);
          });


          describe('after beeing collapsed again', function () {
            before(function () {
              frontPage.hamburgerButton().click();
            });


            it('does not show the engine selection', function () {
              expect(frontPage.engineSelectDropdown().isDisplayed()).to.eventually.eql(false);
            });

            it('does not show the app switch', function () {
              expect(frontPage.appSwitchDropdown().isDisplayed()).to.eventually.eql(false);
            });

            it('shows a hamburger button', function () {
              expect(frontPage.hamburgerButton().isDisplayed()).to.eventually.eql(true);
            });
          });
        });
      });
    });
  };
};
