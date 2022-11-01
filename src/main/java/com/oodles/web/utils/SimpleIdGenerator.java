package com.oodles.web.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.codecs.IdGenerator;
import org.springframework.stereotype.Component;

@Component
public class SimpleIdGenerator implements IdGenerator{

	private final AtomicInteger atomicInteger = new AtomicInteger();	
	
	@Override
	public UUID generate() {
		return  UUID.randomUUID();
	}
	
	public String generateRandomId() {
		return UUID.randomUUID().toString().substring(0, 16).replace("-", "")
				.concat(String.valueOf(atomicInteger.getAndIncrement()));
	}
}
