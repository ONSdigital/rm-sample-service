# Sample Service API
This page documents the Sample service API endpoints. These endpoints will be secured using HTTP basic authentication initially. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## Create Sample Unit Request
* `POST /sampleunitrequests` creates a sample unit request.

**Required parameters:** `collectionExerciseID` as the ID of the collection exercise, `collectionExerciseScheduledStart` as the date/time of the collection exercise and `surveyID` as the ID of the survey.

### Example JSON Request
```json
{
  "collectionExerciseID": "c6467711-21eb-4e78-804c-1db8392f93fb",
  "collectionExerciseActualPublish": "2017-06-01T00:00:00Z",
  "surveyID": "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87"
}
```

### Example JSON  Response
```json
{
  "sampleUnitsTotal": "670"
}
```

An `HTTP 201 Created` status code is returned if the sample unit request creation was a success. An `HTTP 400 Bad Request` is returned if any of the required parameters are missing, or if a sample unit request already exists for the same criteria.
