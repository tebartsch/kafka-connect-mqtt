package com.bartsch.kafka.connect;

import com.bartsch.kafka.connect.config.MQTTSinkConnectorConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of the Kafka Connect Sink connector
 */
public class MQTTSinkConnector extends SinkConnector {

    private static final Logger log = LoggerFactory.getLogger(MQTTSinkConnector.class);
    private MQTTSinkConnectorConfig mqttSinkConnectorConfig;
    private Map<String, String> configProps;

    @Override
    public void start(Map<String, String> map) {
        mqttSinkConnectorConfig = new MQTTSinkConnectorConfig(map);
        configProps = Collections.unmodifiableMap(map);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return MQTTSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>(1);
        taskConfigs.add(new HashMap<>(configProps));
        log.debug("Taskconfigs: " + taskConfigs);
        return taskConfigs;
    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return MQTTSinkConnectorConfig.configDef();
    }

    @Override
    public String version() {
        return Version.getVersion();
    }
}
