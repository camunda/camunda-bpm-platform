'use strict';
/* jshint node:true */
describe('The - e2e - environement', function() {
  it('finds the page', function() {
    browser.get('/');
    expect(element(by.css('.page-wrap')).isPresent()).toBe(true);
    // browser.sleep(1000);
  });
});
