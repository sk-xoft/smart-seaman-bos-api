package com.seaman.validate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import java.io.IOException;

public class StringOnlyDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        if("".equalsIgnoreCase(jsonParser.getValueAsString())) {
            throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, jsonParser.getValueAsString());
        } else if (!JsonToken.VALUE_STRING.equals(jsonParser.getCurrentToken())) {
            throw new BusinessException(AppStatus.ATTRIBUTE_IS_REQUIRE, jsonParser.getValueAsString());
        } else {
            return jsonParser.getValueAsString();
        }
    }
}
