import com.lambdaworks.redis.*;

import java.io.IOException;

public class RedisConnector {
    public static void main(String[] args) throws InterruptedException, IOException {
//        RedisClient redisClient = new RedisClient(RedisURI.create("redis://admin123@localhost:6379"));
//        RedisConnection<String, String> connection = redisClient.connect();

        // Try with resources, closes connection of resources by itself.
        try(RedisConnectionPoolV2 connPool = RedisConnectionPoolV2.getBasicConnectionPool(10,"localhost",6379,"admin123")) {

            RedisConnection<String,String> connection = connPool.getConnection();
            connection.set("key1","data");

            RedisConnection<String,String> connection2 = connPool.getConnection();
            String val = connection2.get("key1");
            System.out.println(val);

            connPool.releaseConnection(connection);
            connPool.releaseConnection(connection2);

        }

    }
}
