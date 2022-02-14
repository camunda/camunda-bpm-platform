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
var sinon = require('sinon');
var isSidebarCollapsed = require('../isSidebarCollapsed');

describe('isSidebarCollapsed', function() {
  var callbackFunc;
  var $parse;
  var unregister;
  var $rootScope;
  var scope;
  var element;
  var attrs;

  beforeEach(function() {
    callbackFunc = sinon.spy();
    $parse = sinon.stub().returns(callbackFunc);

    unregister = sinon.spy();
    $rootScope = {
      $on: sinon.stub().returns(unregister)
    };

    scope = {
      $on: sinon.stub().callsArg(1)
    };

    element = {
      hasClass: sinon.stub().returns(true)
    };

    attrs = {
      $observe: sinon.stub().callsArgWith(1, 'attribute')
    };

    var linkFn = isSidebarCollapsed[2]($parse, $rootScope).link;

    linkFn(scope, element, attrs);
  });

  it('should observe isSidebarCollapsed attribute', function() {
    expect(attrs.$observe.calledWith('isSidebarCollapsed')).to.eql(true);
  });

  it('should parse attribute', function() {
    expect($parse.calledWith('attribute')).to.eql(true);
  });

  it('should notifify callback of collapsed state', function() {
    expect(callbackFunc.calledOnce).to.eql(true);
    expect(
      callbackFunc.calledWith({
        collapsed: true
      })
    );
  });

  it('should add restore, maximize and resize listeners', function() {
    expect($rootScope.$on.calledWith('restore')).to.eql(true);
    expect($rootScope.$on.calledWith('maximize')).to.eql(true);
    expect($rootScope.$on.calledWith('resize')).to.eql(true);
  });

  it('should notify callback when restore, maximize and resize event are fired', function() {
    callbackFunc.reset();

    $rootScope.$on.getCall(0).args[1]();
    $rootScope.$on.getCall(1).args[1]();
    $rootScope.$on.getCall(2).args[1]();

    expect(callbackFunc.callCount).to.eql(3);
  });

  it('should remove $rootScope event listeners', function() {
    expect(unregister.callCount).to.eql(3);
  });
});
