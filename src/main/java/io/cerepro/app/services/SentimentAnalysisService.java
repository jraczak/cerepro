package io.cerepro.app.services;

import io.cerepro.app.data.SentimentAnalysisRepository;
import io.cerepro.app.models.SentimentAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SentimentAnalysisService {

    private SentimentAnalysisRepository sentimentAnalysisRepository;

    @Autowired
    public SentimentAnalysisService(SentimentAnalysisRepository repository) {
        this.sentimentAnalysisRepository = repository;
    }

    public void saveSentimentAnalysis(SentimentAnalysis sentimentAnalysis) {
        sentimentAnalysisRepository.save(sentimentAnalysis);
    }

    public List<SentimentAnalysis> findAll() {
        return sentimentAnalysisRepository.findAll();
    }

    public List<SentimentAnalysis> findAllBySentiment(String sentiment) {
        return sentimentAnalysisRepository.findAllBySentiment(sentiment);
    }
}
