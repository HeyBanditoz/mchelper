package io.banditoz.mchelper.urbandictionary;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UDDefinition {
    @JsonProperty("defid")
    private int defid;

    @JsonProperty("sound_urls")
    private List<String> soundUrls;

    @JsonProperty("thumbs_down")
    private int thumbsDown;

    @JsonProperty("author")
    private String author;

    @JsonProperty("written_on")
    private LocalDateTime writtenOn;

    @JsonProperty("definition")
    private String definition;

    @JsonProperty("permalink")
    private String permalink;

    @JsonProperty("thumbs_up")
    private int thumbsUp;

    @JsonProperty("word")
    private String word;

    @JsonProperty("current_vote")
    private String currentVote;

    @JsonProperty("example")
    private String example;

    public int getDefid() {
        return defid;
    }

    public List<String> getSoundUrls() {
        return soundUrls;
    }

    public int getThumbsDown() {
        return thumbsDown;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getWrittenOn() {
        return writtenOn;
    }

    public String getDefinition() {
        return definition;
    }

    public String getPermalink() {
        return permalink;
    }

    public int getThumbsUp() {
        return thumbsUp;
    }

    public String getWord() {
        return word;
    }

    public String getCurrentVote() {
        return currentVote;
    }

    public String getExample() {
        return example;
    }
}