package io.cerepro.app.data;

import io.cerepro.app.models.SentimentAnalysis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentimentAnalysisRepository extends CrudRepository<SentimentAnalysis, Long> {
    List<SentimentAnalysis> findAll();
    List<SentimentAnalysis> findAllBySentiment(String sentiment);
}
