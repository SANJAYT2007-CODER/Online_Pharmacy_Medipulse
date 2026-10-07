package com.example.onlinepharmacy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "health_blogs")
public class HealthBlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blogId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String fullContent;

    private String author;
    private String publishedDate;
    private String category;
    private String imageUrl;

    public HealthBlog() {}

    public HealthBlog(String title, String shortDescription, String fullContent, String author,
                      String publishedDate, String category, String imageUrl) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullContent = fullContent;
        this.author = author;
        this.publishedDate = publishedDate;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public Long getBlogId() { return blogId; }
    public void setBlogId(Long blogId) { this.blogId = blogId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getFullContent() { return fullContent; }
    public void setFullContent(String fullContent) { this.fullContent = fullContent; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
