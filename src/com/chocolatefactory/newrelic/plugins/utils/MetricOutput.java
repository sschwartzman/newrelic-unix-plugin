package com.chocolatefactory.newrelic.plugins.utils;

import com.newrelic.metrics.publish.processors.EpochProcessor;

public class MetricOutput {
	private static int kMinterval = 60;
	
	private MetricDetail mdetail;
	private String mname_prefix; 
	private Number mvalue;
	private EpochProcessor dvalue;
	private boolean current;
	
	public MetricOutput(MetricDetail md, String mp, Number mv) {
		setNamePrefix(mp);
		setMetricDetail(md);
		// Initialize EpochCounter if this will measure a delta. 
		// Precedes setValue such that the initial value gets set appropriately.
		if (this.getMetricDetail().getType().equals(MetricDetail.metricTypes.DELTA)) {
			dvalue = new EpochProcessor();
			dvalue.process(mv);
		}
		// Initialize value to 0 if incrementor
		if (this.getMetricDetail().getType().equals(MetricDetail.metricTypes.INCREMENT)) {
			setValue(0);
		}
		setValue(mv);
		setCurrent(true);
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
		switch(this.getMetricDetail().getType()) {
			case INCREMENT:
				if(this.mvalue == null) {
					resetValue();
				}
				this.mvalue = mv.floatValue() + this.getValue().floatValue();
				break;
			// Converting to per-minute (per-interval) delta		
			// EpochProcessor returns a double that is a per-second delta 
			// (based off of the actual delta between command runs)
			case DELTA:
				try {
					this.mvalue = Math.round((dvalue.process(mv).doubleValue() * kMinterval));
				} catch(NullPointerException e) {
					resetValue();
				}
				if(this.mvalue == null) {
					resetValue();
				}
				break;
			case NORMAL:
				this.mvalue = mv;
				break;
			default:
				this.mvalue = mv;
				break;
		}
	}

	public void resetValue() {
		this.mvalue = (float) 0;
	}
	
	public MetricDetail getMetricDetail() {
		return mdetail;
	}

	public void setMetricDetail(MetricDetail md) {
		this.mdetail = md;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}
}
