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
	private String fileName = "";
	private OtlpHttpLogRecordExporter otlpHttpLogRecordExporter;
	private SdkLoggerProvider sdkLoggerProvider ;

	private Logger getOtelLogger(LoggerProvider bitryonlLoggerProvider) {
		if (otelLogger == null) {
			otelLogger = loadOtelLogger(bitryonlLoggerProvider, false);
		}else if (!fileName.equals(bitryonlLoggerProvider.getCurrentLogFileName())) {
			otelLogger = loadOtelLogger(bitryonlLoggerProvider, true);
		}
		return otelLogger;
	}
	
	private synchronized Logger loadOtelLogger(LoggerProvider bitryonlLoggerProvider, boolean force) {
		if (otelLogger == null || force) {
			if (otelLogger!=null && fileName.equals(bitryonlLoggerProvider.getCurrentLogFileName())) { return otelLogger; }
			
			Resource resource = 
					Resource
					.getDefault()
					.toBuilder()
					.put(ServiceAttributes.SERVICE_NAME,
							bitryonlLoggerProvider.getAppNodeConfiguration().getApplicationName())
					.put(ServiceAttributes.SERVICE_VERSION, 
							bitryonlLoggerProvider.getLoggerConfiguration().getLoggerSchemaVersion())
					.build();

			String theFileName = bitryonlLoggerProvider.getCurrentLogFileName();
			
			OtlpHttpLogRecordExporter theOtlpHttpLogRecordExporter = OtlpHttpLogRecordExporter.builder()
					.setEndpoint(expoterEndpoint)
					.addHeader("Authorization", "Bearer " + bitryonlLoggerProvider.getLocalConfiguration().getAppKey())// appkey
					.addHeader(ConfKeyDefinition.HOST_ID, bitryonlLoggerProvider.getAppNodeConfiguration().getHostId())
					.addHeader(ConfKeyDefinition.FILE_NAME, theFileName)
					.build();

//			// Not supported yet
//			OtlpGrpcLogRecordExporter exporter = OtlpGrpcLogRecordExporter
//					.builder()
//					.setEndpoint(expoterEndpoint)
//					.addHeader("Authorization", "Bearer " + bitryonlLoggerProvider.getLocalConfiguration().getAppKey())// appkey
//					.build();

			SdkLoggerProvider theSdkLoggerProvider = 
					SdkLoggerProvider
					.builder()
					.setResource(resource)
					.addLogRecordProcessor(
							BatchLogRecordProcessor
							.builder(theOtlpHttpLogRecordExporter)
							.setMaxQueueSize(2048)
							.setMaxExportBatchSize(512)
							.setScheduleDelay(Duration.ofSeconds(5)).build())
					.build();

			otelLogger = theSdkLoggerProvider.loggerBuilder(bitryonlLoggerProvider.getLogger().getClass().getName()).build();
			fileName = theFileName;

			// call a thread to shut down the old exporters
			if (otlpHttpLogRecordExporter!=null) {
				new Thread(()->{
					sdkLoggerProvider.forceFlush();
					sdkLoggerProvider.shutdown();
					
					otlpHttpLogRecordExporter.flush();
					otlpHttpLogRecordExporter.shutdown();
				}).start();
			}
			otlpHttpLogRecordExporter = theOtlpHttpLogRecordExporter;
			sdkLoggerProvider = theSdkLoggerProvider;
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
