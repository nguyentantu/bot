package com.example.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Message {

    @JsonProperty("account")
    public Account account;

    @JsonProperty("body")
    public String body;
}
