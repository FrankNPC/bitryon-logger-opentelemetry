
![Java](https://img.shields.io/badge/Java-9+-blue?logo=java)
![OpenTelemetry](https://img.shields.io/badge/observability-OpenTelemetry-blueviolet?logo=opentelemetry)


Bitryon Logger Opentelemetry integration.

the LogDispatcher is to forward logs from bitryon logger to opentelemetry's receiver and exporter.
In general, clients should have own local storage and exporter to prevent log loss before sending to bitryon directly.

Currently bitryon logger ingest server supports HTTPs with string/binary data, will see if there is a need of gRPC.

Ideally, we should upload logs from the log files so we can keep file name and logs consistent.


For conf:

local.app-key is required to identify the app to upload logs.

app-node.host-id is required to identify the logs from which node created the logs.

app-node.application-name is for the file name because, we're not able precisely get the file name during the log farward. And the log file name isnt that important.


To start bitryon logger and opentelemetry:

```java 
// In spring
public class BitryonIntegrationExampleBootApplication {
	public static void main(String[] args) {
//		// must load before everything. or add in META-INF/spring.factories 
//		io.bitryon.logger.boostrap.LoggingInitiation.premain(null);

//		// start after the logging proxy to launch Opentelemetry
//		LoggerProvider provider = LoggerFactory.getLoggerProvider(
//				new OpentelemetryLogDispatcher("http://127.0.0.1:8080/v1/logs"));

		// we can also declare it as a LogDispatcher bean if use AutoConfigurationBitryonLogger.class

		new SpringApplicationBuilder(BitryonIntegrationExampleBootApplication.class).run(args);
	}
}
```

Or, check out io.bitryon.logger.opentelemetry.BitryonLoggingOpenTelemetryExample

