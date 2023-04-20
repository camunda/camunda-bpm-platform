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

var chai = require('chai');
var expect = chai.expect;
var sinon = require('sinon');
var directiveFactory = require('../directives/cam-hover-area');

describe('cam-common cam-hover-area', function() {
  var linkFn;
  var $element;
  var $transclude;
  var content;

  beforeEach(function() {
    linkFn = directiveFactory().link;
    $element = {
      empty: sinon.spy(),
      append: sinon.spy()
    };
    content = 'content';
    $transclude = sinon.stub().callsArgWith(0, content);

    linkFn(null, $element, null, null, $transclude);
  });

  it('should call $transclude', function() {
    expect($transclude.calledOnce).to.eql(true);
  });

  it('should call empty $element on transclusion', function() {
    expect($element.empty.calledOnce).to.eql(true);
  });

  it('should append new content to $elmenent on transclusion', function() {
    expect($element.append.calledWith(content)).to.eql(true);
  });
});
