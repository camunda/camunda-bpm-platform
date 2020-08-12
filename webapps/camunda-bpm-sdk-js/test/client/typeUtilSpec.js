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

/* global describe, require, it */
'use strict';
var expect = require('chai').expect;

describe('The type-util', function() {

  var convertToType = require('./../../lib/forms/type-util').convertToType;
  var isType = require('./../../lib/forms/type-util').isType;
  var dateToString = require('./../../lib/forms/type-util').dateToString;

  it('does convert Integer', function() {

    expect(convertToType('100', 'Integer')).to.eql(100);
    expect(convertToType('-100', 'Integer')).to.eql(-100);

    expect(function() {
      return convertToType('100.10', 'Integer');
    }).to.throw('Value \'100.10\' is not of type Integer');

  });

  it('does convert Long', function() {
    expect(convertToType('100', 'Long')).to.eql(100);
    expect(convertToType('-100', 'Long')).to.eql(-100);
    expect(convertToType(' 0 ', 'Long')).to.eql(0);

    expect(function() {
      return convertToType('100.10', 'Long');
    }).to.throw('Value \'100.10\' is not of type Long');
  });

  it('does convert Short', function() {
    expect(convertToType('100', 'Short')).to.eql(100);
    expect(convertToType('-100', 'Short')).to.eql(-100);
    expect(convertToType(' 0 ', 'Short')).to.eql(0);

    expect(function() {
      return convertToType('100.10', 'Short');
    }).to.throw('Value \'100.10\' is not of type Short');
  });

  it('does convert Float', function() {

    expect(convertToType('100', 'Float')).to.eql(100);
    expect(convertToType('-100', 'Float')).to.eql(-100);
    expect(convertToType('100.10', 'Float')).to.eql(100.10);
    expect(convertToType('-100.10', 'Float')).to.eql(-100.10);

    expect(function() {
      return convertToType('100.10a', 'Float');
    }).to.throw('Value \'100.10a\' is not of type Float');

  });

  it('does convert Double', function() {

    expect(convertToType('100', 'Double')).to.eql(100);
    expect(convertToType('-100', 'Double')).to.eql(-100);
    expect(convertToType('100.10', 'Double')).to.eql(100.10);
    expect(convertToType('-100.10', 'Double')).to.eql(-100.10);

    expect(function() {
      return convertToType('100.10a', 'Double');
    }).to.throw('Value \'100.10a\' is not of type Double');
  });

  it('does convert Date', function() {

    // https://app.camunda.com/jira/browse/CAM-4746
    var date = new Date('2016-05-09T08:56:00');
    expect(typeof convertToType(date, 'Date')).to.eql('string');

    expect(convertToType('2013-01-23T13:42:42', 'Date')).to.eql('2013-01-23T13:42:42');
    expect(convertToType(' 2013-01-23T13:42:42 ', 'Date')).to.eql('2013-01-23T13:42:42');

    expect(function() {
      return convertToType('2013-01-23T13:42', 'Date');
    }).to.throw('Value \'2013-01-23T13:42\' is not of type Date');

    expect(function() {
      return convertToType('2013-01-23T60:42:40', 'Date');
    }).to.throw('Value \'2013-01-23T60:42:40\' is not of type Date');

  });

  it('does convert Boolean', function() {
    expect(convertToType('true', 'Boolean')).to.eql(true);
    expect(convertToType(' true', 'Boolean')).to.eql(true);
    expect(convertToType(' true ', 'Boolean')).to.eql(true);

    expect(convertToType('false', 'Boolean')).to.eql(false);
    expect(convertToType(' false', 'Boolean')).to.eql(false);
    expect(convertToType(' false ', 'Boolean')).to.eql(false);
    expect(convertToType('false ', 'Boolean')).to.eql(false);

    expect(function() {
      return convertToType('strue', 'Boolean');
    }).to.throw('Value \'strue\' is not of type Boolean');
  });

  it('detects Integers', function() {
    expect(isType('100', 'Integer')).to.eql(true);
    expect(isType('-100', 'Integer')).to.eql(true);
    expect(isType('100-', 'Integer')).to.eql(false);
  });

  it('detects Floats', function() {
    expect(isType('100', 'Float')).to.eql(true);
    expect(isType('-100', 'Float')).to.eql(true);
    expect(isType('-100e10', 'Float')).to.eql(true);
    expect(isType('-100.01', 'Float')).to.eql(true);
    expect(isType('100-', 'Float')).to.eql(false);
  });

  it('detects Booleans', function() {
    expect(isType('true', 'Boolean')).to.eql(true);
    expect(isType('false', 'Boolean')).to.eql(true);
    expect(isType('wahr', 'Boolean')).to.eql(false);
    expect(isType('1', 'Boolean')).to.eql(false);
    expect(isType('0', 'Boolean')).to.eql(false);
    expect(isType('', 'Boolean')).to.eql(false);
  });

  it('detects Dates', function() {
    var date = new Date('2016-05-09T08:56:00');
    expect(isType(date, 'Date')).to.eql(true);

    expect(isType('2013-01-23T13:42:42', 'Date')).to.eql(true);
    expect(isType('2013-01-23T27:42:42', 'Date')).to.eql(false);
    expect(isType('2013-13-23T13:42:42', 'Date')).to.eql(false);
    expect(isType('tomorrow', 'Date')).to.eql(false);
    expect(isType('2013-01-23D27:42:42', 'Date')).to.eql(false);
  });

});
