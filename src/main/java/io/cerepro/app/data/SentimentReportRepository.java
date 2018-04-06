package io.cerepro.app.data;

import io.cerepro.app.models.SentimentReport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentimentReportRepository extends CrudRepository<SentimentReport, Long> {
}
