/* global describe: false, it: false, element: false, by: false, expect: false, browser: false */
describe('angularjs homepage', function() {
  'use strict';
  it('should greet the named user', function() {
    browser.get('http://www.angularjs.org');

    element(by.model('yourName')).sendKeys('Julie');

    var greeting = element(by.binding('yourName'));

    expect(greeting.getText()).toEqual('Hello Julie!');
  });
});
