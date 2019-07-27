# spring-cloud-config-s3
Spring cloud config S3 Repository implementation.

This implementation uses S3 as config backend for spring cloud config.

## How to use ?


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