var utils = require('./../utils');

describe('the variables', function() {
  it('can change the value of a serialized object', function() {
    utils.loginAndGoTo('/camunda/app/cockpit/default/#/dashboard');

    element(by.css('[href*="process-definition/cornercasesProcess"]'))
      .click().then(function() {
        element.all(by.css('.ctn-tabbed-content [href*="process-instance"]'))
          .then(function(links) {
            links[0]
              .click().then(function() {
                var serializedVarLink = element(by.css('[data-variable-type="org.camunda.bpm.pa.service.CockpitVariable"] .variable-value a'));

                serializedVarLink
                  .click().then(function() {
                    var changeBtn = element(by.css('button.btn-primary[ng-click*="change"]'));
                    var confirmBtn = element(by.css('button.btn-warning[ng-click*="change"]'));
                    var closeBtn = element(by.css('button.btn-primary[ng-click*="close"]'));

                    expect(changeBtn.isEnabled()).toBe(false);
                    expect(confirmBtn.isDisplayed()).toBe(false);
                    expect(closeBtn.isDisplayed()).toBe(false);

                    var json = JSON.stringify({
                      "name": "testaaaa",
                      "value": "cockpitVariableValue",
                      "dates": [
                        "2014-05-15T08:00:59"
                      ]
                    }, null, 2);

                    var textarea = element(by.css('.modal-body textarea[ng-model="currentValue"]'));
                    textarea.clear();
                    textarea
                      .sendKeys(json).then(function() {
                        expect(confirmBtn.isEnabled()).toBe(true);
                        expect(changeBtn.isDisplayed()).toBe(false);
                        expect(closeBtn.isDisplayed()).toBe(false);

                        expect(element(by.css('.modal-body .alert-block')).getText())
                          .toMatch(/Are you sure/);

                        // browser.sleep(5000);

                        confirmBtn
                          .click().then(function() {
                            expect(element(by.css('.modal-body .alert')).getText())
                              .toMatch(/Success/);

                            expect(confirmBtn.isDisplayed()).toBe(false);
                            expect(changeBtn.isEnabled()).toBe(false);
                            expect(closeBtn.isDisplayed()).toBe(true);

                            // browser.sleep(5000);

                            closeBtn
                              .click().then(function() {
                                // browser.sleep(5000);
                              });
                          });
                      });

                  });
              });
          });
      });
  });
});
