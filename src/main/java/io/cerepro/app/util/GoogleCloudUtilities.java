package io.cerepro.app.util;

import com.google.cloud.bigquery.*;
import com.google.cloud.language.v1.*;
import io.cerepro.app.AppApplication;
import io.cerepro.app.models.SentimentAnalysis;
import io.cerepro.app.models.SentimentReport;
import io.cerepro.app.services.SentimentAnalysisService;
import io.cerepro.app.services.SentimentReportService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.logging.Logger;

@Service
public class GoogleCloudUtilities {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;
    @Autowired
    private SentimentReportService sentimentReportService;

    private LanguageServiceClient languageServiceClient;

    // Method to query the data
    public SentimentReport fetchBigQueryFeedbackData(SentimentReport sentimentReport,
                                           String feedbackCategory,
                                           String feedbackSubcategory,
                                           String startDate,
                                           String endDate,
                                           int limit) {

        logger.info("In fetchBigQueryFeedbackData");
        BigQuery bigQuery = BigQueryOptions.newBuilder().setProjectId("tt-dp-prod").build().getService();
        System.out.println("BigQuery is using project: " + bigQuery.getOptions().getProjectId());

        QueryJobConfiguration queryJobConfiguration =
                QueryJobConfiguration.newBuilder(
                        "SELECT\n" +
                                "pf.feedback_id as feedback_id," +
                                "pf.help_center_feedback_category as feedback_category,\n" +
                                "pf.help_center_feedback_subcategory as feedback_subcategory,\n" +
                                "pii.feedback as feedback,\n" +
                                "pf.created_time as created_time\n" +
                                "FROM\n" +
                                "`ops.product_feedback` pf\n" +
                                "JOIN\n" +
                                "`ops_pii.product_feedback` pii\n" +
                                "ON\n" +
                                "pii.feedback_id = pf.feedback_id\n" +
                                "WHERE\n" +
                                "pf.help_center_feedback_category = '" + feedbackCategory + "'" +
                                "AND\n" +
                                "pf.help_center_feedback_subcategory = '" + feedbackSubcategory + "'" +
                                "AND\n" +
                                "created_time BETWEEN '" + startDate + "' AND '" + endDate +"'" +
                                "ORDER BY\n" +
                                "created_time DESC\n" +
                                "LIMIT\n" +
                                limit
                ).setUseLegacySql(false).build();

        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigQuery.create(JobInfo.newBuilder(queryJobConfiguration).setJobId(jobId).build());

        try {
            queryJob = queryJob.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (queryJob == null) {
            logger.error("Query job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            logger.error("There was some other error with the query");
        }

        QueryResponse queryResponse = bigQuery.getQueryResults(jobId);
        try {
            sentimentReportService.saveSentimentReport(sentimentReport);
            TableResult tableResult = queryJob.getQueryResults();
            SentimentAnalysis sentimentAnalysis;
            for (FieldValueList row : tableResult.iterateAll()) {
                String feedback = row.get("feedback").getStringValue();
                String id = row.get("feedback_id").getStringValue();
                if (feedback.length() >= 105) {
                    System.out.printf("Sending feedback " + id + " for analysis: %s%n", feedback);
                    sentimentAnalysis = new SentimentAnalysis(id, feedback);
                    analyzeSentiment(sentimentReport, sentimentAnalysis);
                } else logger.error("Feedback was shorter than 100 characters; skipping");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        computeSentimentReport(sentimentReport, languageServiceClient);

        return sentimentReport;
    }

    //TODO Separate method to create the Sentiment Analyses for each returned row

    private void analyzeSentiment(SentimentReport sentimentReport, SentimentAnalysis sentimentAnalysis) {
        logger.info("In analyzeSentiment utility");
        languageServiceClient = new AppApplication().getLanguageServiceClient();

        Entity entity;

        Document document = Document.newBuilder()
                .setContent(sentimentAnalysis.getSourceText()).setType(Document.Type.PLAIN_TEXT).build();

        Sentiment sentiment = languageServiceClient.analyzeSentiment(document).getDocumentSentiment();
        logger.info("Sentiment was: " + sentiment.getScore() + " at magnitude of " + sentiment.getMagnitude());

        //TODO: Determine if there is a min length requirement for strings

        AnnotateTextRequest.Features features = AnnotateTextRequest.Features.newBuilder()
                .setClassifyText(true)
                .setExtractDocumentSentiment(true)
                .setExtractEntities(true)
                .setExtractEntitySentiment(true)
                .build();
        AnnotateTextResponse response = languageServiceClient.annotateText(document, features, EncodingType.UTF8);
        logger.info("Overall sentiment is: " + response.getDocumentSentiment());
        logger.info("There were " + response.getEntitiesCount() + " entities in the entry:");
        for (int i = 0; i < response.getEntitiesCount(); i++) {
            entity = response.getEntities(i);
            logger.info("Entity " + i + " is " + entity.getName() + " and was mentioned " +
                    entity.getMentionsCount() + " times.");
        }

        sentimentAnalysis.setSentimentScore(sentiment.getScore());
        sentimentAnalysis.setSentiment(sentimentAnalysis.convertSentimentScore(sentiment.getScore()));
        sentimentAnalysis.setSentimentMagnitude(sentiment.getMagnitude());
        sentimentAnalysis.setAnalyzed(true);
        sentimentAnalysis.setSentimentReport(sentimentReport);
        sentimentAnalysisService.saveSentimentAnalysis(sentimentAnalysis);

        sentimentReport.addSentimentAnalysis(sentimentAnalysis);

        sentimentReportService.saveSentimentReport(sentimentReport);

        //TODO Figure out how to actually use a conditional to determine if analysis was successful
    }

    //TODO: This needs to be moved to a background task so as not to hang the UI (or a stateful index that shows in progress)
    private SentimentReport computeSentimentReport(SentimentReport sentimentReport,
                                                   LanguageServiceClient languageServiceClient) {
        double avgSentiment = 0.0;
        for (SentimentAnalysis s : sentimentReport.getSentimentAnalyses()) {
            avgSentiment += s.getSentimentScore();
        }
        sentimentReport.setAvgSentimentScore(avgSentiment / (double) sentimentReport.getSentimentAnalyses().size());
        sentimentReport.convertAndSetSentiment(avgSentiment / (double) sentimentReport.getSentimentAnalyses().size());


        // Aggregate all the text from this report's sentiment analyses into a single document text
        StringBuilder stringBuilder = new StringBuilder();
        for (SentimentAnalysis sa : sentimentReport.getSentimentAnalyses()) {
            logger.info("Adding " + sa.getSourceText() + " to report document text");
            stringBuilder.append(sa.getSourceText()).append("\n");
        }
        //TODO: Check the byte size before sending, bail out if it's too big
        Document aggregateDocument = Document.newBuilder()
                .setType(Document.Type.PLAIN_TEXT)
                .setContent(stringBuilder.toString())
                .build();
        logger.info(String.valueOf(aggregateDocument.getSerializedSize()));
        //TODO: Finish this if getSerializedSize is right
        //if (aggregateDocument.getSerializedSize() >= 1000000)

        // Analyze the aggregate document
        logger.info("Sending document for analysis with document sentiment, entity extraction, and entity sentiment");
        AnnotateTextRequest.Features features = AnnotateTextRequest.Features.newBuilder()
                .setExtractDocumentSentiment(true)
                .setExtractEntities(true)
                .setExtractEntitySentiment(true)
                .build();

        AnnotateTextResponse response = languageServiceClient.annotateText(aggregateDocument,
                features,
                EncodingType.UTF8);

        // Leverage the query results
        //TODO: Figure out how to leverage salience
        for (Entity e : response.getEntitiesList()) {
            System.out.println(e.getName() +
                    " mentioned " +
                    e.getMentionsCount() +
                    " times with sentiment of " +
                    e.getSentiment().getScore());
            if (e.getMentionsCount() > 5 && !e.getName().equals("Thumbtack")) {
                System.out.printf("Keyword %s mentioned more than 5 times; adding to report\n", e.getName());
                sentimentReport.addKeyword(e.getName(), e.getSentiment());
            }
        }

        sentimentReportService.saveSentimentReport(sentimentReport);

        return sentimentReport;
    }
}
