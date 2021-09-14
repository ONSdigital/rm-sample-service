[![Docker Pulls](https://img.shields.io/docker/pulls/sdcplatform/samplesvc.svg)]()

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

## API
See [API.md](https://github.com/ONSdigital/rm-sample-service/blob/main/API.md) for API documentation.

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
- Collection-exercise via PubSub to inform how the enrichment and distribution of sample units to case is going

## What it does
On file upload it creates a new SampleSummary entry in the database with State = INIT

SampleOuboundPublisher - It will then asynchronously parse the sample file and send a message with the routing key `Sample.SampleUploadStarted.binding`
However this routing key doesnt match the binding key `Sample.SampleDelivery.binding` for the `sample-outbound-exchange` so presumably this message will be discarded (Further analysis required). Collection exercise is listening to the `Sample.SampleDelivery` queue

SampleUnitDistributor - For each collection exercise job that has not been completed (jobcomplete=false), look for active sample summaries and send the `PERSISTED` sample unit to the `Sample.SampleDelivery` queue.

## Quick guide

- Sample Unit: A single row within the sample file
- Sample Summary: The entire collection of Sample Units in a sample file (Counting unique refs only, duplicates are discarded, only the first is kept)


## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)
