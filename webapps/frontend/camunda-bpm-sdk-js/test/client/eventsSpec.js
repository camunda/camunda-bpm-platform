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

var isArray = Array.isArray;

describe('The events system', function() {
  var Events, obj = {};
  var counters = {
    on: 0,
    once: 0,
    other: 0
  };

  function onEventCB() {
    counters.on++;
  }

  function onceEventCB() {
    counters.once++;
  }

  function otherEventCB() {
    counters.other++;
  }

  it('does not blow when loading', function() {
    expect(function() {
      Events = require('./../../lib/events');
    }).not.to.throw();
  });


  describe('`attach` function', function() {
    it('is used to provide events to an object', function() {
      expect(function() {
        Events.attach(obj);
      }).not.to.throw();

      expect(typeof obj.on).to.eql('function');

      expect(typeof obj.once).to.eql('function');

      expect(typeof obj.off).to.eql('function');

      expect(typeof obj.trigger).to.eql('function');

      expect(typeof obj._events).to.eql('object');
    });
  });


  describe('`on` function', function() {
    it('is a function', function() {
      expect(typeof Events.on).to.eql('function');

      expect(function() {
        Events.attach(obj);
      }).not.to.throw();

      expect(typeof obj.on).to.eql('function');
    });


    it('adds an event', function() {
      expect(obj).to.not.be.undefined;

      expect(typeof obj._events).to.eql('object');

      expect(function() {
        obj.on('some:event:name', onEventCB);
      }).not.to.throw();

      expect(isArray(obj._events['some:event:name'])).to.eql(true);

      expect(obj._events['some:event:name'].indexOf(onEventCB)).to.be.greaterThan(-1);
    });
  });


  describe('`trigger` function', function() {
    it('is a function', function() {
      expect(typeof Events.trigger).to.eql('function');
    });


    it('calls the functions assigned to the event', function() {
      expect(function() {
        obj.trigger('some:event:name');
        obj.trigger('some:event:name');
      }).not.to.throw();

      expect(counters.on).to.eql(2);
    });
  });


  describe('`once` function', function() {
    it('is a function', function() {
      expect(typeof Events.once).to.eql('function');
    });


    it('adds a function', function() {
      expect(function() {
        obj.once('other:event:name', onceEventCB);
      }).not.to.throw();
    });


    it('calls the added function', function() {
      expect(function() {
        obj.trigger('other:event:name');
        // console.info('counters.once', counters.once);
      }).not.to.throw();
    });


    it('removes the function after it has been called', function() {
      expect(function() {
        obj.trigger('other:event:name');
        // console.info('counters.once', counters.once);
      }).not.to.throw();

      expect(counters.once).to.eql(1);
    });
  });


  describe('`off` function', function() {
    it('is a function', function() {
      expect(typeof Events.off).to.eql('function');
    });


    it('removes a function assigned to an event', function() {
      expect(function() {
        obj.on('some:event:name', otherEventCB);
      }).not.to.throw();

      expect(isArray(obj._events['some:event:name'])).to.eql(true);

      expect(obj._events['some:event:name'].length).to.eql(2);


      expect(function() {
        obj.off('some:event:name', otherEventCB);
      }).not.to.throw();

      expect(isArray(obj._events['some:event:name'])).to.eql(true);

      expect(obj._events['some:event:name'].length).to.eql(1);
    });


    it('removes all the functions assigned to an event', function() {
      expect(function() {
        obj.off('some:event:name');
      }).not.to.throw();

      expect(obj._events['some:event:name']).to.be.undefined;
    });
  });
});
