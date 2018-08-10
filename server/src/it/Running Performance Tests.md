Table of Contents
=================

* [About these Performance Tests](#about-these-performance-tests)
* [Pre-requisites](#pre-requisites)
    * [Setup target project(s) locally](#setup-target-projects-locally)
        * [Setup address-index-api, address-index-demo-ui, address-index-data](#setup-address-index-api-address-index-demo-ui-address-index-data)
    * [Setup Gatling](#setup-gatling)
    * [OS Tuning](#os-tuning)
        * [MacOS](#macos)
        * [Windows (Blocked)](#windows-blocked)
* [Usage](#usage)
* [~~Running gatling tests from a docker container~~](#running-gatling-tests-from-a-docker-container)
* [Configuration](#configuration)
* [License](#license)


# About these Performance Tests

This project contains a suite of Gatling based performance tests targeted at endpoints for address-index-api Server.
Endpoint specific configuration has been included in this project and contains for example, specific parameter value for an endpoint.

Additionally, a generic get request option is also available which allows performance testing of any GET request endpoint.

Note that, this suite of tests can easily overload an endpoint to the extent that the server crashes.
Please ensure that you have the permission(s) and agreements in place before you execute this load test with a high number of RPS (requests per second).

# Pre-requisites

## Setup target project(s) locally

It is assumed that you have setup address-index-api server locally OR have an endpoint which you are allowed to load.
See below for links to this setup documentation.

### Setup address-index-api, address-index-demo-ui, address-index-data

Head over to the [following](https://collaborate2.ons.gov.uk/confluence/display/RAI/Setting+up+Address+Index+Server+and+UI+with+local+ElasticSearch) page on Confluence.

## Setup Gatling

Since this project uses Gatling's SBT plugin, all that is needed is to be able to use SBT.
As a part of setting up [address-index-api](#setup-address-index-api-address-index-demo-ui-address-index-data), you will have all the pre-requisites in place to run the gatling simulations in this project.

## OS Tuning

In order to subject any kind of meaningful load, OS tuning must be done in order to raise or un-restrict resource consumption limits imposed by consumer oriented OSes.

### MacOS

On macOS, the constraints most relevant to Gatling load tests include:

1. The maximum number of open files (`sysctl kern.maxfiles` )
1. The maximum number of open files per process (`sysctl kern.maxfilesperproc` )
1. The maximum number of open processes
1. The maximum number of open ports ('ephemeral port limit')
1. The maximum time before a TCP socket can be re-used

Instructions on altering all of the above limits is detailed in [macOS Tuning for Gatling](macOS%20Tuning%20for%20Gatling.md)

### Windows (Blocked)

A similar exercise from a Windows on-network machine would require elevated privileges since most required operations would require admin access (assuming that removes other security restrictions such as being able to edit the Windows registry).
At the time of this writing, it has not been possible to obtain either a Windows machine (off-network, admin access) OR admin access on the on-network Window laptop.
It is worth bearing in mind that the machine from which the Gatling tests are run must be capable of supporting running load tests of high RPS.

# Usage

The gatling simulations are executed using the official gatling SBT plugin. The parameters need to be passed in as `JAVA_OPTS`:

| Parameter Name            | Description                                                                  | Default Value       | Comments/Notes                                                        |
|:--------------------------|:-----------------------------------------------------------------------------|:--------------------|:----------------------------------------------------------------------|
| REQUESTS_PER_SECOND (RPS) | (Optional) Number of requests per second                                     | 10                  |                                                                       |
| BASE_URL                  | (Mandatory) Complete URL of endpoint with param  OR, `host:port of endpoint` | --                  | Need to be the complete url if CONFIG_NAME is **not** being specified |
| CONFIG_NAME               | (Optional) Name of the key in the configuration files `src/it/resources`     | generic_get_request |                                                                       |


The simplest first run would be run the suite of tests against a generic GET endpoint:
```shell
JAVA_OPTS="-DBASE_URL=https://host:port/getsomeresource?param1=value1&param2=value2" sbt "project address-index-server" "gatling-it:test" "gatling-it:lastReport"
```
Note that, since CONFIG_NAME was not passed in, the complete URL, with all parameters, must be passed in. The above uses a default RPS of 10.
Once the test is completed, the report will be opened in the default browser (thanks to `gatling-it:lastReport`).

To simply run *all* the simulation(s):
`JAVA_OPTS="-DCONFIG_NAME=ai-api-addresses-mock-data -DREQUESTS_PER_SECOND=101" sbt "gatling-it:test" "gatling-it:lastReport"`
this will run the gatling test against an endpoint defined under the key `ai-api-addresses-mock-data` in one of the configuration files under `src/it/resources` directory.

Customize the endpoint by using the `BASE_URL` property like so:
`JAVA_OPTS="-DCONFIG_NAME=ai-api-addresses-mock-data -DREQUESTS_PER_SECOND=1000 -DBASE_URL=http://localhost:9000" sbt "gatling-it:test" "gatling-it:lastReport"`

To run a specific simulation:
`JAVA_OPTS="-DCONFIG_NAME=ai-api-addresses-mock-data -DREQUESTS_PER_SECOND=101" sbt "gatling-it:testOnly uk.gov.ons.gatling.simulations.RegistersSimulation" "gatling-it:lastReport"`

A useful example is as follows, where the number of users is passed in, a specific config file is used and at the end of the simulation the reports is automatically opened:
`JAVA_OPTS="-DREQUESTS_PER_SECOND=100 -DCONFIG_NAME=ai-api-pcs-exeter" sbt "gatling-it:test" "gatling-it:lastReport"`
(The above runs the gatling simulation using the configuration under the ai-api-pcs-exeter key in the conf files in `src/it/resources` and assumes that the AI server is running locally, for example, like so:
```shell
JAVA_OPTS="-Xms2g -Xmx2g -DONS_AI_API_ES_PORT=9200 -DONS_AI_API_ES_URI=localhost -DONS_AI_API_HYBRID_INDEX_HIST=hybrid-historical_811_111017_1530276985432" \
sbt "project address-index-server" "run 9001"
```
The above runs the AI server and configures it to use ElasticSearch running at `localhost:9200` with index name specified by ONS_AI_API_HYBRID_INDEX_HIST property.
)

# ~~Running gatling tests from a docker container~~

This is work in progress.

Execute the following command:

```shell
docker run --rm -it -v /Users/ashjindal/ons/gatling/registers-performance-tests:/usr/perftest-workdir --env JAVA_OPTS="-DCONFIG_NAME=ai-api-simple-pcs -DREQUESTS_PER_SECOND=2000 -DBASE_URL=http://host.docker.internal:9001"  onsdigital/jenkins-slave-sbt
```

# Configuration

The list of configurable properties is visible in the `.conf` files in `src/test/resources/uk/gov/ons/conf/gatling/conf/`

# License

Copyright © 2017, [Office for National Statistics](https://www.ons.gov.uk)

Released under MIT license, see [LICENSE](LICENSE) for details.
