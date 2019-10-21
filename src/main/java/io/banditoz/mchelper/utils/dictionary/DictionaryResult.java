package io.banditoz.mchelper.utils.dictionary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryResult {
    @JsonProperty("definitions")
    private List<Definition> definitions = null;
    @JsonProperty("word")
    private String word;
    @JsonProperty("pronunciation")
    private Object pronunciation;

    @JsonProperty("definitions")
    public List<Definition> getDefinitions() {
        return definitions;
    }

    @JsonProperty("definitions")
    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    @JsonProperty("word")
    public String getWord() {
        return word;
    }

    @JsonProperty("word")
    public void setWord(String word) {
        this.word = word;
    }

    @JsonProperty("pronunciation")
    public Object getPronunciation() {
        return pronunciation;
    }

    @JsonProperty("pronunciation")
    public void setPronunciation(Object pronunciation) {
        this.pronunciation = pronunciation;
    }
}

