package com.adamthody.kafka;

import com.github.jkutner.EnvKeyStore;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class HerokuKafkaConnectionHelper {

  private static final String KAFKA_SSL_SCHEME = "kafka+ssl";

  public static Properties getConfigProperties() {
    assertEnvVarPresent(HerokuKafkaEnvVars.KAFKA_URL, "KAFKA_URL must not be null");

    String kafkaUrl = System.getenv(HerokuKafkaEnvVars.KAFKA_URL);

    Properties properties = new Properties();
    properties.put(ConnectionConfigs.BOOTSTRAP_SERVERS_CONFIG, buildBootstrapServersConfig(kafkaUrl));

    if (shouldUseSSL(kafkaUrl)) {
      assertEnvVarPresent(HerokuKafkaEnvVars.KAFKA_CLIENT_CERT, "KAFKA_CLIENT_CERT must not be null");
      assertEnvVarPresent(HerokuKafkaEnvVars.KAFKA_CLIENT_CERT_KEY, "KAFKA_CLIENT_CERT_KEY must not be null");
      assertEnvVarPresent(HerokuKafkaEnvVars.KAFKA_TRUSTED_CERT, "KAFKA_TRUSTED_CERT must not be null");

      try {
        EnvKeyStore envTrustStore = EnvKeyStore.createWithRandomPassword(HerokuKafkaEnvVars.KAFKA_TRUSTED_CERT);
        EnvKeyStore envKeyStore = EnvKeyStore.createWithRandomPassword(HerokuKafkaEnvVars.KAFKA_CLIENT_CERT_KEY, HerokuKafkaEnvVars.KAFKA_CLIENT_CERT);

        File trustStore = envTrustStore.storeTemp();
        File keyStore = envKeyStore.storeTemp();

        properties.put(ConnectionConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(ConnectionConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, envTrustStore.type());
        properties.put(ConnectionConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStore.getAbsolutePath());
        properties.put(ConnectionConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, envTrustStore.password());
        properties.put(ConnectionConfigs.SSL_KEYSTORE_TYPE_CONFIG, envKeyStore.type());
        properties.put(ConnectionConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStore.getAbsolutePath());
        properties.put(ConnectionConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, envKeyStore.password());

      } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
        throw new RuntimeException("Unable to create Kafka key/trust stores", e);
      }
    } else {
      properties.put(ConnectionConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
    }

    return properties;
  }

  public static Map<String, Object> getConfigMap() {
    Properties properties = getConfigProperties();

    return properties.entrySet().stream().collect(
        Collectors.toMap(
            e -> (String) e.getKey(),
            Map.Entry::getValue
        ));
  }

  private static String buildBootstrapServersConfig(String kafkaUrl) {
    return Arrays.stream(kafkaUrl.split(","))
        .map(HerokuKafkaConnectionHelper::stripScheme)
        .collect(Collectors.joining(","));
  }

  private static String stripScheme(String url) {
    String urlSchemeRegex = "([a-z]*\\+?[a-z]*)://";
    return url.replaceFirst(urlSchemeRegex, "");
  }

  private static boolean shouldUseSSL(String kafkaUrl) {
    return kafkaUrl.contains(KAFKA_SSL_SCHEME);
  }

  private static void assertEnvVarPresent(String envVarKey, String errorMessage) {
    if (System.getenv(envVarKey) == null) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private class HerokuKafkaEnvVars {
    static final String KAFKA_CLIENT_CERT = "KAFKA_CLIENT_CERT";
    static final String KAFKA_CLIENT_CERT_KEY = "KAFKA_CLIENT_CERT_KEY";
    static final String KAFKA_TRUSTED_CERT = "KAFKA_TRUSTED_CERT";
    static final String KAFKA_URL = "KAFKA_URL";
  }

  class ConnectionConfigs {
    static final String SECURITY_PROTOCOL_CONFIG = "security.protocol";
    static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers";
    static final String SSL_TRUSTSTORE_TYPE_CONFIG = "ssl.truststore.type";
    static final String SSL_TRUSTSTORE_LOCATION_CONFIG = "ssl.truststore.location";
    static final String SSL_TRUSTSTORE_PASSWORD_CONFIG = "ssl.truststore.password";
    static final String SSL_KEYSTORE_TYPE_CONFIG = "ssl.keystore.type";
    static final String SSL_KEYSTORE_LOCATION_CONFIG = "ssl.keystore.location";
    static final String SSL_KEYSTORE_PASSWORD_CONFIG = "ssl.keystore.password";
  }
}