describe('googling', function() {

  beforeEach(function() {
    browser().navigateTo('/cockpit/');
    sleep(1);
  });

  it('should show name of process invoice', function() {
    expect(browser().location().path()).toBe("/dashboard");
    expect(element("a.tile").count()).toBe(1);
  });
});