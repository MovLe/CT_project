package utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;
/**
 * @ClassName ConnectionInstance
 * @MethodDesc: TODO ConnectionInstance功能介绍
 * @Author Movle
 * @Date 6/27/20 8:41 下午
 * @Version 1.0
 * @Email movle_xjk@foxmail.com
 **/


public class ConnectionInstance {
    private static Connection conn;

    public static synchronized Connection getConnection(Configuration configuration) {
        try {
            if (conn == null || conn.isClosed()) {
                conn = ConnectionFactory.createConnection(configuration);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
