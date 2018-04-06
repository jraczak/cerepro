package io.cerepro.app.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Date;

@Entity
@Table(name = "sentiment_analyses")
public class SentimentAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sentiment_analysis_id")
    private Long id;

    @Column(name = "source_text", length = 2000)
    private String sourceText;

    @Column(name = "sentiment")
    private String sentiment;

    @Column(name = "sentiment_score")
    private double sentimentScore;

    @Column(name = "sentiment_magnitude")
    private double sentimentMagnitude;

    @Column(name = "is_analyzed")
    private boolean isAnalyzed;

    @Column(name = "bigquery_feedback_id")
    private String bigQueryFeedbackId;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "sentiment_report_id")
    private SentimentReport sentimentReport;

    public SentimentAnalysis(String sourceText) {
        this.sourceText = sourceText;
    }

    public SentimentAnalysis(String feedbackId, String text) {
        this.bigQueryFeedbackId = feedbackId;
        this.sourceText = text;
    }

    // Default obligatory empty constructor
    public SentimentAnalysis() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public boolean isAnalyzed() {
        return isAnalyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        isAnalyzed = analyzed;
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

    public double getSentimentScore() {

        return sentimentScore;
    }

    public String getDisplaySentimentScore() {
        DecimalFormat format = new DecimalFormat("####0.00");
        return format.format(sentimentScore);
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String convertSentimentScore(double sentimentScore) {
        String sentiment = "";
        if (sentimentScore == 0.0) sentiment = "neutral";
        else if (sentimentScore < 0.0) sentiment = "negative";
        else if (sentimentScore > 0.0) sentiment = "positive";
        return sentiment;
    }

    public double getSentimentMagnitude() {
        return sentimentMagnitude;
    }

    public String getDisplaySentimentMagnitude() {
        DecimalFormat format = new DecimalFormat("####0.00");
        return format.format(sentimentMagnitude);
    }

    public void setSentimentMagnitude(double sentimentMagnitude) {
        this.sentimentMagnitude = sentimentMagnitude;
    }

    public SentimentReport getSentimentReport() {
        return sentimentReport;
    }

    public void setSentimentReport(SentimentReport sentimentReport) {
        this.sentimentReport = sentimentReport;
    }

    public String getBigQueryFeedbackId() {
        return bigQueryFeedbackId;
    }

    public void setBigQueryFeedbackId(String bigQueryFeedbackId) {
        this.bigQueryFeedbackId = bigQueryFeedbackId;
    }
}
