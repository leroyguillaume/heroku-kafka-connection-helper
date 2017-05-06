package com.adamthody.kafka;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.util.Map;
import java.util.Properties;

import static com.adamthody.kafka.HerokuKafkaConnectionHelper.ConnectionConfigs.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

public class HerokuKafkaConnectionHelperTest {

  private static final String TEST_CERT = "-----BEGIN CERTIFICATE-----\n" +
      "MIIC0jCCAjugAwIBAgIJAPyNFqzyo6CCMA0GCSqGSIb3DQEBBQUAMFAxCzAJBgNV\n" +
      "BAYTAmNhMRAwDgYDVQQIEwdPbnRhcmlvMRAwDgYDVQQHEwdUb3JvbnRvMQ8wDQYD\n" +
      "VQQKEwZIZXJva3UxDDAKBgNVBAsTA0RvRDAeFw0xNzA0MjExMzEzNDZaFw0xODA0\n" +
      "MjExMzEzNDZaMFAxCzAJBgNVBAYTAmNhMRAwDgYDVQQIEwdPbnRhcmlvMRAwDgYD\n" +
      "VQQHEwdUb3JvbnRvMQ8wDQYDVQQKEwZIZXJva3UxDDAKBgNVBAsTA0RvRDCBnzAN\n" +
      "BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA1CDTx2fluTJEiCBlVh/vsj037iqi6A+E\n" +
      "MAY4qW2CjR4jNvSiZh15GaHx6hVwZSigkSrWnNpOG2D3ptcHbgbzqH2D18qmV4w7\n" +
      "5owKEBOEzFvIZf8CBligXEapuRWEfPf6QldEYyiSiauOQ6IWMRTN4sWZdHDhaZN2\n" +
      "ySmyH3TAqvcCAwEAAaOBszCBsDAdBgNVHQ4EFgQUt6Uhbk1iKIPGRRpk8UEnByU5\n" +
      "bLswgYAGA1UdIwR5MHeAFLelIW5NYiiDxkUaZPFBJwclOWy7oVSkUjBQMQswCQYD\n" +
      "VQQGEwJjYTEQMA4GA1UECBMHT250YXJpbzEQMA4GA1UEBxMHVG9yb250bzEPMA0G\n" +
      "A1UEChMGSGVyb2t1MQwwCgYDVQQLEwNEb0SCCQD8jRas8qOggjAMBgNVHRMEBTAD\n" +
      "AQH/MA0GCSqGSIb3DQEBBQUAA4GBAIdRJqlKlcwSQIZgJwUEz8hPzIOS2JQFFl+3\n" +
      "MCjF0gLL74YBUFifrDOPnbT7/VqUu/OA7Z0/aK2bSHtWipGYYryNgNrgDmAkhj3D\n" +
      "XxHqrldV6qevjeYEoCVs3d2lj4kMGvlLzkysVIBCfxOKfTqVAZIylXSfjbPlwqCZ\n" +
      "t2zz/ol9\n" +
      "-----END CERTIFICATE-----\n";

  private static final String TEST_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
      "MIICWwIBAAKBgQDUINPHZ+W5MkSIIGVWH++yPTfuKqLoD4QwBjipbYKNHiM29KJm\n" +
      "HXkZofHqFXBlKKCRKtac2k4bYPem1wduBvOofYPXyqZXjDvmjAoQE4TMW8hl/wIG\n" +
      "WKBcRqm5FYR89/pCV0RjKJKJq45DohYxFM3ixZl0cOFpk3bJKbIfdMCq9wIDAQAB\n" +
      "AoGAGJpKFbmOIkGbQzKGrkt78G87OmtFg6axDSTbk4L4/kb9jJUo9LD8Z6Wpuh2M\n" +
      "cJ8aqFuQkpabMn+3mWkdGnzB65WuNgdIe6gZedwXaBWWTroGyzi/+fvUFyjVJT7E\n" +
      "1gUb27N3diQ4yZRrJzBmv9d8dPnPnCEPM50SObTYR8fXiTECQQD0B3cVOICSgHte\n" +
      "JN3Bg1K2HIrwb33jG1bEyc0kQEFs6eUTucdN389PWiubmk3+N2GZK3Bs3F0sJP94\n" +
      "YWW1jbNpAkEA3oi/gCoCvVb8qCTYbeEhzI3OEme8KgHONSzx1tvdtpURrOuEy1ey\n" +
      "vdmkp0X1IfYsW91lkEBNXNm6GLZzfJp/XwJAUK53LMj1mXppT4MY3nYwzo05Uq1K\n" +
      "DeBoG8As1yN3fi8G0jLGNnUc8bt6V4P47WgaGK5ICXYCSCojXQW2vwQOoQJAYxiC\n" +
      "LpCGp8oUuMuvlT6rzmtXWNrY79vd99AL4aIHGUdIl7hJakOjVOeWaua8QmNdXYs5\n" +
      "rwLzcGWEeXzF8LbcAQJAa3jJwPGRYM9afLmi+SFg7dOKazWaRsrWkSpxUTdsBhq+\n" +
      "S7aMEyqB/WypKIvTb6x5NCkqxm1F75ZhtR8ue+YYPA==\n" +
      "-----END RSA PRIVATE KEY-----\n";

  @Rule
  public final EnvironmentVariables envVars = new EnvironmentVariables();

  @Test
  public void sslConfiguration() throws Exception {
    envVars.set("KAFKA_URL", "kafka+ssl://1.1.1.1:1,kafka+ssl://2.2.2.2:2,kafka+ssl://3.3.3.3:3");
    envVars.set("KAFKA_TRUSTED_CERT", TEST_CERT);
    envVars.set("KAFKA_CLIENT_CERT", TEST_CERT);
    envVars.set("KAFKA_CLIENT_CERT_KEY", TEST_KEY);

    Properties props = HerokuKafkaConnectionHelper.getConfigProperties();

    assertThat(props.getProperty(SECURITY_PROTOCOL_CONFIG), equalTo("SSL"));
    assertThat(props.getProperty(BOOTSTRAP_SERVERS_CONFIG), equalTo("1.1.1.1:1,2.2.2.2:2,3.3.3.3:3"));
    assertNotNull(props.getProperty(SSL_TRUSTSTORE_TYPE_CONFIG));
    assertNotNull(props.getProperty(SSL_TRUSTSTORE_LOCATION_CONFIG));
    assertNotNull(props.getProperty(SSL_TRUSTSTORE_PASSWORD_CONFIG));
    assertNotNull(props.getProperty(SSL_KEYSTORE_TYPE_CONFIG));
    assertNotNull(props.getProperty(SSL_KEYSTORE_LOCATION_CONFIG));
    assertNotNull(props.getProperty(SSL_KEYSTORE_PASSWORD_CONFIG));
  }

  @Test
  public void missingKafkaUrlEnvVar() throws Exception {
    try {
      HerokuKafkaConnectionHelper.getConfigProperties();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage(), containsString("KAFKA_URL"));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingSSLEnvironmentVariables() throws Exception {
    envVars.set("KAFKA_URL", "kafka+ssl://1.1.1.1:1,kafka+ssl://2.2.2.2:2,kafka+ssl://3.3.3.3:3");
    HerokuKafkaConnectionHelper.getConfigProperties();
  }

  @Test
  public void plaintextConfiguration() throws Exception {
    envVars.set("KAFKA_URL", "kafka://1.1.1.1:1,kafka://2.2.2.2:2,kafka://3.3.3.3:3");

    Properties props = HerokuKafkaConnectionHelper.getConfigProperties();

    assertThat(props.getProperty(SECURITY_PROTOCOL_CONFIG), equalTo("PLAINTEXT"));
    assertThat(props.getProperty(BOOTSTRAP_SERVERS_CONFIG), equalTo("1.1.1.1:1,2.2.2.2:2,3.3.3.3:3"));
  }

  @Test
  public void configurationAsMap() throws Exception {
    envVars.set("KAFKA_URL", "kafka://1.1.1.1:1,kafka://2.2.2.2:2,kafka://3.3.3.3:3");

    Map<String, Object> props = HerokuKafkaConnectionHelper.getConfigMap();

    assertThat(props.get(SECURITY_PROTOCOL_CONFIG), equalTo("PLAINTEXT"));
    assertThat(props.get(BOOTSTRAP_SERVERS_CONFIG), equalTo("1.1.1.1:1,2.2.2.2:2,3.3.3.3:3"));
  }
}