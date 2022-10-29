const core = require('@actions/core');
const collectionUtils = require('./utils/collection-utils.js');
const jsonUtils = require('./utils/json-utils.js');
const slackUtils = require('./utils/slack-utils.js');

module.exports = async function() {
  try {
    const links = jsonUtils.parseJSONToMap(core.getInput('links'));
    const issues = jsonUtils.parseJSONToMap(core.getInput('issues'));
    const jiraProject = core.getInput('jira-project');
    
    const linkRegex = new RegExp(
      '\\bhttps?:\\/\\/\\S+(' + jiraProject + '-[0-9]+)',
      'i');
    
    const jiraProjectLinks = new Map(); // url => title
    
    links.forEach((githubIssues, webLink) => {
      const match = webLink.match(linkRegex);
      if (match) {
        const title = match[1];
        jiraProjectLinks.set(webLink, title);
      }
    });
    
    var linksBlocks = [];
    
    jiraProjectLinks.forEach((label, linkUrl) => {
      const issueIds = links.get(linkUrl);
      collectionUtils.sortIntArray(issueIds);
      
      var blockContent = slackUtils.formatLink(linkUrl, label);
      blockContent += '\\n';
      
      var issueLinks = new Map();
      issueIds.forEach(id => {
        var issue = issues.get(id);
        issueLinks.set(issue.url, `${issue.owner}/${issue.repo}#${issue.number}`);
      });
      
      blockContent += slackUtils.formatLinkList(issueLinks, '    ');
      
      linksBlocks.push(slackUtils.formatSlackBlockSection(blockContent));
    });
    
    const linksBlocksJson = linksBlocks.join(',');
    
    core.debug(`Result Slack Blocks: ${linksBlocksJson}'`);
    
    core.setOutput('links-blocks', linksBlocksJson);
    
  } catch (error) {
    core.setFailed(error.message);
  }
}

