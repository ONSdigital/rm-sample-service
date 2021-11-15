package uk.gov.ons.ctp.response.sample.config;

import lombok.Data;

@Data
public class GCP {
  String project;
  String sampleSummaryActivationSubscription;
  String sampleSummaryActivationStatusTopic;
  String caseNotificationTopic;
  String collectionExerciseEndSubscription;
}
