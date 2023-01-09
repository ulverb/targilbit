package bit.exam.redis;

public interface IRedisRepository {

    String getIpStackInfoByIp(String ip);

    void createIpStackInfo(String ip, UserEvent userEvent);
}
