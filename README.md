
![Java](https://img.shields.io/badge/Java-9+-blue?logo=java)
![OpenTelemetry](https://img.shields.io/badge/observability-OpenTelemetry-blueviolet?logo=opentelemetry)


Opentelemetry integration.

the LogDispatcher is to forward logs from bitryon logger to opentelemetry's receiver and exporter.
In general, clients should schedule own local storage and exporter to prevent log loss before sending to bitryon directly.
Currently bitryon logger ingest server only supports HTTPs with string/binary data, will see if there is a need of gRPC.

Ideally, we should upload logs from the log files so we can keep file name and logs consistent.


For conf:

local.app-key is required to identify the app to upload logs.

app-node.host-id is required to identify the logs from which node created the logs.

logger.file-name is required to identify the file of the logs. It will be only one file name. You can turn off writing logs to file and console, only to opentelemetry.


To start bitryon logger and opentelemetry:

```java
public class BitryonIntegrationExampleBootApplication {
	public static void main(String[] args) {
//		// must load before everything. or add in META-INF/spring.factories 
//		io.bitryon.logger.boostrap.LoggingInitiation.premain(null);
//		// start after the logging proxy to launch Opentelemetry
//		io.bitryon.logger.provider.LoggerFactory.getLoggerProvider(
//				new io.bitryon.logger.opentelemetry.OpentelemetryLogDispatcher(null));
		new SpringApplicationBuilder(BitryonIntegrationExampleBootApplication.class).run(args);
	}
}
```

Or, check out io.bitryon.logger.opentelemetry.BitryonLoggingOpenTelemetryExample

