package lol.catgirl.file;

import com.google.gson.JsonElement;

public interface Serializable {
    JsonElement toJson();
    void fromJson(JsonElement json);
}
