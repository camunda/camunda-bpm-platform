exports.formatSlackBlockSection = function(text) {
  return `{
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "${text}"
      }
    }`;
}

exports.formatLink = function(linkUrl, title) {
  return `<${linkUrl}|${title}>`;
}

// links: Map link => title
exports.formatLinkList = function(links, prefix) {
  var linkList = "";
  links.forEach((title, linkUrl) => {
    linkList += prefix;
    linkList += '• ';
    linkList += exports.formatLink(linkUrl, title);
    linkList += '\\n';
  });
  
  return linkList;
  
}
