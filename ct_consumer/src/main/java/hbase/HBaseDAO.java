package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import utils.ConnectionInstance;
import utils.HBaseUtil;
import utils.PropertiesUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName HBaseDAO
 * @MethodDesc: TODO HBaseDAO功能介绍
 * @Author Movle
 * @Date 6/27/20 8:36 下午
 * @Version 1.0
 * @Email movle_xjk@foxmail.com
 **/


public class HBaseDAO {
    private String namespace;
    private String tableName;
    private int regions;
    private static final Configuration conf;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
    private Connection connection;
    private HTable table;

    private List<Put> cacheList = new ArrayList<>();
    static {
        conf = HBaseConfiguration.create();
    }

    public HBaseDAO() {
        try {
            namespace = PropertiesUtil.getProperty("hbase.calllog.namespace");
            tableName = PropertiesUtil.getProperty("hbase.calllog.tablename");
            regions = Integer.valueOf(PropertiesUtil.getProperty("hbase.calllog.regions"));
            if (!HBaseUtil.isExistTable(conf, tableName)) {
                HBaseUtil.initNameSpace(conf, namespace);
                HBaseUtil.createTable(conf, tableName, regions, "f1", "f2");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ori: 17269452013,15542823911,2018-08-28 11:58:23,0800
     * ori：caller,callee.buildTime,duration
     *
     * @param ori
     */
    public void put(String ori) {
        try {
            if (cacheList.size() == 0) {
                connection = ConnectionInstance.getConnection(conf);
                table = (HTable)connection.getTable(TableName.valueOf(tableName));
                table.setAutoFlushTo(false);
                table.setWriteBufferSize(2 * 1024 * 1024);
            }


            String[] splitOri = ori.split(",");
            String caller = splitOri[0];
            String callee = splitOri[1];
            String buildTime = splitOri[2];
            String duration = splitOri[3];

            //这个变量用于插入点HBase中
            String buildTimeReplace = sdf2.format(sdf1.parse(buildTime));
            //作为Rowkey所需参数
            String buildTimeTs = String.valueOf(sdf1.parse(buildTime).getTime());
            //散列得分区号
            String regionCode = HBaseUtil.genRegionCode(caller, buildTime, regions);

            /**
             *  Rowkey
             */
            String rowkey = HBaseUtil.genRowkey(regionCode, caller, buildTimeReplace, callee, "1", duration);
            //向表中插数据
            Put put = new Put(Bytes.toBytes(rowkey));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("caller"), Bytes.toBytes(caller));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("callee"), Bytes.toBytes(callee));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("buildTimeReplace"), Bytes.toBytes(buildTimeReplace));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("buildTimeTS"), Bytes.toBytes(buildTimeTs));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("flag"), Bytes.toBytes("1"));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("duration"), Bytes.toBytes(duration));

            cacheList.add(put);
            if (cacheList.size() >= 30) {
                table.put(cacheList);
                table.flushCommits();

                table.close();
                cacheList.clear();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
