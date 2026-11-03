# JQS Monitoring Plugin

<a href="https://jenkins.io">
    <img width="400" src="https://www.jenkins.io/images/jenkins-logo-title-dark.svg" alt="Jenkins logo"> 
</a>

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fjqs-monitoring-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/jqs-monitoring-plugin/job/master/)
[![Popularity](https://img.shields.io/jenkins/plugin/i/jqs-monitoring.svg)](https://plugins.jenkins.io/jqs-monitoring/)

## Table of Contents

- [What does it do?](#what-does-it-do)
- [Usage](#usage)
- [Environment](#environment)

## What does it do?

This plugin show information about jobs, agents and the queue in Jenkins.

![Just the main plugin view](docu/main.png)

The JQS-Monitoring Plugin makes it possible to **monitor what is happening in the job queue**, what is the status of each agent or each job that is being build at the moment. You can also get information about the number of failed builds for the last 24 hours in a histogram graphic. With the plugin you can easily detect if a job is stuck in the queue or being built for too long. The information for the different components can be viewed separately in single pages or simultaneously in one big page. The links to this pages can be found in the left menu-panel of the plugin.

This menu-panel also contains a link to the configuration page, where a number of options can be configured to change the way information is shown(size, color, hide/show options).

![And bit of configuration](docu/config.png)


## Usage

The plugin can be used to show information on small or big monitors, the different components can be added to other pages using the links. For example you can use iframes to continuously monitor several Jenkins instances.

**The plugin will appear in the left-side panel of the jenkins homepage.** Its icon and links are available only for users with administrator permission.


## Environment

The following build environment is required to build this plugin:

* `java-25` and `maven-3.9.16` 

Remembering that whenever there are changes about, consult the new releases, anyway this documentation will always be updated.