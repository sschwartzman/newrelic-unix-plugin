package com.chocolatefactory.newrelic.plugins.unix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class AIXMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "AIX";
	public List<Integer> aixvmstatignores = Arrays.asList(14, 15, 16, 17);
	
	public AIXMetrics() {
		/*
		** AIX Commands
		** Ignore fields 14-17 (CPU measurements) from VMSTAT, get from LPARSTAT instead.
		*/
		allCommands.put("df", new UnixCommand(new String[]{"df","-k"}, commandTypes.COMPLEXDIM, defaultignores));
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat","-d"}, commandTypes.COMPLEXDIM, defaultignores));
		allCommands.put("lparstat", new UnixCommand(new String[]{"lparstat"}, commandTypes.MULTIDIM, defaultignores));
		allCommands.put("svmon", new UnixCommand(new String[]{"svmon","-G","-Ounit=KB"}, commandTypes.COMPLEXDIM, aixvmstatignores));
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat"}, commandTypes.MULTIDIM, aixvmstatignores));
		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEMULTIDIM, defaultignores));

		/*
		 * Parser & declaration for 'uptime' command
		 */
		HashMap<Pattern, String[]> uptimeMapping = new HashMap<Pattern, String[]>();
		uptimeMapping.put(Pattern.compile(".*load average:\\s+([0-9\\.]+),\\s+([0-9\\.]+),\\s+([0-9\\.]+)"),
				new String[]{"la1", "la5", "la15"});
		allCommands.put("uptime", new UnixCommand(new String[]{"uptime"}, commandTypes.SINGLELINEDIM, defaultignores, 0, uptimeMapping));

		// using lparstat instead of iostat to get CPU
		// allCommands.put("IostatTtyCpu", new UnixCommand(new String[]{"iostat","-t"}, commandTypes.MULTIDIM, defaultignores));
		// Not yet written: parsing for netstat
		// allCommands.put("netstat", new UnixCommand(new String[]{"netstat","-n","-f","inet"}, commandTypes.MULTIDIM, defaultignores));		
		
		/*
		** AIX-specific Metrics
		*/
		allMetrics.put(mungeString("df", "1024-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "free"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "%used"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "iused"), new MetricDetail("Disk", "INodes Used", "inodes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "%iused"), new MetricDetail("Disk", "INodes Used", "%", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("iostat", "kbps"), new MetricDetail("DiskIO", "Data Transferred per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "tps"), new MetricDetail("DiskIO", "Transfers per Second", "transfers/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kb_read"), new MetricDetail("DiskIO", "Data Read", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kb_wrtn"), new MetricDetail("DiskIO", "Data Written", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "tm_act"), new MetricDetail("DiskIO", "Percentage of Time Active", "%", metricTypes.NORMAL, 1));

		allMetrics.put(mungeString("lparstat", "%user"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("lparstat", "%sys"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("lparstat", "%idle"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("lparstat", "%wait"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("svmon", "size"), new MetricDetail("MemoryDetailed", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "inuse"), new MetricDetail("MemoryDetailed", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "free"), new MetricDetail("MemoryDetailed", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "pin"), new MetricDetail("MemoryDetailed", "Pinned", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "virtual"), new MetricDetail("MemoryDetailed", "Virtual", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "available"), new MetricDetail("MemoryDetailed", "Available", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "work"), new MetricDetail("MemoryDetailed", "Working", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "pers"), new MetricDetail("MemoryDetailed", "Persistent", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "clnt"), new MetricDetail("MemoryDetailed", "Client", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("svmon", "other"), new MetricDetail("MemoryDetailed", "Other", "kb", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("uptime", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("uptime", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("uptime", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("vmstat", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "avm"), new MetricDetail("Memory", "Active", "pages", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "fre"), new MetricDetail("Memory", "Free", "pages", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "re"), new MetricDetail("Page", "Reclaimed", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "pi"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "po"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "fr"), new MetricDetail("Page", "Freed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "sr"), new MetricDetail("Page", "Scanned By Page-Replacement", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "cy"), new MetricDetail("Page", "Clock Cycles By Page-Replacement", "cycles", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("Faults", "System Calls", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		/*
		 * Skipping 4 columns of vmstat for CPU measurement - using iostat instead.
		 * allMetrics.put(mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "wa"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
	    */
		
		/*
		** To be used for netstat parsing:
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
		 */
	}
}
