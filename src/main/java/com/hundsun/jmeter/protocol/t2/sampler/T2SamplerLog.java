package com.hundsun.jmeter.protocol.t2.sampler;

import com.hundsun.jmeter.protocol.t2.sampler.T2Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hundsun.t2sdk.impl.util.AbstractLogAdapter;

public class T2SamplerLog extends AbstractLogAdapter{
	
	private static final Logger log = LoggerFactory.getLogger(T2Sampler.class);
	
	public void export(String msg)
	{
	  log.info("T2LOG: " + msg);
	}
}
