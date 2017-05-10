# TODOs Code review
- Add more coverage to unit test SampleEndpointUnitTest. Check for instance https://github.com/ONSdigital/rm-sdx-gateway/blob/master/src/test/java/uk/gov/ons/ctp/sdx/endpoint/ReceiptEndpointUnitTest.java
- In SampleEndpoint, should @Valid be added to @RequestBody CollectionExerciseJobCreationRequestDTO collectionExerciseJobCreationRequestDTO as in https://github.com/ONSdigital/rm-sdx-gateway/blob/master/src/main/java/uk/gov/ons/ctp/sdx/endpoint/ReceiptEndpoint.java
- In SendToCollExQueueTest, you should not use setSampleUnitStateTransitionManager. You should remove this set method from SampleServiceImpl and find a way to autowire the object in test. If you can't find a solution, let me know and I can show you how to do it.
        - to do it, see https://github.com/ONSdigital/kirona-drs-gateway/blob/master/src/test/java/uk/gov/ons/ctp/response/kirona/drs/service/impl/ReceiptServiceImplTest.java and how  ReceiptServiceImplTestConfig is used to inject the classes required.


# Sample Service
This repository contains the Sample service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/) and has the following responsibilities:

* Providing XML schemas that define the format of the sample
* Ingesting a sample file
* Storing the summary of that sample in its own database schema
* Sending the details of each sample unit in the sample (a business survey reporting unit or a census/social survey household) to the [Party service](https://github.com/ONSdigital/ras-party), which will be responsible for storing the sample unit details
* Receiving confirmation from the Party service once it has consumed/stored the sample units sent above, and marking the sample summary as ready for collection
* Providing on request the sample summaries matching a given criteria to the Collection Exercise service

## Prerequisites

* To run this project you must have the following running:

    - rabbitMQ
    - redis
    - postgres
    - an sftp server

  You can get all of this using the following docker project:

    https://github.com/ONSdigital/rm-docker-dev

* You must first clone and build the following projects:

    - https://github.com/ONSdigital/rm-common-service
    - https://github.com/ONSdigital/rm-party-service-api

  and build them using

      mvn clean install

* In Documents create the following directory structure:

        sftp/business-sftp/
            /census-sftp/
            /social-sftp/

  These will act as a mock remote file store

* Add the xml files found in rm-sample-service/samplesvc-api/src/test/resources/xml to the corresponding sftp directories

* to run samplesvc

      cd code/rm-sample-service
      mvn clean install
      cd samplesvc
      ./mvnw spring-boot:run

* to add a collectionExerciseJob to the postgres database

      curl -H "Content-Type: application/json" -X POST -d '{ "collectionExerciseId" : "4","surveyRef" : "str1234","exerciseDateTime" : "2012-12-13T12:12:12.000+00" }' http://localhost:8125/samples/sampleunitrequests

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)
