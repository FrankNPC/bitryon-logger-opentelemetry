package io.bitryon.logger.opentelemetry;

import java.util.Arrays;

import io.bitryon.logger.annotation.Logging;

public class POJOSampleHelper {

	@Logging
	static POJOSample staticRandomPOJOSample(String name) {
		POJOSample sample = new POJOSample();
		sample.setId(System.currentTimeMillis());
		sample.setTags(Arrays.asList("123", "456"));
		return sample;
	}
	

	@Logging
	public POJOSample publicRandomPOJOSample(String name) {
		POJOSample sample = new POJOSample();
		sample.setId(System.currentTimeMillis());
		sample.setTags(Arrays.asList("123", "456"));
		return sample;
	}
	
}

