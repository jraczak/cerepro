package io.cerepro.app.data;

import io.cerepro.app.models.SupportCaseReport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportCaseReportRepository extends CrudRepository<SupportCaseReport, Long> {

}
