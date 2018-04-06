package io.cerepro.app.services;

import io.cerepro.app.data.SentimentReportRepository;
import io.cerepro.app.models.SentimentReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SentimentReportService {

    private SentimentReportRepository sentimentReportRepository;

    @Autowired
    public SentimentReportService(SentimentReportRepository repository) {
        this.sentimentReportRepository = repository;
    }

    public void saveSentimentReport(SentimentReport sentimentReport) {
        sentimentReportRepository.save(sentimentReport);
    }

    //TODO Handle nulls
    public SentimentReport findById(Long id) {
        return sentimentReportRepository.findById(id).get();
    }
}
