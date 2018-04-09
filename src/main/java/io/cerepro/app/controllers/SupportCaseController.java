package io.cerepro.app.controllers;

import io.cerepro.app.models.SupportCaseReport;
import io.cerepro.app.services.SupportCaseReportService;
import io.cerepro.app.util.GoogleCloudUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

@Controller
public class SupportCaseController {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Autowired
    GoogleCloudUtilities googleCloudUtilities;

    @Autowired
    SupportCaseReportService supportCaseReportService;

    @RequestMapping(value = "/new_support_case_report", method = RequestMethod.GET)
    public ModelAndView newSupportCaseReport(ModelAndView modelAndView, SupportCaseReport supportCaseReport) {
        modelAndView.setViewName("new_support_case_report");

        //TODO: Add the rest of the support case categories
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Instant Match");

        ArrayList<String> subcategories = new ArrayList<>();
        subcategories.add("Budget");
        subcategories.add("Category availability");
        subcategories.add("How does IM work?");
        subcategories.add("Matching issues");
        subcategories.add("Other");
        subcategories.add("PPC pricing");
        subcategories.add("Quote sheets");

        modelAndView.addObject("categories", categories);
        modelAndView.addObject("subcategories", subcategories);

        //TODO: Make categories conditional selections somehow

        modelAndView.addObject(supportCaseReport);
        return modelAndView;
    }

    @RequestMapping(value = "/support_case_reports/{id}", method = RequestMethod.GET)
    public ModelAndView showSupportCaseReport(ModelAndView modelAndView, @PathVariable Long id) {
        modelAndView.setViewName("show_support_case_report");

        SupportCaseReport supportCaseReport = supportCaseReportService.findById(id);
        modelAndView.addObject("support_case_report", supportCaseReport);

        //TODO: Redirect somewhere useful if you don't find any results

        return modelAndView;
    }

    // POST action to create the support case report and redirect to the show view
    @RequestMapping(value = "/support_case_reports/create", method = RequestMethod.POST)
    public String createSupportCaseReport(SupportCaseReport supportCaseReport) {
        logger.info("In createSupportCaseReport");
        logger.info("Support case report is " + supportCaseReport.toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        supportCaseReport = googleCloudUtilities.fetchBigQuerySupportCaseData(
                supportCaseReport,
                supportCaseReport.getCaseCategory(),
                supportCaseReport.getCaseSubcategory(),
                dateFormat.format(supportCaseReport.getStartDate()),
                dateFormat.format(supportCaseReport.getEndDate()),
                500);
        logger.info("Support case report is " + supportCaseReport.toString());

        return "redirect:/support_case_reports/" + supportCaseReport.getId();
    }
}
