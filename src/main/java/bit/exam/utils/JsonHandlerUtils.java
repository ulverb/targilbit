package bit.exam.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonHandlerUtils {

    private JsonHandlerUtils(){

    }

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T  convertJsonStringToObject(String jsonString, Class<T> clazz ){
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(jsonString,clazz);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
