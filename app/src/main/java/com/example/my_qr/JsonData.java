package com.example.my_qr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class JsonData implements Serializable {//不知道會傳過來什麼物件 使用 Serializable
    private transient JSONObject object;

    JsonData(JSONObject object) {
        this.object = object;
    }

    protected <T> T mustGet(String name) throws JSONException {//can String int
        return (T) this.object.get(name);
    }

    protected <T extends Object> T defaultGet(String name, T def) {
        try {
            return (T) this.object.get(name);
        } catch (JSONException e) {
            return def;
        }
    }
}
