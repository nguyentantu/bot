package com.example.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BotStatus {

    @JsonProperty("mention_num")
    public int mentionRoomNum;
}
