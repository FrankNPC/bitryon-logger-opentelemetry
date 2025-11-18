package io.bitryon.logger.opentelemetry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.bitryon.logger.boostrap.LoggingProxyInitiation;
import io.bitryon.logger.boostrap.LoggingMethodIntercepter;
import io.bitryon.logger.provider.LoggerFactory;
import io.bitryon.logger.provider.LoggerProvider;

public class BitryonLoggingOpenTelemetryExampleTest {
	
	static {
		// 1: load logger agent
		LoggingProxyInitiation.premain(null);
	}

	@Test
	public void test_OpentelemetryLogDispatcher() throws IOException {

		// 2: load logger configure
//		// start after the logging proxy to launch Opentelemetry
		LoggerProvider provider = LoggerFactory.getLoggerProvider(
				new OpenTelemetryLogDispatcher("http://127.0.0.1:8080/v1/logs"));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		provider.addOutputStream(bos);
		
		// 3: load LoggingMethodIntercepter, In spring it doesn't need
		// Must do to setup LoggingMethodIntercepter and LoggerProvider
		new LoggingMethodIntercepter(provider);
//		new LoggingMethodIntercepter(LoggerFactory.getLoggerProvider());
		
		String rand1 = ""+System.currentTimeMillis();
		POJOSampleHelper.staticRandomPOJOSample(rand1);
		
		String rand2 = ""+System.currentTimeMillis();
		new POJOSampleHelper().publicRandomPOJOSample(rand2);
		
		Assertions.assertTrue(bos.toString().contains("POJOSampleHelper.java#io.bitryon.logger.opentelemetry.POJOSampleHelper#staticRandomPOJOSample#10#|"));
		Assertions.assertTrue(bos.toString().contains("\"bigName\": null"));
		Assertions.assertTrue(bos.toString().contains("\"tags\": [\n		\"123\",\n		\"456\""));
		Assertions.assertTrue(bos.toString().contains("POJOSampleHelper.java#io.bitryon.logger.opentelemetry.POJOSampleHelper#publicRandomPOJOSample#19#|"));
		Assertions.assertTrue(bos.toString().contains("POJOSampleHelper.java#io.bitryon.logger.opentelemetry.POJOSampleHelper#publicRandomPOJOSample#19#R|\n[{\n	\"id\": "));

	}
}
