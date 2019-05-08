# README #

[![Build Status](https://travis-ci.com/ONSdigital/address-index-api.svg?token=wrHpQMWmwL6kpsdmycnz&branch=develop)](https://travis-ci.com/ONSdigital/address-index-api)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/83c0fb7ca2e64567b0998848ca781a36)](https://www.codacy.com/app/Valtech-ONS/address-index-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/address-index-api&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/ONSdigital/address-index-api/branch/develop/graph/badge.svg)](https://codecov.io/gh/ONSdigital/address-index-api)

### What is this repository for? ###

Address Index is a Play Framework (2.6) application which matches addresses.

The input can be a complete address (from any source), and the system will use advanced data science techniques to determine the most likely matching AddressBase entries with UPRNs (Unique Property Reference Numbers).

Addresses can be matched one at a time or in batches.

Additional functions exist for postcode searching and partial address string matching for typeaheads.

It is planned to deploy the application in the near future as a service intended to be freely available to all members of the Public Sector Mapping Agreement.

### How do I get set up? ###

1) Required Installations

    * Java 1.8 
    * sbt 0.13.13 (or 0.13.16)
    * Scala 2.12.4
    * Elasticsearch 5.6.7
    * An IDE such as IntelliJ is recommended

2) Create Project from GitHub (IntelliJ shown as example)

    * File, New, Project From Version Control, GitHub
    * Git Repository URL - select https://github.com/ONSdigital/address-index-api 
    * Parent Directory: any local drive, typically your IdeaProject directory
    * Directory Name: address-index-api or address-index-data
    * Clone

    The references in the build.sbt are used to draw down additional components

3) Run

    * The project consists of an assembly of several subprojects - server, parsers, model and demo-ui
    * The list of sub projects can be seen by running sbt projects from the root of the project.
    * The list contains the project IDs that must be used for all sbt commands which require a Project ID to be supplied, for example:
    * sbt "project address-index-server" run

    * The application.conf of the demo-ui can point to the API on localhost or a deployed copy of the API
    * The application.conf of the server project points to an elastic search endpoint, this can be local or a server

    To run or test the demo-ui and server together on your local machine:
    
    Open two command windows running sbt as shown above, one for the API and one for UI
    
    Use run 9001 to have the API on port 9001 and run 9000 to have the UI on 9000
    (i.e. from the root of the address-index-api project run the following commands : 
        sbt "project address-index-server" "run 9001"
        sbt "project address-index-demo-ui" "run 9000"
    )
    If the UI's application.conf is set to look at localhost:9001 for the API it will work.

    Note that when working on the UI you can save changes and it will autodeploy. This doesn't work with the API becuase of the CRFSuite executable. You have to exit out of sbt and rerun.

### How do I run unit tests ###

sbt test

will run them all, or you can select a subproject, or use testOnly feature to restrict what is run.

### How do I run performance tests ###

See [Running Performance Tests](server/src/it/Running%20Performance%20Tests.md)

### Related Repos ###

[Address Index Data](https://github.com/ONSdigital/address-index-data) - Apache Spark job used to create the Elasticsearch index

[Address Index Developers](https://github.com/ONSdigital/address-index-developers) - Flask web site for API users

[Address Index UI](https://github.com/ONSdigital/address-index-ui) - New Flask UI to replace current demo-ui

### What if I just want to use the API ###

See [API Help and Swagger](api-definitions/readme.md)
