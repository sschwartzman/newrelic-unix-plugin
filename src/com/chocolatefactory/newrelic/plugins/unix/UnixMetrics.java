package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;

public class UnixMetrics {
	
	public static final String kCategoryMetricName="Component";
	public static final String kDeltaMetricName="delta";
	public static final String kOverviewMetricName="overview";
	public static final String kDefaultMetricType="ms";
	public static final int kMetricInterval = 60;
	public static final char kMetricTreeDivider='/';
	public static final float kGigabytesToBytes=1073741824;
	public static final float kMegabytesToBytes=1048576;
	public static final String kDefaultAgentName = "AIX";
	public static final String kAgentVersion = "0.1";
	public static final String kAgentGuid = "com.chocolatefactory.newrelic.plugins.unix";
	public HashMap<String, MetricDetail> dfMetrics = new HashMap<String, MetricDetail>();
	public HashMap<String, MetricDetail> iostatMetrics = new HashMap<String, MetricDetail>();
	public HashMap<String, MetricDetail> vmstatMetrics = new HashMap<String, MetricDetail>();
	public HashMap<String, MetricDetail> netstatMetrics = new HashMap<String, MetricDetail>();
	
	public UnixMetrics() {
		
		dfMetrics.put("1024-blocks", new MetricDetail("Total Size", "k", MetricDetail.metricTypes.NORMAL, 1));
		dfMetrics.put("Free", new MetricDetail("Free Size", "k", MetricDetail.metricTypes.NORMAL, 1));
		dfMetrics.put("%Used", new MetricDetail("Used", "%", MetricDetail.metricTypes.NORMAL, 1));
		dfMetrics.put("Iused", new MetricDetail("INodes Used", "inodes", MetricDetail.metricTypes.NORMAL, 1));
		dfMetrics.put("%Iused", new MetricDetail("INodes Used", "%", MetricDetail.metricTypes.NORMAL, 1));
		
		iostatMetrics.put("tin", new MetricDetail("tty/Total Read", "chars", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("tout", new MetricDetail("tty/Total Written", "chars", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("%user", new MetricDetail("CPU/User", "%", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("%sys", new MetricDetail("CPU/System", "%", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("%idle", new MetricDetail("CPU/Idle", "%", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("%iowait", new MetricDetail("CPU/Waiting", "%", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("%tm_act", new MetricDetail("Disk/Percentage of Time Active", "%", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("Kbps", new MetricDetail("Disk/Data Transferred Per Second", "kb", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("tps", new MetricDetail("Disk/Transfers per Second", "transfers", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("Kb_read", new MetricDetail("Disk/KB Read", "kb", MetricDetail.metricTypes.NORMAL, 1));
		iostatMetrics.put("Kb_wrtn", new MetricDetail("Disk/KB Written", "kb", MetricDetail.metricTypes.NORMAL, 1));

		vmstatMetrics.put("r", new MetricDetail("Kernel Threads/Runnable", "threads", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("b", new MetricDetail("Kernel Threads/In Wait Queue", "threads", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("avm", new MetricDetail("Memory/Active Virtual Pages", "bytes", MetricDetail.metricTypes.NORMAL, 4096));
		vmstatMetrics.put("fre", new MetricDetail("Memory/Free List", "bytes", MetricDetail.metricTypes.NORMAL, 4096));
		vmstatMetrics.put("re", new MetricDetail("Page/Pager Input/Output List", "pages", MetricDetail.metricTypes.NORMAL, 1024));
		vmstatMetrics.put("pi", new MetricDetail("Page/Pages Paged In From Paging Space", "pages", MetricDetail.metricTypes.NORMAL, 1024));
		vmstatMetrics.put("po", new MetricDetail("Page/Pages Paged Out To Paging Space", "pages", MetricDetail.metricTypes.NORMAL, 1024));
		vmstatMetrics.put("fr", new MetricDetail("Page/Pages Freed", "pages", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("sr", new MetricDetail("Page/Pages Scanned By Page-Replacement", "pages", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("cy", new MetricDetail("Page/Clock Cycles By Page-Replacement", "cycles", MetricDetail.metricTypes.NORMAL, 1024));
		vmstatMetrics.put("in", new MetricDetail("Faults/Device Interrupts", "interrupts", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("sy", new MetricDetail("Faults/System Calls", "threads", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("cs", new MetricDetail("Faults/Context Switches", "switches", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("us", new MetricDetail("CPU/User", "%", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("sy", new MetricDetail("CPU/System", "%", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("id", new MetricDetail("CPU/Idle", "%", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("wa", new MetricDetail("CPU/IOWait", "%", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("alp", new MetricDetail("LargePage/In Use", "large pages", MetricDetail.metricTypes.NORMAL, 1));
		vmstatMetrics.put("flp", new MetricDetail("LargePage/Free List", "large pages", MetricDetail.metricTypes.NORMAL, 1));

		netstatMetrics.put("ESTABLISHED", new MetricDetail("connections/ESTABLISHED", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("SYN_SENT", new MetricDetail("connections/SYN_SENT", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("SYN_RECV", new MetricDetail("connections/SYN_RECV", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("FIN_WAIT1", new MetricDetail("connections/FIN_WAIT1", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("FIN_WAIT2", new MetricDetail("connections/FIN_WAIT2", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("TIME_WAIT", new MetricDetail("connections/TIME_WAIT", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("CLOSED", new MetricDetail("connections/CLOSED", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("CLOSED_WAIT", new MetricDetail("connections/CLOSED_WAIT", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("LAST_ACK", new MetricDetail("connections/LAST_ACK", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("CLOSING", new MetricDetail("connections/CLOSING", "connections", MetricDetail.metricTypes.INCREMENT, 1));
		netstatMetrics.put("UNKNOWN", new MetricDetail("connections/UNKNOWN", "connections", MetricDetail.metricTypes.INCREMENT, 1));
	}
}
