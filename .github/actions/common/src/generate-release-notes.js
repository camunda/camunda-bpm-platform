const core = require('@actions/core');
const github = require('@actions/github');
const slackUtils = require('./utils/slack-utils.js');

module.exports = async function() {
  try {
    const versionsString = core.getInput('versions');
    const versionPrefix = core.getInput('version-prefix');
    const repoToken = core.getInput('repo-token');
    
    var versions = versionsString.split(",");
    versions.forEach(function(part, index) {
      this[index] = part.trim();
      
    }, versions);
    
    releaseNotes = new Map(); // releaseNotesUrl => link label
    
    const repo = github.context.payload.repository;
    const repoUrl = `${github.context.serverUrl}/${repo.owner.login}/${repo.name}`;
    
    versions.forEach(function(version) {
    
      const releaseNotesParams = encodeURIComponent(`is:issue label:${versionPrefix}${version}`);
      const searchUrl = `${repoUrl}/issues?q=${releaseNotesParams}`
      releaseNotes.set(searchUrl, `Release notes ${version}`);
    });
    
    const releaseNotesList = slackUtils.formatLinkList(releaseNotes, '    ');
    const releaseNotesSection = slackUtils.formatSlackBlockSection(`Release notes:\\n${releaseNotesList}`);
    
    core.setOutput('release-notes-block', releaseNotesSection);
    
  } catch (error) {
    core.setFailed(error.message);
  }
}


