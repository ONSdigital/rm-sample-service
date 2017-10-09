# Sample Service API
This page documents the Sample service API endpoints. Apart from the Service Information endpoint, all these endpoints are secured using HTTP basic authentication. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## Service Information
* `GET /info` will return information about this service, collated from when it was last built.

### Example JSON Response
```json
{
  "name": "samplesvc",
  "version": "10.42.0",
  "origin": "git@github.com:ONSdigital/rm-sample-service.git",
  "commit": "4b53f1c4e56d5d59e753696ac524233626eaed64",
  "branch": "master",
  "built": "2017-07-12T13:18:28Z"
}
```

## Create Sample Unit Request
* `POST /sampleunitrequests` creates a sample unit request.

**Required parameters:** `collectionExerciseId` as the ID of the collection exercise, `exerciseDateTime` as the date/time of the collection exercise and `surveyRef` as the ID of the survey.

### Example JSON Request
```json
{
  "collectionExerciseId": "14fb3e68-4dca-46db-bf49-04b84e07e77c",
  "exerciseDateTime": "2017-08-29T23:00:00.000+0000",
  "surveyRef": "221"
}
```

### Example JSON Response
```json
{
  "sampleUnitsTotal": "670"
}
```

An `HTTP 201 Created` status code is returned if the sample unit request creation was a success. An `HTTP 400 Bad Request` is returned if any of the required parameters are missing, or if a sample unit request already exists for the same criteria.
