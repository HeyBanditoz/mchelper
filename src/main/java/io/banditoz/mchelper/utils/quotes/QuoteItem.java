package io.banditoz.mchelper.utils.quotes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties("tags")
public class QuoteItem {
    @JsonProperty("date")
    private String date;

    @JsonProperty("quote")
    private String quote;

    @JsonProperty("author")
    private String author;

    @JsonProperty("background")
    private String background;

    @JsonProperty("length")
    private String length;

    @JsonProperty("language")
    private String language;

    @JsonProperty("id")
    private String id;

    @JsonProperty("category")
    private String category;

    @JsonProperty("permalink")
    private String permalink;

    @JsonProperty("title")
    private String title;

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getQuote() {
        return quote;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getBackground() {
        return background;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getLength() {
        return length;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}