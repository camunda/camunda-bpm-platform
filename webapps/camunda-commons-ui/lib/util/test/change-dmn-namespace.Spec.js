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

var expect = require('chai').expect;
var changeDmnNamespace = require('../change-dmn-namespace');
var fs = require('fs');

var oldXML = fs.readFileSync(__dirname + '/old-dmn.dmn', 'utf8');
var currentXML = fs.readFileSync(__dirname + '/current-dmn.dmn', 'utf8');

describe('changeDmnNamespace', function() {
  it('should change dmn namespace in xml file', function() {
    var newXML = changeDmnNamespace(oldXML);
    var lines = newXML.split('\n');
    var expectedLine = '<definitions xmlns="http://www.omg.org/spec/DMN/20151101/dmn.xsd"' +
      ' id="definitions" name="camunda" namespace="http://camunda.org/schema/1.0/dmn">';

    expect(lines[1]).to.contain(expectedLine);
  });

  it('should not change random occurrence of old namespace in text', function() {
    var result = changeDmnNamespace('http://www.omg.org/spec/DMN/20151101/dmn11.xsd');

    expect(result).to.eql('http://www.omg.org/spec/DMN/20151101/dmn11.xsd');
  });

  it('should not change current dmn file', function() {
    var newXML = changeDmnNamespace(currentXML);

    expect(newXML).to.contain(currentXML);
  });
});
