package io.cerepro.app.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "monkey_learn_keywords")
public class MonkeyLearnKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monkey_learn_keyword_id")
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;

    @Column(name = "word")
    private String word;

    //TODO MonkeyLearnKeywordSamples needs to have database column size increased in prod
    /**
     * This is related to an issue where the samples were throwing errors about varchar)255) when being saved
     */
    @ElementCollection
    @Column(name = "samples", length = 2000)
    private List<String> samples;

    @Column(name = "count")
    private int count;

    @Column(name = "relevance")
    private double relevance;

    @ManyToOne
    @JoinColumn(name = "support_case_report_id")
    private SupportCaseReport supportCaseReport;

    public MonkeyLearnKeyword() {
        // obligatory empty default constructor
    }

    public MonkeyLearnKeyword(String word, int count, double relevance, List<String> samples) {
        this.word = word;
        this.count = count;
        this.relevance = relevance;
        this.samples = samples;
    }

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

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public SupportCaseReport getSupportCaseReport() {
        return supportCaseReport;
    }

    public void setSupportCaseReport(SupportCaseReport supportCaseReport) {
        this.supportCaseReport = supportCaseReport;
    }
}
