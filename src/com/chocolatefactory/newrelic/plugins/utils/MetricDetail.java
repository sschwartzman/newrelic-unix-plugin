package com.chocolatefactory.newrelic.plugins.utils;

public class MetricDetail {
	
	private String name, units, prefix;
	private int ratio;
	
	public static enum metricTypes{NORMAL, DELTA, EPOCH, INCREMENT};
	private metricTypes this_type;
	
	public MetricDetail(String pname, String mname, String munits, metricTypes mtype, int mratio) {		
		setPrefix(pname);
		setName(mname);
		setRatio(mratio);
		setUnits(munits);
		setType(mtype);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public metricTypes getType() {
		return this_type;
	}

	public void setType(metricTypes mtype) {
		this.this_type = mtype;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
