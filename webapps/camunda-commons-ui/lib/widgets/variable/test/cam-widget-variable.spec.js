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
/* global __dirname: false, xdescribe: false, describe: false, before: false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false, xit: false,
          describe: false, after: false */
'use strict';
var path = require('path');
var projectRoot = path.resolve(__dirname, '../../../../');
var pkg = require(path.join(projectRoot, 'package.json'));
var pageUrl = 'http://localhost:' + pkg.gruntConfig.connectPort +
              '/lib/widgets/variable/test/cam-widget-variable.spec.html';

var page = require('./cam-widget-variable.page.js');

describe('Variable', function() {
  var variable;
  describe('editing', function() {
    before(function() {
      browser.get(pageUrl + '#example-1');
    });

    describe('when empty', function() {
      before(function() {
        variable = page.variable('#example-1', 0);
      });

      it('has a dropdown', function() {
        expect(variable.type().element(by.css('select')).isDisplayed()).to.eventually.eql(true);
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('');
      });

      it('is not valid', function() {
        expect(variable.typeSelectElement().getAttribute('class'))
          .to.eventually.match(/invalid/);

        expect(variable.name().getAttribute('class'))
          .to.eventually.match(/invalid/);

        expect(variable.value().getAttribute('class'))
          .to.eventually.match(/invalid/);
      });
    });


    describe('validation', function() {
      before(function() {
        // the integer variable
        variable = page.variable('#example-1', 4);
      });


      it('adds the invalid CSS class when value is not of the correct type', function() {
        variable.value().sendKeys('leet').then(function() {
          expect(variable.value().getAttribute('class'))
            .to.eventually.match(/invalid/);
        });
      });

      it('removes the invalid CSS class when is of the correct type', function() {
        variable.value().clear().sendKeys('1337');
        expect(variable.value().getAttribute('class'))
          .not.to.eventually.match(/invalid/);
      });



      it('adds the invalid CSS class when no name is given', function() {
        variable.name().clear();

        expect(variable.nameValue()).to.eventually.eql('');

        expect(variable.name().getAttribute('class'))
          .to.eventually.match(/invalid/);
      });


      it('removes the invalid CSS class when a name is given', function() {
        variable.name().sendKeys('integerVar');

        expect(variable.nameValue()).to.eventually.eql('integerVar');

        expect(variable.typeSelectElement().getAttribute('class'))
          .not.to.eventually.match(/invalid/);

        expect(variable.name().getAttribute('class'))
          .not.to.eventually.match(/invalid/);

        expect(variable.value().getAttribute('class'))
          .not.to.eventually.match(/invalid/);
      });
    });


    describe('"null" support', function() {
      before(function() {
        // the empty variable
        variable = page.variable('#example-1', 0);

        variable.typeSelect('String');
        variable.name().sendKeys('aName');
        variable.value().sendKeys('a value');
      });

      it('allows to set a variable value to "null"', function() {
        expect(variable.setNullBtn().isPresent())
          .to.eventually.eql(true);
        expect(variable.setNonNullBtn().isPresent())
          .to.eventually.eql(false);
      });

      it('allows to revert a variable value to its previous value', function() {
        variable.setNullBtn().click();

        expect(variable.setNullBtn().isPresent())
          .to.eventually.eql(false);
        expect(variable.setNonNullBtn().isPresent())
          .to.eventually.eql(true);

        variable.setNonNullBtn().click();
        expect(variable.valueValue()).to.eventually.eql('a value');
      });
    });


    describe('Boolean variable', function() {
      var variable2;
      before(function() {
        variable = page.variable('#example-1', 1);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Boolean');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('booleanVar');
      });

      describe('value input', function() {
        it('is a checkbox input', function() {
          expect(variable.valueValue()).to.eventually.eql('on');
          expect(variable.valueType()).to.eventually.eql('checkbox');
        });
      });
    });


    xdescribe('Bytes variable', function() {
      before(function() {
        variable = page.variable('#example-1', 2);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Bytes');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('bytesVar');
      });

      it('is always valid', function() {
        expect(variable.editingGroupClass())
          .not.to.eventually.match(/invalid/);
      });


      xdescribe('value input', function() {
        it('is disabled', function() {
          expect(variable.value().getAttribute('disabled')).to.eventually.eql('disabled');
        });

        it('shows the object type', function() {
          expect(variable.valueValue()).to.eventually.eql('');
        });
      });
    });


    describe('Date variable', function() {
      before(function() {
        variable = page.variable('#example-1', 2);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Date');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('dateVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('2015-03-23T13:14:06.340');
      });


      describe('default value', function() {
        before(function() {
          variable.typeSelect('String');
          variable.value().clear();
          variable.setNullBtn().click();
          variable.setNonNullBtn().click();
          variable.typeSelect('Date');
        });

        after(function() {
          variable.value().clear().sendKeys('2015-03-23T13:14:06.340');
        });

        it('is a valid camunda date', function() {
          expect(variable.valueCss())
            .not.to.eventually.match(/invalid/);
        });
      });


      xdescribe('value input', function() {
        it('has a datepicker button', function() {
          expect(variable.value().element(by.css('.btn')).isPresent()).to.eventually.eql(true);
        });
      });
    });


    describe('Double variable', function() {
      before(function() {
        variable = page.variable('#example-1', 3);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Double');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('doubleVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('12.34');
      });
    });


    describe('Integer variable', function() {
      before(function() {
        variable = page.variable('#example-1', 4);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Integer');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('integerVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('1337');
      });
    });


    describe('Long variable', function() {
      before(function() {
        variable = page.variable('#example-1', 5);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Long');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('longVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('-100000000');
      });
    });


    describe('Null variable', function() {
      before(function() {
        variable = page.variable('#example-1', 6);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Null');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('nullVar');
      });

      it('has no value input', function() {
        expect(variable.valueText()).to.eventually.eql('CAM_WIDGET_VARIABLE_NULL');
      });
    });


    describe('Object variable', function() {
      before(function() {
        variable = page.variable('#example-1', 7);
      });


      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Object');
      });


      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('objectVar');
      });


      it('is always valid', function() {
        expect(variable.editingGroupClass())
          .not.to.eventually.match(/invalid/);
      });


      describe('popup to edit', function() {
        it('can be opened by a link', function() {
          expect(variable.valueModalLink().isDisplayed()).to.eventually.eql(true);
        });


        it('opens when its link is clicked', function() {
          // when
          variable.valueModalLink().click().then(function() {

            browser.sleep(500);

            // then
            expect(page.modal().node.isDisplayed()).to.eventually.eql(true);
          });
        });


        it('has a textarea with serialized value of variable', function() {
          var textarea = page.modal().textareaSerialized();

          expect(textarea.isDisplayed()).to.eventually.eql(true);
          expect(textarea.getAttribute('readonly')).to.eventually.eql(null);
        });


        it('has a button to close itself', function() {
          expect(page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CLOSE').isDisplayed()).to.eventually.eql(true);
        });


        it('has a button to change the serialized value', function() {
          var changeBtn = page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CHANGE');

          expect(changeBtn.isDisplayed()).to.eventually.eql(true);
          expect(changeBtn.getAttribute('readonly')).to.eventually.eql('true');
        });


        it('has an input for the object java class', function() {
          expect(page.modal().objectTypeInput().isDisplayed()).to.eventually.eql(true);
        });


        it('has an input for the serialization data format', function() {
          expect(page.modal().serializationTypeInput().isDisplayed()).to.eventually.eql(true);
        });


        it('can only be saved when something has been changed', function() {
          var changeBtn = page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CHANGE');
          var textarea = page.modal().textareaSerialized();

          textarea.getAttribute('value').then(function(original) {
            textarea.sendKeys('modified').then(function() {
              expect(changeBtn.getAttribute('readonly')).to.eventually.eql(null);

              textarea.clear().sendKeys(original).then(function() {
                expect(changeBtn.getAttribute('readonly')).to.eventually.eql('true');
              });
            });
          });
        });


        it('closes when the button is clicked', function() {
          // when
          page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CLOSE').click();

          // then
          expect(page.modal().node.isPresent()).to.eventually.eql(false);

          expect(variable.valueModalLink().getText()).to.eventually.eql('org.camunda.bpm.pa.service.CockpitVariable');
        });


        it('can pass the changed variable back', function() {
          variable.valueModalLink().click().then(function() {
            var changeBtn = page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CHANGE');

            page.modal().objectTypeInput().clear().sendKeys('papi.papo').then(function() {
              expect(changeBtn.getAttribute('disabled')).to.eventually.eql(null);

              changeBtn.click().then(function() {
                expect(variable.valueModalLink().getText()).to.eventually.eql('papi.papo');
              });
            });
          });
        });
      });
    });


    describe('Short variable', function() {
      before(function() {
        variable = page.variable('#example-1', 8);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Short');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('shortVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('-32768');
      });
    });


    describe('String variable', function() {
      before(function() {
        variable = page.variable('#example-1', 9);
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('String');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('stringVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('Some string value');
      });

      it('is valid when empty', function() {
        // when
        variable.value().clear();

        // then
        expect(variable.value().getAttribute('class'))
          .not.to.eventually.match(/invalid/);
      });
    });
  });


  describe('display', function() {
    before(function() {
      browser.get(pageUrl + '#example-2');
    });


    describe('Boolean', function() {
      var control;
      before(function() {
        variable = page.variable('#example-2', 1);
        control = page.variable('#example-1', 1);
      });

      it('prints "false" or "true"', function() {
        expect(variable.valueText()).to.eventually.eql('true');

        control.value().click().then(function() {
          expect(variable.valueText()).to.eventually.eql('false');
        });
      });
    });


    describe('Bytes', function() {
      before(function() {
        variable = page.variable('#example-2', 2);
      });

      it('prints nothing');
    });


    describe('Null', function() {
      before(function() {
        variable = page.variable('#example-2', 7);
      });

      it('prints nothing');
    });


    describe('Object', function() {
      before(function() {
        variable = page.variable('#example-2', 7);
      });

      it('prints the object Java class', function() {
        expect(variable.valueModalLink().getText())
          .to.eventually.eql('org.camunda.bpm.pa.service.CockpitVariable');
      });



      describe('popup to inspect', function() {
        it('can be opened by a link', function() {
          expect(variable.valueModalLink().isDisplayed()).to.eventually.eql(true);
        });


        it('opens when its link is clicked', function() {
          // when
          variable.valueModalLink().click().then(function() {
            // then
            expect(page.modal().node.isDisplayed()).to.eventually.eql(true);
          });
        });


        it('has a undeditable textarea with serialized value of variable', function() {
          var textarea = page.modal().textareaSerialized();
          expect(textarea.isDisplayed()).to.eventually.eql(true);
          expect(textarea.getAttribute('readonly')).to.eventually.eql('true');
        });


        it('has a button to close itself', function() {
          expect(page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CLOSE').isDisplayed()).to.eventually.eql(true);
        });


        it('does not have a button to change the serialized value', function() {
          expect(page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CHANGE').isPresent()).to.eventually.eql(false);
        });


        it('closes when the button is clicked', function() {
          // when
          page.modal().button('CAM_WIDGET_VARIABLE_DIALOG_BTN_CLOSE').click();

          // then
          expect(page.modal().node.isPresent()).to.eventually.eql(false);
        });
      });
    });
  });


  describe('partially shown', function() {
    describe('when value is set', function() {
      before(function() {
        browser.get(pageUrl + '#example-3');
      });

      it('shows the name and value', function() {
        variable = page.variable('#example-3', 0);
        expect(variable.nameValue()).to.eventually.eql('stringVar');
        expect(variable.valueValue()).to.eventually.eql('Some string value');
      });

      it('shows the type and value', function() {
        variable = page.variable('#example-3', 1);
        expect(variable.typeSelected()).to.eventually.eql('String');
        expect(variable.valueValue()).to.eventually.eql('Some string value');
      });

      it('shows the type and name', function() {
        variable = page.variable('#example-3', 2);
        expect(variable.typeSelected()).to.eventually.eql('String');
        expect(variable.nameValue()).to.eventually.eql('stringVar');
      });
    });
  });


  describe('partially disabled', function() {
    describe('when value is set', function() {
      before(function() {
        browser.get(pageUrl + '#example-5');
      });

      it('disables the name and value', function() {
        variable = page.variable('#example-5', 0);
        expect(variable.typeSelectElement().isEnabled()).to.eventually.eql(true);
        expect(variable.name().isEnabled()).to.eventually.eql(false);
        expect(variable.value().isEnabled()).to.eventually.eql(false);
      });

      it('disables the type and value', function() {
        variable = page.variable('#example-5', 1);
        expect(variable.typeSelectElement().isEnabled()).to.eventually.eql(false);
        expect(variable.name().isEnabled()).to.eventually.eql(true);
        expect(variable.value().isEnabled()).to.eventually.eql(false);
      });

      it('disables the type and name', function() {
        variable = page.variable('#example-5', 2);
        expect(variable.typeSelectElement().isEnabled()).to.eventually.eql(false);
        expect(variable.name().isEnabled()).to.eventually.eql(false);
        expect(variable.value().isEnabled()).to.eventually.eql(true);
      });
    });
  });

  describe('in form', function() {
    before(function() {
      browser.get(pageUrl + '#example-4');
    });

    it('enables button on valid input', function() {
      variable = page.variable('#example-4', 1);
      variable.typeSelect('Integer');
      variable.name().sendKeys('aName');
      variable.value().clear().sendKeys('153');

      expect(page.applyButton().isEnabled()).to.eventually.eql(true);
    });

    it('disables button on invalid input', function() {
      variable = page.variable('#example-4', 1);
      variable.typeSelect('Integer');
      variable.name().sendKeys('aName');
      variable.value().clear().sendKeys('153foo');

      expect(page.applyButton().isEnabled()).to.eventually.eql(false);
    });
  });
});
