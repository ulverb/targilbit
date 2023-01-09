package bit.exam.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@RedisHash("UserEvent")
public class UserEvent implements Serializable {

    @Id
    private String ip;
    private String userDataAsJsonString;

    @TimeToLive
    private long userEventExpirationTime; // ttl of transaction presence timer in the cache, represent in timestamp


}

