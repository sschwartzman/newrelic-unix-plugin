package com.chocolatefactory.newrelic.plugins.newnix.utils;

import java.util.HashMap;

import com.newrelic.metrics.publish.processors.EpochProcessor;

public class NewMetricOutput {
	private static int kMinterval = 60;
	
	private HashMap<String, Object> mdetail;
	private Number mvalue;
	private EpochProcessor dvalue;
	private boolean current;
	private String type;
	private String category;
	private String name;
	private String units;
	private double ratio;
	
	public NewMetricOutput(HashMap<String, Object> md, Number mv) {
		// Initialize EpochCounter if this will measure a delta. 
		// Precedes setValue such that the initial value gets set appropriately.
		if (md.containsKey("type")) {
			setType((String) md.get("type"));
		} else {
			setType(NewNixConstants.kDefaultMetricType);
		}
		
		if (this.getType().equals("DELTA")) {
			dvalue = new EpochProcessor();
			dvalue.process(mv);
		} else if (this.getType().equals("INCREMENT")) {
			// Initialize value to 0 if incrementor
			setValue(0);
		}
		
		if (md.containsKey("name")) {
			setName((String) md.get("name"));
		} else {
			setName(NewNixConstants.kDefaultMetricName);
		}
		
		if (md.containsKey("category")) {
			setCategory((String) md.get("category"));
		} else {
			setCategory(NewNixConstants.kDefaultMetricCategory);
		}
		
		if (md.containsKey("units")) {
			setUnits((String) md.get("units"));
		} else {
			setUnits(NewNixConstants.kDefaultMetricUnits);
		}
		
		if (md.containsKey("ratio")) {
			setRatio(md.get("ratio"));
		} else {
			setRatio(1);
		}
		
		setValue(mv);
		setCurrent(true);
	}


	public Number getValue() {
		return mvalue;
	}

	public void setValue(Number mv) {
		String thisType = this.getType();

		if(thisType.equals("INCREMENT")) {
			if(this.mvalue == null) {
				resetValue();
			}
			this.mvalue = mv.floatValue() + this.getValue().floatValue();
		// Converting to per-minute (per-interval) delta		
		// EpochProcessor returns a double that is a per-second delta 
		// (based off of the actual delta between command runs)
		} else if (thisType.equals("DELTA")) {
			try {
				this.mvalue = Math.round((dvalue.process(mv).doubleValue() * kMinterval));
			} catch(NullPointerException e) {
				resetValue();
			}
			if(this.mvalue == null) {
				resetValue();
			}
		} else {
			this.mvalue = mv;
		}
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String t) {
		type = t;
	}

	public void resetValue() {
		this.mvalue = (float) 0;
	}
	
	public HashMap<String, Object> getMetricDetail() {
		return mdetail;
	}

	public void setMetricDetail(HashMap<String, Object> md) {
		this.mdetail = md;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getUnits() {
		return units;
	}


	public void setUnits(String units) {
		this.units = units;
	}


	public Number getRatio() {
		return ratio;
	}


	public void setRatio(Object ratio) {
		if(ratio instanceof Number) {
			this.ratio = ((Number)ratio).doubleValue();
		} else if (ratio instanceof String) {
			try {
				this.ratio = Double.parseDouble((String)ratio);
			} catch (NumberFormatException ex) {
				this.ratio = NewNixConstants.kDefaultMetricRatio;
			}
		} else {
			this.ratio = NewNixConstants.kDefaultMetricRatio;
		}

	}
}
