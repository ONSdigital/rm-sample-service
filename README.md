# Sample Service
This repository contains the Sample service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/) and has the following responsibilities:

* Providing XML schemas that define the format of the sample
* Ingesting a sample file
* Storing the summary of that sample in its own database schema
* Sending the details of each sample unit in the sample (a business survey reporting unit or a census/social survey household) to the [Party service](https://github.com/ONSdigital/ras-party), which will be responsible for storing the sample unit details
* Receiving confirmation from the Party service once it has consumed/stored the sample units sent above, and marking the sample summary as ready for collection
* Providing on request the sample summaries matching a given criteria to the Collection Exercise service

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)
