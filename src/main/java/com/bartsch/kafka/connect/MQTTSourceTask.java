package com.bartsch.kafka.connect;

import com.bartsch.kafka.connect.config.MQTTSourceConnectorConfig;
import com.github.jcustenborder.kafka.connect.utils.data.SourceRecordDeque;
import com.github.jcustenborder.kafka.connect.utils.data.SourceRecordDequeBuilder;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.*;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Actual implementation of the Kafka Connect MQTT Source Task
 */
public class MQTTSourceTask extends SourceTask {

    private final Logger log = LoggerFactory.getLogger(MQTTSourceConnector.class);
    private MQTTSourceConnectorConfig config;
    private MQTTSourceConverter mqttSourceConverter;
    private SourceRecordDeque sourceRecordDeque;

    private IMqttAsyncClient mqttAsyncClient;

    public void start(Map<String, String> props) {
        config = new MQTTSourceConnectorConfig(props);
        mqttSourceConverter = new MQTTSourceConverter(config);
        sourceRecordDeque = SourceRecordDequeBuilder.of().batchSize(4096).emptyWaitMs(100).maximumCapacityTimeoutMs(60000).maximumCapacity(50000).build();
        try {
            mqttAsyncClient = new MqttAsyncClient(config.getString(MQTTSourceConnectorConfig.MQTT_BROKER), config.getString(MQTTSourceConnectorConfig.MQTT_CLIENT_ID), new MemoryPersistence());
            mqttAsyncClient.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                    throw new ConnectException(disconnectResponse.toString());
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    throw new ConnectException(exception);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    log.debug("Message " + message.toDebugString() + " arrived in connector from topic " + topic);
                    SourceRecord record = mqttSourceConverter.convert(topic, message);
                    log.debug("Converted record: " + record);
                    sourceRecordDeque.add(record);
                }

                @Override
                public void deliveryComplete(IMqttToken token) {

                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    log.info("Connected to MQTT Broker");
                    List<String> topicFilterList = config.getList(MQTTSourceConnectorConfig.MQTT_TOPIC_FILTER_LIST);
                    int qosLevel = config.getInt(MQTTSourceConnectorConfig.MQTT_QOS);

                    for (String topicFilter: topicFilterList) {
                        log.info("Subscribing to " + topicFilter + " with QOS " + qosLevel);
                        try {
                            IMqttToken subscribeToken = mqttAsyncClient.subscribe(new MqttSubscription[]{new MqttSubscription(topicFilter, qosLevel)});
                            subscribeToken.waitForCompletion(5000);
                        } catch (MqttException e) {
                            throw new ConnectException(e);
                        }
                        log.info("Subscribed to " + topicFilter + " with QOS " + qosLevel);
                    }
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {

                }
            });


            log.info("Connecting to MQTT Broker " + config.getString(MQTTSourceConnectorConfig.MQTT_BROKER));
            IMqttToken connectToken = connect(mqttAsyncClient);
            connectToken.waitForCompletion(5000);


        }
        catch (MqttException e) {
            throw new ConnectException(e);
        }
    }

    private IMqttToken connect(IMqttAsyncClient mqttAsyncClient) throws MqttException{
        MqttConnectionOptions connOpts = new MqttConnectionOptions();
        connOpts.setCleanStart(config.getBoolean(MQTTSourceConnectorConfig.MQTT_CLEAN_START));
        connOpts.setKeepAliveInterval(config.getInt(MQTTSourceConnectorConfig.MQTT_KEEP_ALIVE_INTERVAL));
        connOpts.setConnectionTimeout(config.getInt(MQTTSourceConnectorConfig.MQTT_CONNECTION_TIMEOUT));
        connOpts.setAutomaticReconnect(config.getBoolean(MQTTSourceConnectorConfig.MQTT_AUTOMATIC_RECONNECT));

        if (!config.getString(MQTTSourceConnectorConfig.MQTT_USERNAME).equals("") && !config.getPassword(MQTTSourceConnectorConfig.MQTT_PASSWORD).value().equals("")) {
            connOpts.setUserName(config.getString(MQTTSourceConnectorConfig.MQTT_USERNAME));
            connOpts.setPassword(config.getPassword(MQTTSourceConnectorConfig.MQTT_PASSWORD).value().getBytes(StandardCharsets.UTF_8));
        }

        log.info("MQTT Connection properties: " + connOpts);

        return mqttAsyncClient.connect(connOpts);

    }

    /**
     * Method is called periodically by the Connect framework
     *
     * @throws InterruptedException
     */
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> records = sourceRecordDeque.getBatch();
        log.trace("Records returning to poll(): " + records);
        return records;
    }

    @Override
    public synchronized void stop() {
        if (mqttAsyncClient.isConnected()) {
            try {
                log.debug("Disconnecting from MQTT Broker " + config.getString(MQTTSourceConnectorConfig.MQTT_BROKER));
                mqttAsyncClient.disconnect();
            } catch (MqttException mqttException) {
                log.error("Exception thrown while disconnecting client.", mqttException);
            }
        }
    }

    public String version() {
        return Version.getVersion();
    }

}
