package io.banditoz.mchelper.utils.paste;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Paste {
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("visibility")
    private String visibility;
    @JsonProperty("expires")
    private String expires;
    @JsonProperty("files")
    private List<File> files;

    public Paste(String message, String fileName) {
        this(message);
        getFiles().get(0).setName(fileName);
    }

    public Paste(String message) {
        this.setVisibility("unlisted");
        File file = new File();
        Content content = new Content();
        content.setFormat("text");
        content.setValue(message);
        file.setContent(content);

        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        this.setFiles(files);
    }

    public Paste(String message, int expireInHours) {
        this.setVisibility("unlisted");
        File file = new File();
        Content content = new Content();
        content.setFormat("text");
        content.setValue(message);
        file.setContent(content);

        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        this.setFiles(files);

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.add(Calendar.HOUR_OF_DAY, expireInHours);

        this.setExpires(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(c.getTime()));
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("visibility")
    public String getVisibility() {
        return visibility;
    }

    @JsonProperty("visibility")
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @JsonProperty("expires")
    public String getExpires() {
        return expires;
    }

    @JsonProperty("expires")
    public void setExpires(String expires) {
        this.expires = expires;
    }

    @JsonProperty("files")
    public List<File> getFiles() {
        return files;
    }

    @JsonProperty("files")
    public void setFiles(List<File> files) {
        this.files = files;
    }
}
