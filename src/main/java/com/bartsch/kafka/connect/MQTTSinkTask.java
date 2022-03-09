package com.bartsch.kafka.connect;

import com.bartsch.kafka.connect.config.MQTTSinkConnectorConfig;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of the Kafka Connect Sink task
 */
public class MQTTSinkTask extends SinkTask {

    private final Logger log = LoggerFactory.getLogger(MQTTSinkTask.class);
    private MQTTSinkConnectorConfig config;
    private MQTTSinkConverter mqttSinkConverter;

    private IMqttClient mqttClient;

    @Override
    public String version() {
        return Version.getVersion();
    }

    @Override
    public void start(Map<String, String> map) {
        config = new MQTTSinkConnectorConfig(map);
        mqttSinkConverter = new MQTTSinkConverter(config);
        try {
            mqttClient = new MqttClient(config.getString(MQTTSinkConnectorConfig.MQTT_BROKER), config.getString(MQTTSinkConnectorConfig.MQTT_CLIENT_ID), new MemoryPersistence());

            log.info("Connecting to MQTT Broker " + config.getString(MQTTSinkConnectorConfig.MQTT_BROKER));
            connect(mqttClient);
            log.info("Connected to MQTT Broker. This connector publishes to the " + this.config.getString(MQTTSinkConnectorConfig.MQTT_TOPIC) + " topic");

        }
        catch (MqttException e) {
            throw new ConnectException(e);
        }
    }

    private void connect(IMqttClient mqttClient) throws MqttException{
        MqttConnectionOptions connOpts = new MqttConnectionOptions();
        connOpts.setCleanStart(config.getBoolean(MQTTSinkConnectorConfig.MQTT_CLEAN_START));
        connOpts.setKeepAliveInterval(config.getInt(MQTTSinkConnectorConfig.MQTT_KEEP_ALIVE_INTERVAL));
        connOpts.setConnectionTimeout(config.getInt(MQTTSinkConnectorConfig.MQTT_CONNECTION_TIMEOUT));
        connOpts.setAutomaticReconnect(config.getBoolean(MQTTSinkConnectorConfig.MQTT_AUTOMATIC_RECONNECT));

        if (!config.getString(MQTTSinkConnectorConfig.MQTT_USERNAME).equals("") && !config.getPassword(MQTTSinkConnectorConfig.MQTT_PASSWORD).value().equals("")) {
            connOpts.setUserName(config.getString(MQTTSinkConnectorConfig.MQTT_USERNAME));
            connOpts.setPassword(config.getPassword(MQTTSinkConnectorConfig.MQTT_PASSWORD).value().getBytes(StandardCharsets.UTF_8));
        }

        log.debug("MQTT Connection properties: " + connOpts);

        mqttClient.connect(connOpts);
    }

    @Override
    public void put(Collection<SinkRecord> collection) {
        try {
            for (SinkRecord sinkRecord : collection) {
                log.debug("Received message with offset " + sinkRecord.kafkaOffset());
                MqttMessage mqttMessage = mqttSinkConverter.convert(sinkRecord);

                String mqtt_topic;
                if (config.getBoolean(MQTTSinkConnectorConfig.MQTT_TOPIC_APPEND_KAFKA_TOPIC_NAME)) {
                    String kafka_topic = sinkRecord.topic();
                    String converted_kafka_topic = kafka_topic.replace('.', '/');
                    log.debug("Kafka Topic Name is appended to mqtt topic. Kafka topic: " + kafka_topic + ", Corresponding MQTT addition: " + converted_kafka_topic);
                    mqtt_topic = config.getString(MQTTSinkConnectorConfig.MQTT_TOPIC) + converted_kafka_topic;
                } else {
                    mqtt_topic = config.getString(MQTTSinkConnectorConfig.MQTT_TOPIC);
                }

                if (!mqttClient.isConnected()) mqttClient.connect();
                log.debug("Publishing message to topic " + mqtt_topic + " with payload " + new String(mqttMessage.getPayload()));
                mqttClient.publish(mqtt_topic, mqttMessage);
            }
        } catch (MqttException e) {
            throw new ConnectException(e);
        }

    }

    @Override
    public void stop() {
        if (mqttClient.isConnected()) {
            try {
                log.debug("Disconnecting from MQTT Broker " + config.getString(MQTTSinkConnectorConfig.MQTT_BROKER));
                mqttClient.disconnect();
            } catch (MqttException mqttException) {
                log.error("Exception thrown while disconnecting client.", mqttException);
            }
        }
    }
}
