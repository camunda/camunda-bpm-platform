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

describe('The select field', function() {
  /* global jQuery: false */
  var $ = jQuery;

  var exampleVariableLabel = '__exampleVariableLabel__';
  var exampleVariableName = '__exampleVariableName__';
  var exampleVariableStringValue = '__exampleVariableStringValue__';
  // var exampleVariableIntegerValue = 100;
  // var exampleVariableFloatValue = 100.100;
  // var exampleVariableBooleanValue = true;
  // var exampleVariableDateValue = '2013-01-23T13:42:42';

  var exampleChoicesVariableName = '__exampleChoicesVarName__';
  var exampleList = ['option1', 'option2'];
  var exampleStringMap = {
    option1: 'Option 1',
    option2: 'Option 2'
  };
  var exampleIntegerMap = {
    1: 'Option 1',
    2: 'Option 2'
  };

  var VariableManager = CamSDK.Form.VariableManager;
  var ChoicesFieldHandler = CamSDK.Form.fields.ChoicesFieldHandler;
  var selectTemplate = '<select />';


  function varElement() {
    return $(selectTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String')
      .append($('<option></option>')
        .text(exampleVariableLabel)
        .attr('value', exampleVariableStringValue)
        .attr('selected', 'selected'));
  }



  it('should init the var name', function() {

    var variableManager = new VariableManager();

    // given:

    // a select box with 'cam-variable-name' directive
    var element = $(selectTemplate).attr('cam-variable-name', exampleVariableName);

    // if:

    // I create an Input field
    new ChoicesFieldHandler(element, variableManager);

    // then:

    // the variable is created in the variable manager
    var variable = variableManager.variable(exampleVariableName);

    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleVariableName);
    expect(variable.type).to.be.undefined;
    expect(variable.value).to.be.false;
  });


  it('should init the var type', function() {
    var variableManager = new VariableManager();

    // given:

    // a select box with 'cam-variable-name' and 'cam-variable-type' directive
    var element = $(selectTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String');

    // if:

    // I create an Input field
    new ChoicesFieldHandler(element, variableManager);

    // then:

    // the variable is created in the variable manager
    var variable = variableManager.variable(exampleVariableName);
    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleVariableName);
    expect(variable.type).to.eql('String');

    // <!> different from <input> of type 'String'
    // If no value is selected, the variable value is expected to be 'null', regardless of whether
    // the selectbox is of type 'String' or not
    expect(variable.value).to.eql(null);
  });


  it('should init the variable value', function() {

    var variableManager = new VariableManager();

    // given:

    // an input field with 'cam-variable-name' and 'cam-variable-type' directive and an initial value
    var element = varElement();

    // if:

    // I create an Input field
    new ChoicesFieldHandler(element, variableManager);

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
    var element = varElement();

    // with no option selected
    element[0].selectedIndex = -1;


    var choicesHandler = new ChoicesFieldHandler(element, variableManager);
    // defined variable ...
    var variable = variableManager.variable(exampleVariableName);
    // without value
    expect(variable.value).to.eql(null);

    // if:

    // I set the value of the select box
    element.val(exampleVariableStringValue);
    // and get it using the field handler
    choicesHandler.getValue();

    // then:

    // the value is set in the variable manager
    expect(variable.value).to.eql(exampleVariableStringValue);
  });


  it('should apply a string value to the control', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized select
    var element = varElement();

    // with no option selected
    element[0].selectedIndex = -1;

    var inputFieldHandler = new ChoicesFieldHandler(element, variableManager);
    // defined variable ...
    var variable = variableManager.variable(exampleVariableName);
    // without value
    expect(variable.value).to.eql(null);

    // if:

    // I set the value to the variable
    variable.value = exampleVariableStringValue;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // it selects the value
    expect(element.val()).to.eql(exampleVariableStringValue);
  });


  // option handling (cam-choices) ////////////////////////

  it('should fetch cam-choices list', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized select
    var element = $(selectTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String')
      .attr('cam-choices', exampleChoicesVariableName);
    // with no option selected
    element[0].selectedIndex = -1;

    var inputFieldHandler = new ChoicesFieldHandler(element, variableManager);
    // defined choices variable.
    var variable = variableManager.variable(exampleChoicesVariableName);
    // without value
    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleChoicesVariableName);

    // if:

    // I set the choices variable
    variable.value = exampleList;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the choices are applied
    var options = $('option', element).map(function() {return $(this).val();}).get();
    expect(options).to.eql(exampleList);

    // still no value selected
    expect(element[0].selectedIndex).to.eql(-1);
  });


  it('should fetch cam-choices string map', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized select
    var element = $(selectTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'String')
      .attr('cam-choices', exampleChoicesVariableName);
    // with no option selected
    element[0].selectedIndex = -1;

    var inputFieldHandler = new ChoicesFieldHandler(element, variableManager);
    // defined choices variable.
    var variable = variableManager.variable(exampleChoicesVariableName);
    // without value
    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleChoicesVariableName);

    // if:

    // I set the choices variable
    variable.value = exampleStringMap;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the choices are applied
    var options = $('option', element).map(function() {return $(this).val();}).get();
    expect(options).to.eql(exampleList);

    // still no value selected
    expect(element[0].selectedIndex).to.eql(-1);
  });


  it('should fetch cam-choices integer map', function() {

    var variableManager = new VariableManager();

    // given:

    // an initialized select
    var element = $(selectTemplate)
      .attr('cam-variable-name', exampleVariableName)
      .attr('cam-variable-type', 'Integer')
      .attr('cam-choices', exampleChoicesVariableName);
    // with no option selected
    element[0].selectedIndex = -1;

    var inputFieldHandler = new ChoicesFieldHandler(element, variableManager);
    // defined choices variable.
    var variable = variableManager.variable(exampleChoicesVariableName);
    // without value
    expect(variable).to.not.be.undefined;
    expect(variable.name).to.eql(exampleChoicesVariableName);

    // if:

    // I set the choices variable
    variable.value = exampleIntegerMap;
    // and apply the input field
    inputFieldHandler.applyValue();

    // then:

    // the choices are applied
    var options = $('option', element).map(function() {return $(this).val();}).get();
    expect(options).to.eql(['1', '2']);

    // still no value selected
    expect(element[0].selectedIndex).to.eql(-1);

    // if

    // I select an option
    element.val('2');
    // and get it using the field handler
    inputFieldHandler.getValue();

    // then:

    // the value is set in the variable manager as Number (Integer)
    expect(variableManager.variableValue(exampleVariableName)).to.eql(2);


  });

});
