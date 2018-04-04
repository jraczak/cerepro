package io.cerepro.app.controllers;

import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;
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

import javax.persistence.EntityManager;


@Controller
public class ApplicationController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    SentimentAnalysisService sentimentAnalysisService;

    private MonkeyLearn monkeyLearn;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView welcome(ModelAndView modelAndView) {

        String greeting = "Welcome to CerePro!";

        String text = "I'm very frustrated with all these changes. I used to make a lot of money and so that means " +
                "so did you. But now I never get any jobs. The people who do contact me aren't serious about the projects. " +
                "I think things were better before.";

        modelAndView.setViewName("welcome");
        //modelAndView.addObject("sentiment", analyzeSentiment());
        modelAndView.addObject("text", text);

        modelAndView.addObject("greeting", greeting);
        return modelAndView;
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

        logger.info(sentimentAnalysis.getSourceText());
        analyzeSentiment(sentimentAnalysis);

        modelAndView.addObject("sentiment_analysis", sentimentAnalysis);
        return modelAndView;
    }

    public boolean analyzeSentiment(SentimentAnalysis sentimentAnalysis) {
        System.out.println("In sentiment method");

        monkeyLearn = new MonkeyLearn("3a1676bd86dc12eb834f90d160522b799ade3d18");
        String monkeyLearnModuleId = "cl_Jx8qzYJh";
        String[] text = {sentimentAnalysis.getSourceText()};

        try {
            MonkeyLearnResponse monkeyLearnResponse = monkeyLearn.classifiers.classify(monkeyLearnModuleId, text, false);

            System.out.println(monkeyLearnResponse.jsonResult);

            JSONArray responseArray = monkeyLearnResponse.arrayResult;
            JSONArray array2 = (JSONArray) responseArray.get(0);
            JSONObject object = (JSONObject) array2.get(0);

            System.out.println("MonkeyLearn response: " + monkeyLearnResponse.arrayResult.get(0).toString());

            sentimentAnalysis.setAnalyzed(true);
            sentimentAnalysis.setSentiment(object.get("label").toString());
            //TODO FIX THIS
            sentimentAnalysisService.saveSentimentAnalysis(sentimentAnalysis);
            return true;
        } catch (MonkeyLearnException e) {
            logger.error("Problem with MonkeyLearn: " + e);
        }
        return false;
    }


}
