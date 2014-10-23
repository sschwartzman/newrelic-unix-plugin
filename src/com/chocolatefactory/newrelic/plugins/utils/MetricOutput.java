package com.chocolatefactory.newrelic.plugins.utils;

import com.newrelic.metrics.publish.processors.EpochCounter;

public class MetricOutput {

	private MetricDetail mdetail;
	private String mname_prefix; 
	private Number mvalue;
	private EpochCounter dvalue;
	
	public MetricOutput(MetricDetail md, String mp, Number mv) {
		setNamePrefix(mp);
		setMetricDetail(md);
		
		// Initialize EpochCounter if this will measure a delta. 
		// Precedes setValue such that the initial value gets set appropriately.
		if (this.getMetricDetail().getType().equals(MetricDetail.metricTypes.DELTA)) {
			dvalue = new EpochCounter();
		}
		
		setValue(mv);
	}

	public String getNamePrefix() {
		return mname_prefix;
	}

	public void setNamePrefix(String mp) {
		this.mname_prefix = mp;
	}

	public Number getValue() {
		return mvalue;
	}

	public void setValue(Number mv) {
		if (this.getMetricDetail().getType().equals(MetricDetail.metricTypes.DELTA)) {
			this.mvalue = dvalue.process(mv);
		} else {
			this.mvalue = mv;
		}
		this.mvalue = mv;
	}

	public MetricDetail getMetricDetail() {
		return mdetail;
	}

	public void setMetricDetail(MetricDetail md) {
		this.mdetail = md;
	}

}
