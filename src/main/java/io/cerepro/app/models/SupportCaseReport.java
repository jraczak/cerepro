package io.cerepro.app.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "support_case_reports")
public class SupportCaseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "support_case_report_id")
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;

    @Column(name = "name")
    private String name;

    @Column(name = "notes")
    private String notes;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "case_category")
    private String caseCategory;

    @Column(name = "case_subcategory")
    private String caseSubcategory;

    @ElementCollection
    @Column(name = "entities")
    private List<com.google.cloud.language.v1.Entity> entities;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getStartDate() {
        return startDate;
    }

    //TODO: Deal with the parse exception
    public void setStartDate(String startDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.startDate = dateFormat.parse(startDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.endDate = dateFormat.parse(endDate);
    }

    public String getCaseCategory() {
        return caseCategory;
    }

    public void setCaseCategory(String caseCategory) {
        this.caseCategory = caseCategory;
    }

    public String getCaseSubcategory() {
        return caseSubcategory;
    }

    public void setCaseSubcategory(String caseSubcategory) {
        this.caseSubcategory = caseSubcategory;
    }

    public List<com.google.cloud.language.v1.Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<com.google.cloud.language.v1.Entity> entities) {
        this.entities = entities;
    }

    public void addEntity(com.google.cloud.language.v1.Entity entity) {
        if (this.entities == null) {
            this.entities = new ArrayList<>();
            this.entities.add(entity);
        } else this.entities.add(entity);
    }
}
