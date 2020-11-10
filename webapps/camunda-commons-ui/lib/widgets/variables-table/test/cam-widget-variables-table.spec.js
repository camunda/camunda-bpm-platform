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
              '/lib/widgets/variables-table/test/cam-widget-variables-table.spec.html';

var page = require('./cam-widget-variables-table.page.js');

describe('Variables Table', function() {
  var variable;





  describe('editing', function() {
    before(function() {
      browser.get(pageUrl + '#example-1');
    });

    describe('access', function() {
      before(function() {
        variable = page.variable('#example-1', 0);
      });



      it('can be entered by clicking the pencil button', function() {
        variable.enterEditMode();

        expect(variable.classes()).to.eventually.match(/editing/);
        expect(variable.revertButton().isDisplayed()).to.eventually.eql(true);
      });


      it('can be left by clicking the cross button', function() {
        variable.leaveEditMode();

        expect(variable.classes()).not.to.eventually.match(/editing/);
      });

      it('reverts the original state of the variable', function() {

      });
    });


    describe('revert button', function() {
      var originalValue;
      before(function() {
        // String variable
        variable = page.variable('#example-1', 9);

        variable.enterEditMode();

        variable.valueValue().then(function(val) {
          originalValue = val;
        });
      });

      after(function() {
        variable.enterEditMode();

        variable.valueInput().clear().sendKeys(originalValue);

        variable.leaveEditMode();
      });


      it('sets the variable to its original state', function() {
        variable.valueInput().clear().sendKeys('MC Hammer rulz');

        expect(variable.valueValue()).to.eventually.eql('MC Hammer rulz');

        variable.leaveEditMode();

        expect(variable.valueText()).to.eventually.eql(originalValue);
      });
    });


    describe('when empty', function() {
      before(function() {
        variable = page.variable('#example-1', 0);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });

      it('has a dropdown', function() {
        expect(variable.typeSelectElement().isDisplayed()).to.eventually.eql(true);
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('');
      });

      it('is not valid', function() {
        expect(variable.classes())
          .to.eventually.match(/invalid/);
      });
    });


    describe('validation', function() {
      before(function() {
        // the integer variable
        variable = page.variable('#example-1', 4);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });


      it('adds the invalid CSS class when value is not of the correct type', function() {
        variable.valueInput().clear();
        variable.valueInput().sendKeys('leet');

        expect(variable.classes())
          .to.eventually.match(/invalid/);
      });

      it('removes the invalid CSS class when value is of the correct type', function() {
        variable.valueInput().clear();
        variable.valueInput().sendKeys('1337');

        expect(variable.classes())
          .not.to.eventually.match(/invalid/);
      });



      it('adds the invalid CSS class when no name is given', function() {
        variable.nameInput().clear();

        expect(variable.nameValue()).to.eventually.eql('');

        expect(variable.classes())
          .to.eventually.match(/invalid/);
      });

      it('removes the invalid CSS class when a name is given', function() {
        variable.nameInput().clear();
        variable.nameInput().sendKeys('integerVar');

        expect(variable.nameValue()).to.eventually.eql('integerVar');

        expect(variable.classes())
          .not.to.eventually.match(/invalid/);
      });
    });


    describe('"null" support', function() {
      before(function() {
        // the empty variable
        variable = page.variable('#example-1', 0);
        variable.enterEditMode();

        variable.typeSelect('String');
        variable.nameInput().sendKeys('aName');
        variable.valueInput().sendKeys('a value');
      });

      after(function() {
        variable.leaveEditMode();
      });



      it('allows to set a variable value to "null"', function() {
        expect(variable.setNullButton().isPresent())
          .to.eventually.eql(true);

        expect(variable.setNonNullButton().isPresent())
          .to.eventually.eql(false);
      });

      it('allows to revert a variable value to its previous value', function() {
        variable.setNullButton().click();

        expect(variable.setNullButton().isPresent())
          .to.eventually.eql(false);

        expect(variable.setNonNullButton().isPresent())
          .to.eventually.eql(true);

        variable.setNonNullButton().click();
        expect(variable.valueValue()).to.eventually.eql('a value');
      });
    });


    describe('Boolean variable', function() {
      var variable2;
      before(function() {
        variable = page.variable('#example-1', 1);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
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


    describe('Bytes variable', function() {
      before(function() {
        variable = page.variable('#example-1', 10);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Bytes');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('bytesVar');
      });

      xit('is always valid', function() {
        expect(variable.classes()).not.to.eventually.match(/invalid/);
      });
    });


    describe('Date variable', function() {
      before(function() {
        variable = page.variable('#example-1', 2);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
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
          variable.valueInput().clear();
          variable.setNullButton().click();
          variable.setNonNullButton().click();
          variable.typeSelect('Date');
        });

        after(function() {
          variable.valueInput().clear().sendKeys('2015-03-23T13:14:06.340');
        });

        it('is a valid camunda date', function() {
          expect(variable.classes())
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
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
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
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Integer');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('integerVar');
      });

      it('has a value input', function() {
        expect(variable.valueValue()).to.eventually.eql('1000');
      });
    });


    describe('Long variable', function() {
      before(function() {
        variable = page.variable('#example-1', 5);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
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
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });

      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Null');
      });

      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('nullVar');
      });

      it('has no value input', function() {
        expect(variable.valueText()).to.eventually.eql('CAM_WIDGET_VARIABLES_TABLE_NULL');
      });
    });


    describe('Object variable', function() {
      before(function() {
        variable = page.variable('#example-1', 7);
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });


      it('has a dropdown', function() {
        expect(variable.typeSelected()).to.eventually.eql('Object');
      });


      it('has a name input', function() {
        expect(variable.nameValue()).to.eventually.eql('objectVar');
      });


      it('is always valid', function() {
        expect(variable.classes())
          .not.to.eventually.match(/invalid/);
      });


      describe('popup to edit', function() {
        it('can be opened by a link', function() {
          expect(variable.valueModalLink().isDisplayed()).to.eventually.eql(true);
        });


        it('opens when its link is clicked', function() {
          // when
          variable.valueModalLink().click();

          // then
          expect(page.modal().node.isDisplayed()).to.eventually.eql(true);
        });


        it('has a textarea with serialized value of variable', function() {
          var textarea = page.modal().textareaSerialized();

          expect(textarea.isDisplayed()).to.eventually.eql(true);
          expect(textarea.getAttribute('readonly')).to.eventually.eql(null);
        });


        it('has a button to close itself', function() {
          expect(page.modal().button('Close').isDisplayed()).to.eventually.eql(true);
        });


        it('has a button to change the serialized value', function() {
          var changeButton = page.modal().button('Change');

          expect(changeButton.isDisplayed()).to.eventually.eql(true);
          expect(changeButton.getAttribute('disabled')).to.eventually.eql('true');
        });


        it('has an input for the object java class', function() {
          expect(page.modal().objectTypeInput().isDisplayed()).to.eventually.eql(true);
        });


        it('has an input for the serialization data format', function() {
          expect(page.modal().serializationTypeInput().isDisplayed()).to.eventually.eql(true);
        });


        it('can only be saved when something has been changed', function() {
          var changeButton = page.modal().button('Change');
          var textarea = page.modal().textareaSerialized();

          textarea.getAttribute('value').then(function(original) {
            textarea.sendKeys('modified').then(function() {
              expect(changeButton.getAttribute('disabled')).to.eventually.eql(null);

              textarea.clear().sendKeys(original).then(function() {
                expect(changeButton.getAttribute('disabled')).to.eventually.eql('true');
              });
            });
          });
        });


        it('closes when the button is clicked', function() {
          // when
          page.modal().button('Close').click().then(function() {

            // then
            expect(page.modal().node.isPresent()).to.eventually.eql(false);
          });

          expect(variable.valueModalLink().getText()).to.eventually.eql('org.camunda.bpm.pa.service.CockpitVariable');
        });


        it('can pass the changed variable back', function() {
          variable.valueModalLink().click().then(function() {
            var changeButton = page.modal().button('Change');

            page.modal().objectTypeInput().clear().sendKeys('papi.papo').then(function() {
              expect(changeButton.getAttribute('disabled')).to.eventually.eql(null);

              changeButton.click().then(function() {
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
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
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
        variable.enterEditMode();
      });

      after(function() {
        variable.leaveEditMode();
      });

      describe('popup to show long variable', function() {
        before(function() {
          variable.leaveEditMode();
        });

        after(function() {
          variable.enterEditMode();
        });

        it('can be opened by a link', function() {
          expect(variable.stringModalLink().isDisplayed()).to.eventually.eql(true);
        });


        it('opens when its link is clicked', function() {
          // when
          variable.stringModalLink().click();

          // then
          expect(page.modal().node.isDisplayed()).to.eventually.eql(true);
        });


        it('has a textarea with serialized value of variable', function() {
          var textarea = page.modal().textareaSerialized();

          expect(textarea.isDisplayed()).to.eventually.eql(true);
          expect(textarea.getAttribute('readonly')).to.eventually.eql('true');
        });


        it('has a button to close itself', function() {
          expect(page.modal().button('CAM_WIDGET_STRING_DIALOG_LABEL_CLOSE').isDisplayed()).to.eventually.eql(true);
        });

        it('closes when the button is clicked', function() {
          // when
          page.modal().button('CAM_WIDGET_STRING_DIALOG_LABEL_CLOSE').click();

          // then
          expect(page.modal().node.isPresent()).to.eventually.eql(false);

          expect(variable.stringModalLink().getText()).to.eventually.eql('Some string value');
        });
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
        variable.valueInput().clear();

        // then
        expect(variable.classes())
          .not.to.eventually.match(/invalid/);
      });
    });
  });


  describe('cam-headers attribute', function() {
    before(function() {
      browser.get(pageUrl + '#example-2');
      variable = page.variable('#example-2', 2);
    });

    it('can be used to display the wanted columns', function() {
      var headers = element.all(by.css('#example-2 .first thead td'));

      expect(headers.count()).to.eventually.eql(6);

      expect(headers.getText()).to.eventually.eql([
        'Plain',
        'Name',
        'Value',
        'Type',
        'Formatted',
        'Actions'
      ]);
    });

    it('shows the name, type, value and actions by default', function() {
      var headers = element.all(by.css('#example-1 thead td'));

      expect(headers.count()).to.eventually.eql(4);

      expect(headers.getText()).to.eventually.eql([
        'Name',
        'Value',
        'Type',
        'Actions'
      ]);
    });

    it('takes an object (expression) of column names', function() {
      var headers = element.all(by.css('#example-2 .second thead td'));

      expect(headers.count()).to.eventually.eql(3);

      expect(headers.getText()).to.eventually.eql([
        'Plain text',
        'Value',
        'Variable name',
      ]);
    });
  });


  describe('cam-editable attribute', function() {
    before(function() {
      browser.get(pageUrl + '#example-2');
      variable = page.variable('#example-2', 9);
    });


    it('takes an array (expression) of column names who should be editable', function() {
      variable.enterEditMode();

      expect(variable.nameInput().isPresent()).to.eventually.eql(false);

      expect(variable.valueInput().isPresent()).to.eventually.eql(true);

      expect(variable.typeSelectElement().isPresent()).to.eventually.eql(false);
    });


    it('hides the actions column if no other column is editable', function() {
      expect(element(by.css('#example-2 .second thead')).getText()).not.to.eventually.match(/Actions/);
    });
  });


  describe('on-save attribute', function() {
    before(function() {
      browser.get(pageUrl + '#example-1');
      variable = page.variable('#example-1', 9);
    });


    it('can be used to interact with the rest of the app', function() {
      variable.enterEditMode();
      variable.valueInput().clear().sendKeys('pipapo');
      variable.saveButton().click();

      var changeItems = element.all(by.css('#example-1 ol li'));

      expect(changeItems.count()).to.eventually.eql(1);

      expect(changeItems.getText()).to.eventually.match(/pipapo/);
    });
  });



  describe('on-delete attribute', function() {

  });



  describe('on-edit attribute', function() {

  });


  describe('is-variable-editable', function() {
    before(function() {
      browser.get(pageUrl + '#example-1');
      variable = page.variable('#example-1', 11);
    });

    it('should disable edit button', function() {
      expect(variable.editButton().isEnabled()).to.eventually.eql(false);
    });

    it('should disable delete button', function() {
      expect(variable.deleteButton().isEnabled()).to.eventually.eql(false);
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
      });


      it('prints "true" or "false"', function() {
        expect(variable.valueText()).to.eventually.eql('true');

        variable.enterEditMode();

        variable.valueInput().click();

        variable.saveButton().click();

        expect(variable.valueText()).to.eventually.eql('false');
      });
    });


    describe('Bytes', function() {
      before(function() {
        variable = page.variable('#example-2', 10);
      });


      it('prints a download link', function() {
        expect(variable.valueText()).to.eventually.eql('Download');
      });


      it('can use the `on-download` attribute to format the download URL', function() {
        expect(variable.value().element(by.css('a')).getAttribute('href'))
          .to.eventually.eql('http://i.ytimg.com/vi/2DzryjDrjCM/maxresdefault.jpg');
      });
    });


    describe('Null', function() {
      before(function() {
        variable = page.variable('#example-2', 6);
      });


      it('prints nothing', function() {
        expect(variable.valueText()).to.eventually.eql('');
      });
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
          browser.actions()
            .mouseMove(variable.valueModalLink())
            .perform();

          // when
          variable.valueModalLink().click();

          // then
          expect(page.modal().node.isDisplayed()).to.eventually.eql(true);
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


    describe('arbitrary content', function() {
      before(function() {
        variable = page.variable('#example-2', 9);
      });

      it('can be used with plain text', function() {
        expect(variable.textCell('plain').isPresent()).to.eventually.eql(true);

        expect(variable.textCell('plain').getText()).to.eventually.eql('variable #9');
      });

      it('can be used with compiled code', function() {
        expect(variable.textCell('formatted').isPresent()).to.eventually.eql(true);

        expect(variable.textCell('formatted').getText()).to.eventually.eql('HTMLSome string value');
      });
    });
  });
});
