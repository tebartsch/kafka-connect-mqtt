package com.bartsch.kafka.connect;

import com.bartsch.kafka.connect.config.MQTTSourceConnectorConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.source.SourceRecord;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Converts a MQTT message to a Kafka message
 */
public class MQTTSourceConverter {

    private final MQTTSourceConnectorConfig mqttSourceConnectorConfig;

    private final Logger log = LoggerFactory.getLogger(MQTTSourceConverter.class);

    public MQTTSourceConverter(MQTTSourceConnectorConfig mqttSourceConnectorConfig) {
        this.mqttSourceConnectorConfig = mqttSourceConnectorConfig;
    }

    protected SourceRecord convert(String topic, MqttMessage mqttMessage) {
        log.debug("Converting MQTT message: " + mqttMessage.toDebugString());

        ConnectHeaders headers = new ConnectHeaders();
        headers.addInt("mqtt.message.id", mqttMessage.getId());
        headers.addInt("mqtt.message.qos", mqttMessage.getQos());
        headers.addBoolean("mqtt.message.duplicate", mqttMessage.isDuplicate());

        String kafka_topic;
        if (mqttSourceConnectorConfig.getBoolean(MQTTSourceConnectorConfig.KAFKA_TOPIC_NAME_APPEND_MQTT_TOPIC_NAME)) {
            String converted_mqtt_topic = topic.replace('/', '.');
            log.debug("Mqtt Topic Name is appended to kafka topic. MQTT topic: " + topic + ", Corresponding Kafka addition: " + converted_mqtt_topic);
            kafka_topic = mqttSourceConnectorConfig.getString(MQTTSourceConnectorConfig.KAFKA_TOPIC) + converted_mqtt_topic;
        } else {
            kafka_topic = mqttSourceConnectorConfig.getString(MQTTSourceConnectorConfig.KAFKA_TOPIC);
        }
        log.debug("Using Kafka Topic " + kafka_topic);

        SourceRecord sourceRecord = new SourceRecord(new HashMap<>(),
                new HashMap<>(),
                kafka_topic,
                (Integer) null,
                Schema.STRING_SCHEMA,
                topic,
                Schema.STRING_SCHEMA,
                new String(mqttMessage.getPayload()),
                System.currentTimeMillis(),
                headers);
        log.debug("Converted MQTT Message: " + sourceRecord);
        return sourceRecord;
    }
}
