package io.cerepro.app.controllers;

import io.cerepro.app.models.SentimentReport;
import io.cerepro.app.services.SentimentReportService;
import io.cerepro.app.util.GoogleCloudUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class SentimentReportController {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    //TODO: Autowire Sentiment Analysis Report Service
    @Autowired
    GoogleCloudUtilities googleCloudUtilities;

    @Autowired
    SentimentReportService sentimentReportService;

    @RequestMapping(value = "/sentiment_reports", method = RequestMethod.GET)
    public ModelAndView indexSentimentReports(ModelAndView modelAndView) {

        modelAndView.setViewName("index_sentiment_reports");
        modelAndView.addObject("sentiment_reports", sentimentReportService.findAll());

        return modelAndView;
    }

    @RequestMapping(value = "/new_sentiment_report", method = RequestMethod.GET)
    public ModelAndView newSentimentReport(ModelAndView modelAndView, SentimentReport sentimentReport) {
        modelAndView.setViewName("new_sentiment_report");

        //TODO: Add the rest of the categories to the dropdown lists
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Paying for contacts");
        categories.add("Instant Match");
        categories.add("Quoting and payment");
        ArrayList<String> subcategories = new ArrayList<>();
        subcategories.add("Pricing");
        subcategories.add("Budget");
        subcategories.add("General");
        subcategories.add("General feedback");
        subcategories.add("How it works");
        subcategories.add("Process of receiving quotes");
        subcategories.add("Quoting flow");
        subcategories.add("Cost to quote");
        subcategories.add("Getting feedback from customers");

        modelAndView.addObject("categories", categories);
        modelAndView.addObject("subcategories", subcategories);

        //TODO: Categories need to be conditional and dependent

        modelAndView.addObject("sentiment_report", sentimentReport);
        return modelAndView;
    }

    @RequestMapping(value = "/sentiment_reports/{id}", method = RequestMethod.GET)
    public ModelAndView showSentimentReport(ModelAndView modelAndView, @PathVariable Long id) {
        modelAndView.setViewName("show_sentiment_report");

        SentimentReport sentimentReport = sentimentReportService.findById(id);
        modelAndView.addObject("sentiment_report", sentimentReport);

        //TODO find report by id and add as object to view

        return modelAndView;
    }

    //TODO: createSentimentReport
    @RequestMapping(value = "/sentiment_reports/create", method = RequestMethod.POST)
    public String createSentimentReport(ModelAndView modelAndView, SentimentReport sentimentReport) {

        logger.info("In createSentimentReport");
        logger.info("Sentiment report is : " + sentimentReport.toString());
        modelAndView.addObject("sentiment_report", sentimentReport);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //TODO: Process the sentiment
        sentimentReport = googleCloudUtilities.fetchBigQueryFeedbackData(sentimentReport,
                sentimentReport.getFeedbackCategory(),
                sentimentReport.getFeedbackSubcategory(),
                dateFormat.format(sentimentReport.getStartDate()),
                dateFormat.format(sentimentReport.getEndDate()),
                100);
        logger.info("Sentiment report is " + sentimentReport.toString());

        //TODO: Redirect to the report view

        return "redirect:/sentiment_reports/"+sentimentReport.getId();
    }
}
