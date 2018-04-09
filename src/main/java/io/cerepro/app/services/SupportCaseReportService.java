package io.cerepro.app.services;

import io.cerepro.app.data.SupportCaseReportRepository;
import io.cerepro.app.models.SupportCaseReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportCaseReportService {

    private SupportCaseReportRepository supportCaseReportRepository;

    @Autowired
    public SupportCaseReportService(SupportCaseReportRepository repository) {
        this.supportCaseReportRepository = repository;
    }

    public void saveSupportCaseReport(SupportCaseReport supportCaseReport) {
        supportCaseReportRepository.save(supportCaseReport);
    }

    //TODO: Handle null return (no results)
    public SupportCaseReport findById(Long id) {
        return supportCaseReportRepository.findById(id).get();
    }
}
