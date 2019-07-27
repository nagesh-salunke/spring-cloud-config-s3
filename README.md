# spring-cloud-config-s3
Spring cloud config server with S3 Repository implementation.

This implementation uses S3 as config backend for spring cloud config.

[![build status ](https://gitlab.com/salunkenagesh14/spring-cloud-config-s3/badges/master/pipeline.svg)](https://gitlab.com/salunkenagesh14/spring-cloud-config-s3/badges/master/pipeline.svg)

## How to use ?

Configure following parameters to use the s3 repository of your configuration.  

```
spring.cloud.config.server.s3.uri : Bucket name where config will is available.
spring.cloud.config.server.s3.metafile= # meta file for config version upgrade/maintain
```
metafile : Should have x-amz-meta-version as metadata that maintains current version of the configuration.

To enable polling configuration at certain interval

```
spring.cloud.config.server.s3.enabled=true
spring.cloud.config.server.s3.enablePollingschedule=true # To enable polling from s3 bucket
spring.cloud.config.server.s3.pollingRefreshRate=900000 # polling interval milliseconds
```

Config search paths : As given by spring cloud config [searchpaths](https://cloud.spring.io/spring-cloud-config/multi/multi__spring_cloud_config_server.html#_placeholders_in_git_search_paths)

## How it works ?
On config server startup a local copy of the config from s3 is made.
This copy is updated at interval mentioned by 'spring.cloud.config.server.s3.pollingRefreshRate'.  
Local copy is updated only if the metafile metadata "x-amz-meta-version" has changed.

You can
 - Enable your jenkins/deployment job to update the config + metafile on deployments.
 - Restart your spring-boot microservices to consume config server configuration.

## Parameters for config

```
spring.cloud.config.server.s3.enabled=true
spring.cloud.config.server.s3.searchPaths={profile},{profile}/{application}
spring.cloud.config.server.s3.enablePollingschedule=true # To enable polling from s3 bucket
spring.cloud.config.server.s3.pollingRefreshRate=900000 # polling interval milliseconds

aws.accessKey= # aws access key
aws.secretKey= # aws secret key
aws.region= # aws region

spring.cloud.config.server.s3.uri=s3://xxx
spring.cloud.config.server.s3.metafile= # meta file for config version upgrade/maintain
```