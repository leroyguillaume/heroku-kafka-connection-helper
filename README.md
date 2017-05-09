# Heroku Kafka Connection Helper [![Build Status](https://travis-ci.org/thody/heroku-kafka-connection-helper.svg?branch=master)](https://travis-ci.org/thody/heroku-kafka-connection-helper) [![Maven Central](https://img.shields.io/maven-central/v/com.adamthody/heroku-kafka-connection-helper.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.adamthody%22%20AND%20a%3A%22heroku-kafka-connection-helper%22)

A Java library to help configure Kafka client connection Properties from Heroku Kafka environment variables.

## Usage

Include this library in your application as a Maven dependency:

```xml
<dependency>
  <groupId>com.adamthody</groupId>
  <artifactId>heroku-kafka-connection-helper</artifactId>
  <version>0.1.8</version>
</dependency>
```

### Configuring a Client

Calling `HerokuKafkaConnectionHelper.getConfigProperties()` will return a Properties object that has the appropriate connection
properties set, as per the environment variables available. 

Based on the URL scheme of the `KAFKA_URL` environment variable, the properties will either be for a simple plaintext 
connection, or it will configure an SSL connection, including the KeyStore and TrustStore.
 
Heroku Kafka uses SSL by default, but by setting `KAFKA_URL` locally, you can also test against a local cluster with a 
plaintext connection for local development without having to modify code.

```java
Properties properties = HerokuKafkaConnectionHelper.getConfigProperties();
... // Additional properties
KafkaConsumer<Integer, String> consumer = new KafkaConsumer<>(properties);
```

If you're using Spring's `ProducerFactory` or `ConsumerFactory`, you may prefer to get your connection configuration as 
a Map.
 
 ```java
 Map<String, Object> configMap = HerokuKafkaConnectionHelper.getConfigMap();
 ... // Additional properties
 ProducerFactory<Integer, String> producer = new DefaultKafkaProducerFactory<>(configMap);
```
