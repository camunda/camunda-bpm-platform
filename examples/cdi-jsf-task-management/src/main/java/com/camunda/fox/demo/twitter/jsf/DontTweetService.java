package com.camunda.fox.demo.twitter.jsf;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

@Named
public class DontTweetService implements JavaDelegate {
  
  @Inject
  private TweetFeed tweetFeed;
  
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // hide tweet 
    if (tweetFeed == null) {
      System.out.println("NULL");
    }
    tweetFeed.addTweet("hidden tweet");
  }

}
