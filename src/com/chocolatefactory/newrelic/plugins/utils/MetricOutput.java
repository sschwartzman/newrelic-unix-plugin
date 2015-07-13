package com.chocolatefactory.newrelic.plugins.utils;

import com.newrelic.metrics.publish.processors.EpochProcessor;

public class MetricOutput {

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
					this.mvalue = (float) 0;
				}
				this.mvalue = mv.floatValue() + this.getValue().floatValue();
				break;
			case DELTA:
				this.mvalue = dvalue.process(mv);
				if(this.mvalue == null) {
					this.mvalue = (float) 0;
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
