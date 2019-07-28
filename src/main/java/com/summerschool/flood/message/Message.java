package com.summerschool.flood.message;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private MessageType type;
    private Map<String,Object> payload = new HashMap<>();

    @JsonAnySetter
    void setPayload(String key, String value) {
        payload.put(key, value);
    }

}