package io.bitryon.logger.opentelemetry;

import java.io.IOException;
import java.time.Duration;

import io.bitryon.logger.ConfKeyDefinition;
import io.bitryon.logger.provider.LoggerProvider;
import io.bitryon.logger.provider.io.LogDispatcher;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;


public class OpentelemetryLogDispatcher implements LogDispatcher {
	
	private String expoterEndpoint;
	public OpentelemetryLogDispatcher(String expoterEndpoint) {
		this.expoterEndpoint = expoterEndpoint;
	}
	
	private Logger otelLogger;
	private OtlpHttpLogRecordExporter otlpHttpLogRecordExporter;
	private SdkLoggerProvider sdkLoggerProvider ;

	private Logger getOtelLogger(LoggerProvider bitryonlLoggerProvider) {
		if (otelLogger == null) {
			otelLogger = loadOtelLogger(bitryonlLoggerProvider);
		}
		return otelLogger;
	}
	
	private synchronized Logger loadOtelLogger(LoggerProvider bitryonlLoggerProvider) {
		if (otelLogger == null) {
			
			Resource resource = 
					Resource
					.getDefault()
					.toBuilder()
					.put(ServiceAttributes.SERVICE_NAME,
							bitryonlLoggerProvider.getAppNodeConfiguration().getApplicationName())
					.put(ServiceAttributes.SERVICE_VERSION, 
							bitryonlLoggerProvider.getLoggerConfiguration().getLoggerSchemaVersion())
					.build();

			otlpHttpLogRecordExporter = OtlpHttpLogRecordExporter.builder()
					.setEndpoint(expoterEndpoint)
					.addHeader("Authorization", "Bearer " + bitryonlLoggerProvider.getLocalConfiguration().getAppKey())// appkey
					.addHeader(ConfKeyDefinition.HOST_ID, bitryonlLoggerProvider.getAppNodeConfiguration().getHostId())
					.addHeader(ConfKeyDefinition.FILE_NAME, bitryonlLoggerProvider.getAppNodeConfiguration().getApplicationName())// the constant file name if no file writing
					.build();

//			// Not supported yet
//			OtlpGrpcLogRecordExporter exporter = OtlpGrpcLogRecordExporter
//					.builder()
//					.setEndpoint(expoterEndpoint)
//					.addHeader("Authorization", "Bearer " + bitryonlLoggerProvider.getLocalConfiguration().getAppKey())// appkey
//					.build();

			sdkLoggerProvider = 
					SdkLoggerProvider
					.builder()
					.setResource(resource)
					.addLogRecordProcessor(
							BatchLogRecordProcessor
							.builder(otlpHttpLogRecordExporter)
							.setMaxQueueSize(2048)
							.setMaxExportBatchSize(512)
							.setScheduleDelay(Duration.ofSeconds(5)).build())
					.build();

			otelLogger = sdkLoggerProvider.loggerBuilder(bitryonlLoggerProvider.getLogger().getClass().getName()).build();
		}
		return otelLogger;
	}

	@Override
	public void emit(LoggerProvider bitryonlLoggerProvider, String traceId, byte[] logBytes) {
		getOtelLogger(bitryonlLoggerProvider)
			.logRecordBuilder()
			//.setSeverity(Severity.INFO)
			.setBody(Value.of(logBytes))
			.emit();
	}

	@Override
	public void close() throws IOException {
		sdkLoggerProvider.forceFlush();
		sdkLoggerProvider.shutdown();
		
		otlpHttpLogRecordExporter.flush();
		otlpHttpLogRecordExporter.shutdown();
	}

}
