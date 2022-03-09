# kafka-connect-mqtt

Build the connector using maven
```
mvn clean install
```

## Start the test stack

Copy folder `/target/kafka-connect-mqtt-1.0.0-package/share/kafka-connect-mqtt` to plugin path
```
rm -r test/kafka/libs/plugins/kafka-connect-mqtt
cp -r target/kafka-connect-mqtt-1.0.0-package/kafka-connect-mqtt test/kafka/libs/plugins
```

Start the test stack
```
cd test
docker stack up -c docker-compose.yml kafka-connect-mqtt
```
and monitor with kafka-ui at http://127.0.0.1:8081.

### Verify Plugin Availability

Check if the kafka connect can find the plugins.
```
curl http://127.0.0.1:8083/connector-plugins
```

If following entries are present, the connectors are available.
```
{
    "class": "MQTTSourceConnector",
    "type": "source",
    "version": "1.0.0"
},
{
    "class": "MQTTSinkConnector",
    "type": "sink",
    "version": "1.0.0"
},
```
### Change plugin log level

To change the log level of the connector dynamically use
```
curl -s -X PUT -H "Content-Type:application/json" \
    http://localhost:8083/admin/loggers/com.bartsch \
    -d '{"level": "DEBUG"}' | jq '.'
```


## Configuring the Source connector

The MQTT Source connector subscribes to a Topic on a MQTT Broker and sends the messages to a Kafka topic.

Here is a basic configuration example:
```
curl -X POST \
  http://127.0.0.1:8083/connectors \
  -H 'Content-Type: application/json' \
  -d '{ "name": "mqtt-source-connector",
    "config":
    {
      "connector.class":"com.bartsch.kafka.connect.MQTTSourceConnector",
      "kafka.topic":"c.",
      "kafka.topic.append-mqtt-topic-name":true,
      "mqtt.topic-filter-list":"a/#,b/#",
      "mqtt.client-id":"kafka-forwarder",
      "mqtt.broker":"tcp://mosquitto:1883",
      "key.converter":"org.apache.kafka.connect.storage.StringConverter",
      "key.converter.schemas.enable":false,
      "value.converter":"org.apache.kafka.connect.storage.StringConverter",
      "value.converter.schemas.enable":false
    }
}'
```
### List of configuration options

| Name                                 | Required | Default  | Description                                                                                                                               |
|:-------------------------------------|----------|----------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `kafka.topic`                        | yes      |          | Kafka topic to write mqtt messages to.                                                                                                    |
| `kafka.topic.append-mqtt-topic-name` | no       | `false`  | I.e. convert mqtt topic `"a/b/c"` to `"a.b.c"` and append `".a.b.c"` to `kafka.topic`.                                               |
| `mqtt.topic-filter-list`             | yes      |          | Multiple topic filters possibly containing wildcards. Mqtt message from these topics are fowarded to kafka. (Example value: `a/#,b/#,c/d`) |
| `mqtt.client-id`                     | yes      |          | Mqtt client id.                                                                                |
| `mqtt.broker`                        | yes      |          | Mqtt broker location. (example value: `tcp://localhost:1883`)                                                                             |
| `mqtt.qos`                           | no       | `1`      | 0 – At most Once, 1 – At Least Once, 2 – Exactly Once                                                                                     |
| `mqtt.automatic-reconnect`           | no       | `true`   |                                                                                                                                           |
| `mqtt.keep-alive-interval`           | no       | `60`     | Controls the state after disconnecting the client from the broker                                                                         |
| `mqtt.clean-start`                   | no       | `true`   |                                                                                                                                           |
| `mqtt.connection-timeout`            | no       | `30`     |                                                                                                                                           |
| `mqtt.username`                      | no       | `""`     | Username to connect to MQTT broker                                                                                                        |
| `mqtt.password`                      | no       | `""`     | Password to connect to MQTT broker                                                                                                        |


## Configuring the Sink connector

The MQTT Sink Connector reads messages from a Kafka topic and publishes them to a MQTT topic.

Here is a basic configuration example:
```
curl -X POST \
  http://127.0.0.1:8083/connectors \
  -H 'Content-Type: application/json' \
  -d '{ "name": "mqtt-sink-connector",
    "config":
    {
      "connector.class":"com.bartsch.kafka.connect.MQTTSinkConnector",
      "topics.regex":"t.(.*)",
      "mqtt.topic":"s/",
      "mqtt.topic.append-kafka-topic-name":true,
      "mqtt.client-id": "kafka-receiver",
      "mqtt.broker":"tcp://mosquitto:1883",
      "key.converter":"org.apache.kafka.connect.storage.StringConverter",
      "key.converter.schemas.enable":false,
      "value.converter":"org.apache.kafka.connect.storage.StringConverter",
      "value.converter.schemas.enable":false
    }
}'
```

### Optional Configuration options

| Name                                  | Required | Default  | Description                                                                            |
|:--------------------------------------|----------|----------|----------------------------------------------------------------------------------------|
| `topics`                              | yes      |          | Kafka topics to consume. (example valiue: `topic1,topic2`)                             |
| `topics.regex`                        | (yes)    |          | Regular expression giving topics to consume. Only use `topics` OR `topics.regex`.      |
| `mqtt.topic`                          | yes      |          | Mqtt topic to publish messages to. (Example value: `a/b`)                              |
| `mqtt.topic.append-kafka-topic-name`  | no       | `false`  | I.e. convert kafka topic `"a.b.c"` to `"a/b/c"` and append `".a.b.c"` to `mqtt.topic`. |
| `mqtt.client-id`                      | yes      |          | Mqtt client id.                                                                        |
| `mqtt.broker`                         | yes      |          | Mqtt broker location. (example value: `tcp://localhost:1883`)                          |
| `mqtt.qos`                            | no       | `1`      | 0 – At most Once, 1 – At Least Once, 2 – Exactly Once                                  |
| `mqtt.automatic-reconnect`            | no       | `true`   |                                                                                        |
| `mqtt.keep-alive-interval`            | no       | `60`     | Controls the state after disconnecting the client from the broker                      |
| `mqtt.clean-start`                    | no       | `true`   |                                                                                        |
| `mqtt.connection-timeout`             | no       | `30`     |                                                                                        |
| `mqtt.username`                       | no       | `""`     | Username to connect to MQTT broker                                                     |
| `mqtt.password`                       | no       | `""`     | Password to connect to MQTT broker                                                     |


## Authors

* **Johan Vandevenne** - *Initial work* 
* **Tilmann Bartsch** - *Mqtt 5, Topic Mappings*
