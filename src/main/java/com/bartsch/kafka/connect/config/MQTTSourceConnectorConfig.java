package com.bartsch.kafka.connect.config;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class MQTTSourceConnectorConfig extends AbstractConfig {

    // Kafka Configuration

    public static final String KAFKA_TOPIC = "kafka.topic";
    public static final String KAFKA_TOPIC_DOC = "kafka topic to publish to";

    public static final String KAFKA_TOPIC_NAME_APPEND_MQTT_TOPIC_NAME = "kafka.topic.append-mqtt-topic-name";
    public static final String KAFKA_TOPIC_NAME_APPEND_MQTT_TOPIC_NAME_DOC = "append translated mqtt Topic Name to kafka.topic, default is false";

    // MQTT Configuration

    public static final String MQTT_BROKER = "mqtt.broker";
    public static final String MQTT_BROKER_DOC = "Host and port of the MQTT broker, eg: tcp://192.168.1.1:1883";

    public static final String MQTT_CLIENT_ID = "mqtt.client-id";
    public static final String MQTT_CLIENT_ID_DOC = "mqtt client-id ";

    public static final String MQTT_TOPIC_FILTER_LIST = "mqtt.topic-filter-list";
    public static final String MQTT_TOPIC_FILTER_LIST_DOC = "List of topic names to subscribe to";

    public static final String MQTT_QOS = "mqtt.qos";
    public static final String MQTT_QOS_DOC = "Quality of service MQTT messaging, default is 1 (at least once)";

    public static final String MQTT_AUTOMATIC_RECONNECT = "mqtt.automatic-reconnect";
    public static final String MQTT_AUTOMATIC_RECONNECT_DOC = "set Automatic reconnect, default true";

    public static final String MQTT_KEEP_ALIVE_INTERVAL = "mqtt.keep-alive-interval";
    public static final String MQTT_KEEP_ALIVE_INTERVAL_DOC = "set the keepalive interval, default is 60 seconds";

    public static final String MQTT_CLEAN_START = "mqtt.clean-start";
    public static final String MQTT_CLEAN_START_DOC = "Sets whether the client and server should remember state across restarts and reconnects, default is true";

    public static final String MQTT_CONNECTION_TIMEOUT = "mqtt.connection-timeout";
    public static final String MQTT_CONNECTION_TIMEOUT_DOC = "Sets the connection timeout, default is 30";

    public static final String MQTT_USERNAME = "mqtt.username";
    public static final String MQTT_USERNAME_DOC = "Sets the username for the MQTT connection timeout, default is \"\"";

    public static final String MQTT_PASSWORD = "mqtt.password";
    public static final String MQTT_PASSWORD_DOC = "Sets the password for the MQTT connection timeout, default is \"\"";


    public MQTTSourceConnectorConfig(Map<?, ?> originals) {
        super(configDef(), originals);
    }

    public static ConfigDef configDef() {
        return new ConfigDef()
                .define(KAFKA_TOPIC,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        KAFKA_TOPIC_DOC)
                .define(KAFKA_TOPIC_NAME_APPEND_MQTT_TOPIC_NAME,
                        ConfigDef.Type.BOOLEAN,
                        false,
                        ConfigDef.Importance.HIGH,
                        KAFKA_TOPIC_NAME_APPEND_MQTT_TOPIC_NAME_DOC)
                .define(MQTT_BROKER,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        MQTT_BROKER_DOC)
                .define(MQTT_CLIENT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        MQTT_CLIENT_ID_DOC)
                .define(MQTT_TOPIC_FILTER_LIST,
                        ConfigDef.Type.LIST,
                        ConfigDef.Importance.HIGH,
                        MQTT_TOPIC_FILTER_LIST_DOC)
                .define(MQTT_QOS,
                        ConfigDef.Type.INT,
                        1,
                        ConfigDef.Range.between(1, 3),
                        ConfigDef.Importance.MEDIUM,
                        MQTT_QOS_DOC)
                .define(MQTT_AUTOMATIC_RECONNECT,
                        ConfigDef.Type.BOOLEAN,
                        true,
                        ConfigDef.Importance.MEDIUM,
                        MQTT_AUTOMATIC_RECONNECT_DOC)
                .define(MQTT_KEEP_ALIVE_INTERVAL,
                        ConfigDef.Type.INT,
                        60,
                        ConfigDef.Importance.LOW,
                        MQTT_KEEP_ALIVE_INTERVAL_DOC)
                .define(MQTT_CLEAN_START,
                        ConfigDef.Type.BOOLEAN,
                        true,
                        ConfigDef.Importance.LOW,
                        MQTT_CLEAN_START_DOC)
                .define(MQTT_CONNECTION_TIMEOUT,
                        ConfigDef.Type.INT,
                        30,
                        ConfigDef.Importance.LOW,
                        MQTT_CONNECTION_TIMEOUT_DOC)
                .define(MQTT_USERNAME,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        MQTT_USERNAME_DOC)
                .define(MQTT_PASSWORD,
                        ConfigDef.Type.PASSWORD,
                        "",
                        ConfigDef.Importance.LOW,
                        MQTT_PASSWORD_DOC)
                ;
    }
}
