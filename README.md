
![Java](https://img.shields.io/badge/Java-9+-blue?logo=java)
![OpenTelemetry](https://img.shields.io/badge/observability-OpenTelemetry-blueviolet?logo=opentelemetry)


# Bitryon Logger Opentelemetry.

bitryon-logger-opentelemetry is a wrapper through opentelemetry and LogDispatcher, to forward logs from bitryon logger to opentelemetry's receiver and exporter.
In general, clients should have own local storage and exporter to prevent log loss before sending to bitryon directly.

- Currently bitryon logger ingest server supports HTTPs with string/binary data, will see if there is a need of gRPC.

bitryon logger is a tracer and a logger both. See [bitryon-logging-java-example](https://github.com/FrankNPC/bitryon-logging-examples)


### There are two options with OpenTelemetry to upload logs:

1, OpentelemetryLogDispatcher, conf:

https://dev-logging-ingest-server.bitryon.io:8443/v1/logs 

local.app-key is required to identify the app to upload logs. 

app-node.host-id is required to identify the logs from which node created the logs.

app-node.application-name will be the file name because, we're not able precisely get the file name during the log forward.



2, OpenTelemetry's built-in Collector and Exporter, conf:

https://dev-logging-ingest-server.bitryon.io:8443/v2/logs 

```java
receivers:
  filelog:
    include:
      - /var/log/myapp.log
    include_file_name: true
    start_at: end
    poll_interval: 200ms
    operators:
      - type: add
        field: attributes.environment
        value: production

exporters:
  otlphttp:
    endpoint: "https://your-receiver.example.com:4318"
    headers:
      Authorization: "Bearer ApiKey_abc123"
      host-id: "your-service-name" # required
    tls:
      insecure: false

service:
  pipelines:
    logs:
      receivers: [filelog]
      exporters: [otlphttp]

```




## start bitryon logger and OpenTelemetry:

```java 
// In spring
public class BitryonIntegrationExampleBootApplication {
	public static void main(String[] args) {
//		// must load before everything. or add in META-INF/spring.factories 
//		io.bitryon.logger.boostrap.LoggingProxyInitiation.premain(null);

//		// start after the logging proxy to launch Opentelemetry
//		LoggerProvider provider = LoggerFactory.getLoggerProvider(
//				new OpentelemetryLogDispatcher("http://127.0.0.1:8080/v1/logs"));

		// we can also declare it as a LogDispatcher bean if use AutoConfigurationBitryonLogger.class

		new SpringApplicationBuilder(BitryonIntegrationExampleBootApplication.class).run(args);
	}
}
```

Or, check out [BitryonLoggingOpenTelemetryExampleTest](https://github.com/FrankNPC/bitryon-logger-opentelemetry/blob/master/src/test/java/io/bitryon/logger/opentelemetry/BitryonLoggingOpenTelemetryExampleTest.java)

