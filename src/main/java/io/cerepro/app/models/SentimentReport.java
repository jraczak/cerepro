package io.cerepro.app.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sentiment_reports")
public class SentimentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sentiment_report_id")
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;

    @Column(name = "name")
    private String name;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "avg_sentiment_score")
    private double avgSentimentScore;

    @Column(name = "avg_sentiment")
    private String avgSentiment;

    @Column(name = "feedback_category")
    private String feedbackCategory;

    @Column(name = "feedback_subcategory")
    private String feedbackSubcategory;

    @OneToMany(mappedBy = "sentimentReport", cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<SentimentAnalysis> sentimentAnalyses;

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

    public double getAvgSentimentScore() {
        return avgSentimentScore;
    }

    public String getDisplayAvgSentimentScore() {
        return String.format("%.2f", this.avgSentimentScore);
    }

    public void setAvgSentimentScore(double avgSentimentScore) {
        this.avgSentimentScore = avgSentimentScore;
    }

    public String getAvgSentiment() {
        return avgSentiment;
    }

    public void setAvgSentiment(String avgSentiment) {
        this.avgSentiment = avgSentiment;
    }

    public Set<SentimentAnalysis> getSentimentAnalyses() {
        return sentimentAnalyses;
    }

    public void setSentimentAnalyses(Set<SentimentAnalysis> sentimentAnalyses) {
        this.sentimentAnalyses = sentimentAnalyses;
    }

    public void addSentimentAnalysis(SentimentAnalysis sentimentAnalysis) {
        if (this.sentimentAnalyses == null) {
            this.sentimentAnalyses = new HashSet<>();
            this.sentimentAnalyses.add(sentimentAnalysis);
        } else this.sentimentAnalyses.add(sentimentAnalysis);
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

    public String getFeedbackCategory() {
        return feedbackCategory;
    }

    public void setFeedbackCategory(String feedbackCategory) {
        this.feedbackCategory = feedbackCategory;
    }

    public String getFeedbackSubcategory() {
        return feedbackSubcategory;
    }

    public void setFeedbackSubcategory(String feedbackSubcategory) {
        this.feedbackSubcategory = feedbackSubcategory;
    }

    public void convertAndSetSentiment(double sentimentScore) {
        String sentiment = "";
        if (sentimentScore == 0.0) sentiment = "neutral";
        else if (sentimentScore < 0.0) sentiment = "negative";
        else if (sentimentScore > 0.0) sentiment = "positive";
        this.avgSentiment = sentiment;
    }

    //TODO: Update this to return an array and label the indexes
    public int getSentimentCount(String sentiment) {
        int count = 0;
        for (SentimentAnalysis s : this.sentimentAnalyses) {
            if (s.getSentiment().equals(sentiment)) count++;
        }
        return count;
    }
}
