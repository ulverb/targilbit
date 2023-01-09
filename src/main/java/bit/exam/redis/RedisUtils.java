package bit.exam.redis;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class RedisUtils {

    public static UserEvent buildUserEvent(String ip, String json){

        return UserEvent.builder()
                .ip(ip)
                .userDataAsJsonString(json)
                .userEventExpirationTime(setUserEventExpirationTime())
                .build();
    }

    private static long setUserEventExpirationTime(){
        return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3600);
    }
}
