package io.cerepro.app.controllers;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.language.v1.*;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;
import io.cerepro.app.AppApplication;
import io.cerepro.app.models.SentimentAnalysis;
import io.cerepro.app.services.SentimentAnalysisService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.WebParam;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Controller
public class ApplicationController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    SentimentAnalysisService sentimentAnalysisService;

    private MonkeyLearn monkeyLearn;
    private LanguageServiceClient languageServiceClient;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView welcome(ModelAndView modelAndView) {

        //TODO: Make sure this is safe to remove
        //analyzeBigQueryFeedback();

        String text = "I'm very frustrated with all these changes. I used to make a lot of money and so that means " +
                "so did you. But now I never get any jobs. The people who do contact me aren't serious about the projects. " +
                "I think things were better before. Instant Match has made things especially hard. I don't really like " +
                "Instant Match, I wish you would change things back.";

        String greeting = "Welcome to CerePro!";

        modelAndView.setViewName("welcome");
        modelAndView.addObject("text", text);

        modelAndView.addObject("greeting", greeting);
        return modelAndView;
    }

    //TODO: Add feedback IDs to the query to enforce uniqueness? Maybe...
    private void analyzeBigQueryFeedback() {
        BigQuery bigQuery = BigQueryOptions.newBuilder().setProjectId("tt-dp-prod").build().getService();
        System.out.println("BigQuery is using project: " + bigQuery.getOptions().getProjectId());

        QueryJobConfiguration queryJobConfiguration =
                QueryJobConfiguration.newBuilder(
                        "SELECT\n" +
                                "pf.help_center_feedback_category,\n" +
                                "pf.help_center_feedback_subcategory,\n" +
                                "pii.feedback as feedback,\n" +
                                "pf.created_time as created_time\n" +
                                "FROM\n" +
                                "`ops.product_feedback` pf\n" +
                                "JOIN\n" +
                                "`ops_pii.product_feedback` pii\n" +
                                "ON\n" +
                                "pii.feedback_id = pf.feedback_id\n" +
                                "ORDER BY\n" +
                                "created_time DESC\n" +
                                "LIMIT\n" +
                                "100"
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
            TableResult tableResult = queryJob.getQueryResults();
            SentimentAnalysis sentimentAnalysis;
            for (FieldValueList row : tableResult.iterateAll()) {
                String feedback = row.get("feedback").getStringValue();
                if (feedback.length() > 100) {
                    System.out.printf("Sending feedback for analysis: %s%n", feedback);
                    sentimentAnalysis = new SentimentAnalysis(feedback);
                    analyzeSentiment(sentimentAnalysis);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/analyze/sentiment", method = RequestMethod.GET)
    public ModelAndView analyzeSentimentForm(ModelAndView modelAndView, SentimentAnalysis sentimentAnalysis) {
        modelAndView.setViewName("analyze_sentiment");
        modelAndView.addObject("sentiment_analysis", sentimentAnalysis);

        return modelAndView;
    }

    //TODO Change this to have no view, and redirect to a view with id in the path
    @RequestMapping(value = "analyze/sentiment", method = RequestMethod.POST)
    public ModelAndView displaySentimentAnalysis(ModelAndView modelAndView, SentimentAnalysis sentimentAnalysis) {

        modelAndView.setViewName("sentiment_analysis");
        analyzeSentiment(sentimentAnalysis);

        modelAndView.addObject("sentiment_analysis", sentimentAnalysis);
        return modelAndView;

    }

    @RequestMapping(value = "/sentiment", method = RequestMethod.GET)
    public ModelAndView sentimentIndex(ModelAndView modelAndView) {
        modelAndView.setViewName("sentiment_overview");
        List<SentimentAnalysis> analyses = sentimentAnalysisService.findAll();
        modelAndView.addObject("analyses", analyses);

        int totalAnalyses = sentimentAnalysisService.findAll().size();

        double avgSentiment = 0;
        for (SentimentAnalysis s : sentimentAnalysisService.findAll()) {
            avgSentiment += s.getSentimentScore();
        }

        modelAndView.addObject("average_sentiment", avgSentiment / totalAnalyses);
        modelAndView.addObject("total_analyses", totalAnalyses);

        return modelAndView;
    }

    //TODO Determine if this old version should be deleted
    //public boolean analyzeSentiment(SentimentAnalysis sentimentAnalysis) {
    //    System.out.println("In sentiment method");
//
    //    //monkeyLearn = new MonkeyLearn("3a1676bd86dc12eb834f90d160522b799ade3d18");
    //    monkeyLearn = new AppApplication().getMonkeyLearn();
    //    String monkeyLearnModuleId = "cl_Jx8qzYJh";
    //    String[] text = {sentimentAnalysis.getSourceText()};
//
    //    try {
    //        MonkeyLearnResponse monkeyLearnResponse = monkeyLearn.classifiers.classify(monkeyLearnModuleId, text, false);
//
    //        System.out.println(monkeyLearnResponse.jsonResult);
//
    //        JSONArray responseArray = monkeyLearnResponse.arrayResult;
    //        JSONArray array2 = (JSONArray) responseArray.get(0);
    //        JSONObject object = (JSONObject) array2.get(0);
//
    //        System.out.println("MonkeyLearn response: " + monkeyLearnResponse.arrayResult.get(0).toString());
//
    //        sentimentAnalysis.setAnalyzed(true);
    //        sentimentAnalysis.setSentiment(object.get("label").toString());
    //        //TODO FIX THIS
    //        sentimentAnalysisService.saveSentimentAnalysis(sentimentAnalysis);
    //        return true;
    //    } catch (MonkeyLearnException e) {
    //        logger.error("Problem with MonkeyLearn: " + e);
    //    }
    //    return false;
    //}

    //TODO: Move this to a utility class to be shared across the app
    private void analyzeSentiment(SentimentAnalysis sentimentAnalysis) {
        logger.info("In analyzeSentiment");
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

        sentimentAnalysisService.saveSentimentAnalysis(sentimentAnalysis);

        //TODO Figure out how to actually use a conditional to determine if analysis was successful
    }


}
