package bit.exam.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Slf4j
@Repository
public class RedisRepositoryImpl implements IRedisRepository{

    private RedisKeyValueTemplate redisKeyValueTemplate;

    @Autowired
    public RedisRepositoryImpl(RedisKeyValueTemplate redisKeyValueTemplate) {
        this.redisKeyValueTemplate = redisKeyValueTemplate;

    }

    @Override
    public String getIpStackInfoByIp(String ip){

        log.info("Check if " + ip + " presented in cache");

        Optional<UserEvent> result = redisKeyValueTemplate.findById(ip, UserEvent.class);

        if (result.isPresent()){
            log.info("Ip " + ip + " found in cache.");
            return result.get().getUserDataAsJsonString();
        }

        return "";
    }

    @Override
    public void createIpStackInfo(String ip, UserEvent userEvent) {
        redisKeyValueTemplate.update(ip, userEvent);
    }
}
