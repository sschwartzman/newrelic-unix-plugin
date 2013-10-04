package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;

import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

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
	public HashMap<String, MetricDetail> allMetrics = new HashMap<String, MetricDetail>();
	public HashMap<String, UnixCommand> allCommands = new HashMap<String, UnixCommand>();
	public static enum commandTypes{MULTIDIM, SINGLEDIM};
	
	class UnixCommand {
		private String[] command;
		private commandTypes type;
		
		UnixCommand(String[] tc, commandTypes tt) {
			setCommand(tc);
			setType(tt);
		}
		
		public String[] getCommand() {
			return command;
		}
		public void setCommand(String[] command) {
			this.command = command;
		}
		public commandTypes getType() {
			return type;
		}
		public void setType(commandTypes type) {
			this.type = type;
		}
    }
	
	public String mungeString(String str1, String str2) {
		return str1 + "/" + str2;
	}
	
	public UnixMetrics() {
		// AIX Commands - first to go in
		allCommands.put(mungeString("aix", "df"), new UnixCommand(new String[]{"df","-k"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("aix","iostat"), new UnixCommand(new String[]{"iostat","-s","-f"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("aix","netstat"), new UnixCommand(new String[]{"netstat","-n","-f","inet"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("aix","vmstat"), new UnixCommand(new String[]{"vmstat","-l"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("aix","VirtualMemory"), new UnixCommand(new String[]{"vmstat","-v"}, commandTypes.SINGLEDIM));
		
		// Linux Commands
		allCommands.put(mungeString("linux", "df"), new UnixCommand(new String[]{"df","-k"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("linux","iostat"), new UnixCommand(new String[]{"iostat","-x"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("linux","netstat"), new UnixCommand(new String[]{"netstat","-n"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("linux","vmstat"), new UnixCommand(new String[]{"vmstat","-l"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("linux","free"), new UnixCommand(new String[]{"free"}, commandTypes.MULTIDIM));
		
		// Solaris Commands
		allCommands.put(mungeString("sunos", "df"), new UnixCommand(new String[]{"df","-k"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("sunos","iostat"), new UnixCommand(new String[]{"iostat","-xtc"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("sunos","netstat"), new UnixCommand(new String[]{"netstat","-n","-f","inet"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("sunos","vmstat"), new UnixCommand(new String[]{"vmstat"}, commandTypes.MULTIDIM));
		allCommands.put(mungeString("sunos","VmstatTotals"), new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SINGLEDIM));
				
		// Metrics - based on AIX, subject to change
		allMetrics.put(mungeString("df", "1024-blocks"), new MetricDetail("Disk", "Total Size", "k", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "Free"), new MetricDetail("Disk", "Free Size", "k", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "%Used"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "Iused"), new MetricDetail("Disk", "INodes Used", "inodes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "%Iused"), new MetricDetail("Disk", "INodes Used", "%", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("iostat", "tin"), new MetricDetail("IO", "Total Read", "chars", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "tout"), new MetricDetail("IO", "Total Written", "chars", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%user"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%sys"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%idle"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%iowait"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%tm_act"), new MetricDetail("Disk", "Percentage of Time Active", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "Kbps"), new MetricDetail("Disk", "Data Transferred Per Second", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "tps"), new MetricDetail("Disk", "Transfers per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "Kb_read"), new MetricDetail("Disk", "KB Read", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "Kb_wrtn"), new MetricDetail("Disk", "KB Written", "kb", metricTypes.NORMAL, 1));

		allMetrics.put(mungeString("vmstat", "r"), new MetricDetail("Kernel Threads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "b"), new MetricDetail("Kernel Threads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "avm"), new MetricDetail("Memory", "Active Virtual Pages", "bytes", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "fre"), new MetricDetail("Memory", "Free List", "bytes", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "re"), new MetricDetail("Page", "Pager IO List", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "pi"), new MetricDetail("Page", "Pages Paged In From Paging Space", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "po"), new MetricDetail("Page", "Pages Paged Out To Paging Space", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "fr"), new MetricDetail("Page", "Pages Freed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "sr"), new MetricDetail("Page", "Pages Scanned By Page-Replacement", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "cy"), new MetricDetail("Page", "Clock Cycles By Page-Replacement", "cycles", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("Faults", "System Calls", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		// Get from iostat instead
		// allMetrics.put(mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		// allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		// allMetrics.put(mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		// allMetrics.put(mungeString("vmstat", "wa"), new MetricDetail("CPU", "IOWait", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "alp"), new MetricDetail("LargePage", "In Use", "large pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "flp"), new MetricDetail("LargePage", "Free List", "large pages", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("netstat", "ESTABLISHED"), new MetricDetail("Connections", "ESTABLISHED", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "SYN_SENT"), new MetricDetail("Connections", "SYN_SENT", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "SYN_RECV"), new MetricDetail("Connections", "SYN_RECV", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "FIN_WAIT1"), new MetricDetail("Connections", "FIN_WAIT1", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "FIN_WAIT2"), new MetricDetail("Connections", "FIN_WAIT2", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "TIME_WAIT"), new MetricDetail("Connections", "TIME_WAIT", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "CLOSED"), new MetricDetail("Connections", "CLOSED", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "CLOSED_WAIT"), new MetricDetail("Connections", "CLOSED_WAIT", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "LAST_ACK"), new MetricDetail("Connections", "LAST_ACK", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "CLOSING"), new MetricDetail("Connections", "CLOSING", "connections", metricTypes.INCREMENT, 1));
		allMetrics.put(mungeString("netstat", "UNKNOWN"), new MetricDetail("Connections", "UNKNOWN", "connections", metricTypes.INCREMENT, 1));
		
		// Linux-specific Metrics
		allMetrics.put(mungeString("vmstat", "swap"), new MetricDetail("Memory", "Swap Available", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "swpd"), new MetricDetail("Memory", "Used", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "free"), new MetricDetail("Memory", "Idle", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "buff"), new MetricDetail("Memory", "Buffer", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "si"), new MetricDetail("Swap", "In From Disk", "KB/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "so"), new MetricDetail("Swap", "Out To Disk", "KB/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "bi"), new MetricDetail("IO", "Sent", "Blocks/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "bo"), new MetricDetail("IO", "Received", "Blocks/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "total"), new MetricDetail("Memory", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "used"), new MetricDetail("Memory", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "shared"), new MetricDetail("Memory", "Shared", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "buffers"), new MetricDetail("Memory", "Buffers", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "cached"), new MetricDetail("Memory", "Cached", "kb", metricTypes.NORMAL, 1));
		
		// Solaris-specific Metrics
		allMetrics.put(mungeString("iostat", "r/s"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "w/s"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kr/s"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kw/s"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "wait"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "actv"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "svc_t"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%w"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%b"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kps"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kps"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kps"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kps"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kps"), new MetricDetail("Disk", "Kilobytes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "wt"), new MetricDetail("CPU", "IOWait", "%", metricTypes.NORMAL, 1));

		allMetrics.put(mungeString("vmstat", "w"), new MetricDetail("Kernel Threads", "Swapped", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "swap"), new MetricDetail("Memory", "Swap Available", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "mf"), new MetricDetail("Page", "Minor Faults", "faults", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "de"), new MetricDetail("Page", "Anticipated Short-term Shortfall", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "sr"), new MetricDetail("Page", "Pages Scanned by Clock Algorithm", "pages", metricTypes.NORMAL, 4096));
	}
}
