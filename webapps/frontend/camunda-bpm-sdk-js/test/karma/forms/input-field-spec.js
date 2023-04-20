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
var CamSDK = require('../../../lib/index-browser.js');

describe('The input field', function() {
  /* global jQuery: false */

  var $ = jQuery;

  var exampleVariableName = 'exampleVariableName';
  var exampleVariableStringValue = 'exampleVariableStringValue';
  var exampleVariableIntegerValue = 100;
  var exampleVariableFloatValue = 100.100;
  var exampleVariableBooleanValue = true;
  var exampleVariableDateValue = '2013-01-23T13:42:42';

  var VariableManager = CamSDK.Form.VariableManager;
  var InputFieldHandler = CamSDK.Form.fields.InputFieldHandler;
  var inputFieldTemplate = '<input type="text" />';
  var checkboxTemplate = '<input type="checkbox" />';
  var textareaTemplate = '<textarea></textarea>';


  it('should init the var name', function() {

    var variableManager = new VariableManager();

    // given:

    // an input field with 'cam-variable-name' directive
    var element = $(inputFieldTemplate).attr('cam-variable-name', exampleVariableName);

    // if:

    // I create an Input field
    new InputFieldHandler(element, variableManager);

    // then:

    // the variable is created in the variable manager
    var variable = variableManager.variable(exampleVariableName);

    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleVariableName);
    expect(variable.type).to.be.undefined;
    expect(variable.value).to.eql(null);
  });


  it('should init the var type', function() {

    var variableManager = new VariableManager();

    // given:

    // an input field with 'cam-variable-name' and 'cam-variable-type' directive
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String');

    // if:

    // I create an Input field
    new InputFieldHandler(element, variableManager);

    // then:

    // the variable is created in the variable manager
    var variable = variableManager.variable(exampleVariableName);

    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleVariableName);
    expect(variable.type).to.eql('String');
    expect(variable.value).to.eql('');
  });

  it('should init the variable value', function() {

    var variableManager = new VariableManager();

    // given:

    // an input field with 'cam-variable-name' and 'cam-variable-type' directive and an initial value
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String')
      .val(exampleVariableStringValue);

    // if:

    // I create an Input field
    new InputFieldHandler(element, variableManager);

    // then:

    // the variable is created in the variable manager
    var variable = variableManager.variable(exampleVariableName);

    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleVariableName);
    expect(variable.type).to.eql('String');
    expect(variable.value).to.eql(exampleVariableStringValue);
  });


  it('should get a string value from the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);
    // defined variable ...
    var variable = variableManager.variable(exampleVariableName);
    // without value
    expect(variable.value).to.eql('');

    // if:

    // I set the value of the input field
    element.val(exampleVariableStringValue);
    // and get it using the field handler
    inputFieldHandler.getValue();

    // then:

    // the value is set in the variable manager
    expect(variable.value).to.eql(exampleVariableStringValue);
  });


  it('should apply a string value to the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);
    // defined variable ...
    var variable = variableManager.variable(exampleVariableName);
    // without value
    expect(variable.value).to.eql('');

    // if:

    // I set the value to the variable
    variable.value = exampleVariableStringValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.val()).to.eql(exampleVariableStringValue);
  });


  it('should work with a textarea', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(textareaTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);
    // defined variable ...
    var variable = variableManager.variable(exampleVariableName);
    // without value
    expect(variable.value).to.eql('');

    // if:

    // I set the value to the variable
    variable.value = exampleVariableStringValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.val()).to.eql(exampleVariableStringValue);

  });
  ////////////////////// Integer //////////////////////////

  it('should convert empty string to "null" for Integer', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Integer');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set an empty string to the field
    element.val('');
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is Null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should not accept Float values for Integers', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Integer');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to a float value
    element.val(exampleVariableFloatValue);
    // then
    // getValue throws an exception
    expect(function() {
      inputFieldHandler.getValue();
    }).to.throw();
    // and the value in the variable is still null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should get an Integer value from the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Integer');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the field
    element.val(exampleVariableIntegerValue);
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is an integer
    expect(variableManager.variable(exampleVariableName).value)
      .to.eql(exampleVariableIntegerValue);
  });


  it('should set an Integer value to the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Integer');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = exampleVariableIntegerValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.val()).to.eql(exampleVariableIntegerValue.toString());

  });

  ////////////////////// Float //////////////////////////

  it('should convert empty string to "null" for Float', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Float');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set an empty string to the field
    element.val('');
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is Null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should not accept String values for Floats', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Float');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to a String value
    element.val(exampleVariableStringValue);

    // then
    // getValue throws an exception
    expect(function() {
      inputFieldHandler.getValue();
    }).to.throw();

    // and the value in the variable is still null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should get an Float value from the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Float');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the field
    element.val(exampleVariableFloatValue);
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is an integer
    expect(variableManager.variable(exampleVariableName).value)
      .to.eql(exampleVariableFloatValue);

  });

  it('should set an Float value to the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Float');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = exampleVariableFloatValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.val()).to.eql(exampleVariableFloatValue.toString());

  });

  ////////////////////// Boolean //////////////////////////

  it('should convert empty string to "null" for Boolean', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set an empty string to the field
    element.val('');
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is Null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should not accept String values for Booleans', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to a String value
    element.val(exampleVariableStringValue);

    // then
    // getValue throws an exception
    expect(function() {
      inputFieldHandler.getValue();
    }).to.throw();

    // and the value in the variable is still null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should get a Boolean value from the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the field
    element.val(exampleVariableBooleanValue.toString());
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is a boolean
    expect(variableManager.variable(exampleVariableName).value)
      .to.eql(exampleVariableBooleanValue);
  });


  it('should set a Boolean value to the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = exampleVariableBooleanValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.val()).to.eql(exampleVariableBooleanValue.toString());

  });

  ////////////////////// Boolean Checkbox //////////////////////////

  it('should initialize a boolean value', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(checkboxTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');



    // if:
    new InputFieldHandler(element, variableManager);

    // I set the value to the variable

    // then:
    var variable = variableManager.variable(exampleVariableName);

    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleVariableName);
    expect(variable.type).to.eql('Boolean');
  });


  it('should get a Boolean value from the checkbox control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(checkboxTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:
    // I check the checkbox
    element.prop('checked', exampleVariableBooleanValue);
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is a boolean
    expect(variableManager.variable(exampleVariableName).value)
      .to.eql(exampleVariableBooleanValue);
  });


  it('should set a Boolean value to the checkbox control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(checkboxTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = exampleVariableBooleanValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.prop('checked')).to.eql(exampleVariableBooleanValue);
  });


  it('should handle null values with the checkbox control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(checkboxTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = null;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.prop('checked')).to.eql(false);
  });


  it('should have a boolean value after getting a checkbox input', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(checkboxTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Boolean');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = null;
    // and apply the input field
    inputFieldHandler.applyValue();

    // the value is set to the form control
    expect(element.prop('checked')).to.eql(false);
    expect(variable.value).to.eql(null);

    // if:
    inputFieldHandler.getValue();

    // then:
    expect(variable.value).to.eql(false);
  });

  ////////////////////// Date //////////////////////////

  it('should convert empty string to "null" for Date', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Date');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set an empty string to the field
    element.val('');
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is Null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should not accept String values for Dates', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Date');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to a String value
    element.val(exampleVariableStringValue);

    // then
    // getValue throws an exception
    expect(function() {
      inputFieldHandler.getValue();
    }).to.throw();

    // and the value in the variable is still null
    expect(variableManager.variable(exampleVariableName).value).to.be.null;
  });


  it('should get a Date value from the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Date');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the field
    element.val(exampleVariableDateValue.toString());
    // and get the value from the input field
    inputFieldHandler.getValue();

    // then:

    // the value in the variable manager is a boolean
    expect(variableManager.variable(exampleVariableName).value)
      .to.eql(exampleVariableDateValue);

  });

  it('should set a Date value to the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized input handler
    var element = $(inputFieldTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Date');

    var inputFieldHandler = new InputFieldHandler(element, variableManager);

    // if:

    // I set the value to the variable
    var variable = variableManager.variable(exampleVariableName);
    variable.value = exampleVariableDateValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the value is set to the form control
    expect(element.val()).to.eql(exampleVariableDateValue.toString());

  });
});
