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

## Future changes
The following changes can be safely made to the service/database
- Remove the mi stored procedure, its not used or called by anything.
- Remove the `Report` table
- Remove the Metadata tables, ras-rm is not configurable at all so having metadata tables causes more confusion than its worth.
- Remove the SampleOutboundPublisher, all it does is send messages to a routing key that has no bindings -> nothing is listening to these messages.
- rethink how we process a sample file, we are attempting to do it asynchronously but then instantly syncronising becuase opencsv is not threadsafe, not only does that make the code difficult to understand but it offers absolutely no performance gain. Its very confusing having the sample service publish a message to a queue that the sample service is itself listening to.
- This service needs a complete redesign and rewrite.


## Communicates with
- Party via a rest call `/party-api/v1/parties` for creating a new business
- Collection-exercise via rabbit for both `INIT` and `PERSISTED` sample units

## What it does
On file upload it creates a new SampleSummary entry in the database with State = INIT

SampleOuboundPublisher - It will then asynchronously parse the sample file and send a message with the routing key `Sample.SampleUploadStarted.binding`
However this routing key doesnt match the binding key `Sample.SampleDelivery.binding` for the `sample-outbound-exchange` so presumably this message will be descarded (Further analysis required). Collection exercise is listening to the `Sample.SampleDelivery` queue

CsvIngesterBusiness - Providing the SampleSummary type is "B" it will attempt to parse the uploaded CSV. Foreach line in the sample file create a new callable so this can all be sone asyncronously, pass in a new empty keyset. Our asyncronous callable is now syncronised because opencsv is not actually threadsafe!?!? The empty keyset is then used to store sample units where exceptions are thrown for duplicates.
We're converting each sample file line into a business sample unit. We construct a list of samples, calculate the size of the collection instrument (number of unique formtypes) then save each sample unit to the database and update the SampleSummary with the total number of collection instruments and total number of sample units.
For each sample unit; we create a Party Creation request and ship it off to the `Sample.Party` queue. Sample itself if listening on that queue and will pick up its own message to send to party via a rest call, then it sends an event to the event exchange where the event is `sample PERSISTED` (This exchange has no bindings associated with it so im guess this is just completely ignored and not required?).

SampleUnitDistributor - For each collection exercise job that has not been completed (jobcomplete=false), look for active sample summaries and send the `PERSISTED` sample unit to the `Sample.SampleDelivery` queue.
A lock is put on the Redis database because it can only be done synchronously - lock the redis database so we can safely query the postgres database, this is another massive smell that needs a redesign.

## Quick guide

- Sample Unit: A single row within the sample file
- Sample Summary: The entire collection of Sample Units in a sample file (Counting unique refs only, duplicates are discarded, only the first is kept)


## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)