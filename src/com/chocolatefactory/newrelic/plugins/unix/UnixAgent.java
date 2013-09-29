package com.chocolatefactory.newrelic.plugins.unix;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricOutput;
import com.newrelic.metrics.publish.Agent;

public class UnixAgent extends Agent {

	CommandMetricUtils metricUtils = new CommandMetricUtils();
	boolean useFile = false;
	UnixMetrics aixmetrics = new UnixMetrics();
	HashMap<String, MetricOutput> dfMetricOutput = new HashMap<String, MetricOutput>();
	HashMap<String, MetricOutput> vmstatMetricOutput = new HashMap<String, MetricOutput>();

	public UnixAgent(String GUID, String version) {
		super(GUID, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void pollCycle() {
		// VMSTAT Metrics
		String[] vmstatArgs = {"vmstat", "-l"};
		BufferedReader vmstatCommand = metricUtils.executeCommand(vmstatArgs, false);
		try {
			metricUtils.parseMultiMetricOutput(vmstatMetricOutput, aixmetrics.vmstatMetrics, vmstatCommand);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metricUtils.printMetrics(vmstatMetricOutput);
		
		// DF Metrics
		
		String[] dfArgs = {"df", "-k"};
		BufferedReader dfCommand = metricUtils.executeCommand(dfArgs, false);
		try {
			metricUtils.parseMultiMetricOutput(dfMetricOutput, aixmetrics.dfMetrics, dfCommand);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metricUtils.printMetrics(dfMetricOutput);
	}

	@Override
	public String getComponentHumanLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}
