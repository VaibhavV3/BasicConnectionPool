import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisConnectionPoolV2 implements Closeable {
    private BlockingQueue<RedisConnection> connectionPool;
    private int capacity;
    private AtomicInteger connectionCount;

    private static RedisConnectionPoolV2 instance = null;

    private RedisConnectionPoolV2(int capacity,String host, int port, String password){
        this.capacity = capacity;
        connectionPool = new ArrayBlockingQueue<RedisConnection>(capacity);
        connectionCount = new AtomicInteger();
        createConnection(host, port, password);
    }

    private void createConnection(String host, int port, String password) {
        String uri = "redis://"+password+"@"+host+":"+port;

        for(int i=0;i<capacity;i++){
            RedisClient redisClient = new RedisClient(RedisURI.create(uri));
            RedisConnection<String, String> connection = redisClient.connect();
            connectionPool.add(connection);
        }

    }

    public RedisConnection getConnection() throws InterruptedException {
        RedisConnection connection = connectionPool.poll(10, TimeUnit.SECONDS);
        connectionCount.decrementAndGet();
        return connection;
    }

    public boolean releaseConnection(RedisConnection connection){
        connectionPool.add(connection);
        connectionCount.incrementAndGet();
        return true;
    }

    public static synchronized RedisConnectionPoolV2 getBasicConnectionPool(int capacity,String host, int port, String password){
        if(instance == null){
            instance = new RedisConnectionPoolV2(capacity,host,port,password);
        }
        return instance;
    }

    public void close() throws IOException {
        while(!connectionPool.isEmpty()){
            RedisConnection connection = connectionPool.poll();
            connection.close();
            System.out.println("Connection Closed!!!");
        }
        connectionPool.clear();
    }
}
