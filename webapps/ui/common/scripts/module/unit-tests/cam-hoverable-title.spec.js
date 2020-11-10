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
var directiveFactory = require('../directives/cam-hoverable-title');

describe('cam-common cam-hoverable-title', function() {
  var linkFn;
  var $element;
  var title;
  var $attr;
  var removeHoverListener;
  var HoverArea;

  beforeEach(function() {
    linkFn = directiveFactory().link;
    $element = {
      on: sinon.stub(),
      addClass: sinon.spy(),
      removeClass: sinon.spy()
    };
    title = 'title';
    $attr = {
      hoverClass: 'hovered',
      $observe: sinon.stub().callsArgWith(1, title)
    };
    removeHoverListener = sinon.spy();
    HoverArea = {
      addHoverListener: sinon.stub().returns(removeHoverListener)
    };

    linkFn(null, $element, $attr, HoverArea);
  });

  it('should add $element $destroy listener', function() {
    expect($element.on.calledWith('$destroy')).to.eql(true);
  });

  it('should observe cam-hoverable-title attribute', function() {
    expect($attr.$observe.calledWith('camHoverableTitle')).to.eql(true);
  });

  it('should add hover title listener', function() {
    expect(HoverArea.addHoverListener.calledWith(title)).to.eql(true);
  });

  it('should clean old hover listener when title changes', function() {
    $attr.$observe.getCall(0).args[1]('new title');

    expect(removeHoverListener.calledOnce).to.eql(true);
  });

  it('should clean hover listener on $destroy event', function() {
    $element.on.getCall(0).args[1]();

    expect(removeHoverListener.calledOnce).to.eql(true);
  });

  it('should add hover class when title is hovered', function() {
    HoverArea.addHoverListener.getCall(0).args[1](true);

    expect($element.addClass.calledWith($attr.hoverClass)).to.eql(true);
  });

  it('should remove hover class when title is not hovered', function() {
    HoverArea.addHoverListener.getCall(0).args[1](false);

    expect($element.removeClass.calledWith($attr.hoverClass)).to.eql(true);
  });
});
