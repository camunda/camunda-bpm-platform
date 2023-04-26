---
name: Patch Release
about: Create an patch release task
title: 'Camunda Automation Platform 7.X.X, 7.Y.Y, 7.Z.Z are released'
labels: ["type:task", "group:release"]
assignees: 

---

### Acceptance Criteria (Required on creation)

- Tasks from the [patch release guide](https://confluence.camunda.com/display/AP/Performing+a+Patch+Release) are completed

### Hints

- Release date: `DD Month YYYY`

### Links

- [Release guide](https://confluence.camunda.com/display/AP/Performing+a+Patch+Release)

### Breakdown

**Pre phase**
- [ ] Schedule the Release.
- [ ] Create a Test plan.

**Phase 1**
- [ ] Monitor CI.
- [ ] Close the branch.
- [ ] Trigger the Release Build.
- [ ] Update the Enterprise Download page.
- [ ] Test the Release Build.
- [ ] Check the Docker Images.
- [ ] Unblock the branches.

**Phase 2**
- [ ] Release Javadocs.
- [ ] Publish the Enterprise download page.
- [ ] Release the documentation.
- [ ] Inform the support team and forward the security reports.
- [ ] Update GitHub project board.

```