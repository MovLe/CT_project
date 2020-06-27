package kafka;


import hbase.HBaseDAO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import utils.PropertiesUtil;

import java.util.Arrays;

/**
 * @ClassName HBaseConsumer
 * @MethodDesc: kafka消费者
 * @Author Movle
 * @Date 6/27/20 8:35 下午
 * @Version 1.0
 * @Email movle_xjk@foxmail.com
 **/


public class HBaseConsumer {
    public static void main(String[] args) {
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(PropertiesUtil.properties);
        kafkaConsumer.subscribe(Arrays.asList(PropertiesUtil.getProperty("kafka.topics")));

        HBaseDAO hd = new HBaseDAO();
        while (true){
            ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
            for (ConsumerRecord<String, String> cr: records) {
                String ori = cr.value();
                System.out.println(ori);
                //17269452013,15542823911,2018-08-28 11:58:23,0800
                hd.put(ori);
            }
        }
    }
}
