# Sample Service
This repository contains the Sample service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/) and has the following responsibilities:

* Storing the summary of that sample in its own database schema
* Storing the details of each sample unit in the sample (a business survey reporting unit)
* Sending sample units to [rm-case-service](https://github.com/ONSdigital/rm-case-service) to create cases

## How to use it

### Running

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

### Running tests

Tests can be run with  maven using the following command

```bash
mvn clean verify
```

### API
See [openapi.yaml](https://github.com/ONSdigital/rm-sample-service/blob/main/openapi.yaml) for API documentation.

### Code Styler
To use the code styler please goto this url (https://github.com/google/google-java-format) and follow the Intellij
instructions or Eclipse depending on what you use

The code styler can be run independently of the tests with the following command:
```bash
mvn fmt:format
```

## What it does

### Communicates with
- [ras-party](https://github.com/ONSdigital/ras-party) via a rest call to request reporting unit data and to link a sample summary to a collection exercise.
- [ras-collection-instrument](https://github.com/ONSdigital/ras-collection-instrument) to get the collection_instrument_id 
for the sample unit.
- [rm-collection-exercise-service](https://github.com/ONSdigital/rm-collection-exercise-service) via PubSub to inform 
how the enrichment and distribution of sample units to case is going.
- [rm-case-service](https://github.com/ONSdigital/rm-case-service) via PubSub to send sample units, that will end up
becoming cases.

  
### Sample and Sample summary creation
- Sample summary is created with the INIT state
- samples are individually set up against a sample summary (in the INIT state?)
- Response-operations-ui (or any service) hits the endpoint to check if all sample units have been loaded.  If so, move
  sample summary to ACTIVE state

### Sending samples to case for case creation
- Listens on the <topic> for a sample activation message
- Validates and enriches each sample by getting the partyId and collectionInstrumentId for each sample. It enriches by 
getting this data and validates by failing if it can't get both of these bits of data.
- Once validation and enrichment is complete, a message is sent on <topic> to inform rm-collection-exercise-service of
this fact.
- Distributes each sample that makes up a sample summary to case by putting them on <topic> that rm-case-service listens 
to
- Once distribution is complete, a message is sent on <topic> to inform rm-collection-exercise-service of this fact

## Quick guide

- Sample Unit: A single row within the sample file
- Sample Summary: The entire collection of Sample Units in a sample file (Counting unique refs only, duplicates are
  discarded, only the first is kept)


## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)
