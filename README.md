[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ddd594229c8641afae64acdb31c69745)](https://www.codacy.com/app/sdcplatform/rm-sample-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/rm-sample-service&amp;utm_campaign=Badge_Grade) [![Docker Pulls](https://img.shields.io/docker/pulls/sdcplatform/samplesvc.svg)]()
[![Build Status](https://travis-ci.org/ONSdigital/rm-sample-service.svg?branch=master)](https://travis-ci.org/ONSdigital/rm-sample-service)
[![codecov](https://codecov.io/gh/ONSdigital/rm-sample-service/branch/master/graph/badge.svg)](https://codecov.io/gh/ONSdigital/rm-sample-service)

# Sample Service
This repository contains the Sample service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/) and has the following responsibilities:

* Providing XML schemas that define the format of the sample
* Ingesting a sample file
* Storing the summary of that sample in its own database schema
* Sending the details of each sample unit in the sample (a business survey reporting unit or a census/social survey household) to the [Party service](https://github.com/ONSdigital/ras-party), which will be responsible for storing the sample unit details
* Receiving confirmation from the Party service once it has consumed/stored the sample units sent above, and marking the sample summary as ready for collection
* Providing on request the sample summaries matching a given criteria to the Collection Exercise service

## Running

There are two ways of running this service

* The easiest way is via docker (https://github.com/ONSdigital/ras-rm-docker-dev)
* Alternatively running the service up in isolation
    ```bash
    cp .maven.settings.xml ~/.m2/settings.xml  # This only needs to be done once to set up mavens settings file
    mvn clean install
    mvn spring-boot:run
    ```

Note. If you need to skip the integration tests due to port clashes run

```bash
mvn clean install -Ddocker.skip -DskipITs
```

## Testing
* In Documents create the following directory structure:

        sftp/business-sftp/
            /census-sftp/
            /social-sftp/

  These will act as a mock remote file store

* Add the xml files found in rm-sample-service/samplesvc-api/src/test/resources/xml to the corresponding sftp directories

* to add a collectionExerciseJob to the postgres database

      curl -H "Content-Type: application/json" -X POST -d '{ "collectionExerciseJobPK" : "4","surveyRef" : "str1234","exerciseDateTime" : "2012-12-13T12:12:12.000+00" }' http://localhost:8125/samples/sampleunitrequests

## API
See [API.md](https://github.com/ONSdigital/rm-sample-service/blob/master/API.md) for API documentation.

## Swagger Specifications
To view the Swagger Specifications for the Sample Service, run the service and navigate to http://localhost:8125/swagger-ui.html.

## Code Styler
To use the code styler please goto this url (https://github.com/google/google-java-format) and follow the Intellij instructions or Eclipse depending on what you use

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)