/* global describe: false, it: false, beforeEach: false, expect: false, browser: false, sleep: false, element: false */
describe('cockpit', function() {
  'use strict';

  beforeEach(function() {
    browser().navigateTo('/cockpit/');
    sleep(1);
  });

  it('should show dashboard with navigation', function() {
    expect(browser().location().path()).toBe('/dashboard');
    expect(element('a.tile').count()).toBe(3);

    var tileHeader = element('a.tile:first-child .tile-header');

    expect(tileHeader.text()).toMatch(/\s*CallActivity\s*/i);

    tileHeader.click();

    sleep(1);

    var h1 = element('.left-panel h1');

    expect(h1.text()).toMatch(/\s*CallActivity\s*/i);

    expect(element('#CallActivity_1 .currentActivityCount').text()).toBe('1');
  });
});
