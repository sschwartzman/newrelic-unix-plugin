package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class SolarisMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "SunOS";
	
	public SolarisMetrics() {
		
		/*
		** SOLARIS Commands
		*/
		allCommands.put("df", new UnixCommand(new String[]{"df","-k"}, commandTypes.COMPLEXDIM, defaultignores));
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat","-x", "-c"}, commandTypes.COMPLEXDIM, defaultignores));
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat"}, commandTypes.MULTIDIM, defaultignores));
		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEMULTIDIM, defaultignores));
		
		/*
		 * Parsers & declaration for 'top' command
		 */
		HashMap<Pattern, String[]> topMapping = new HashMap<Pattern, String[]>();
		topMapping.put(Pattern.compile("load averages:\\s+([0-9\\.]+),\\s+([0-9\\.]+),\\s+([0-9\\.]+);.*"), 
				new String[]{"la1", "la5", "la15"});
		topMapping.put(Pattern.compile("([0-9\\.]+)\\s+processes:\\s+([0-9]+)\\s+sleeping,\\s+([0-9]+) on cpu"), 
				new String[]{"proctot", "procslp", "proccpu"});
		topMapping.put(Pattern.compile("Memory:\\s+([0-9]+)M\\s+phys mem,\\s+([0-9]+)M\\s+free mem,\\s+([0-9]+)M\\s+total swap,\\s+([0-9]+)M\\s+free swap"), 
				new String[]{"memphys", "memfree", "swaptot", "swapfree"});
		allCommands.put("top", new UnixCommand(new String[]{"top","-b"}, commandTypes.SINGLELINEDIM, defaultignores, 5, topMapping));
		
		/*
		 * Parser & declaration for 'swap' command
		 */
		HashMap<Pattern, String[]> swapMapping = new HashMap<Pattern, String[]>();
		swapMapping.put(Pattern.compile("total:\\s+(\\d+)k\\s+bytes allocated\\s+\\+\\s+(\\d+)k\\s+reserved\\s+=\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+available"),
				new String[]{"swalloc", "swres", "swused", "swavail"});
		allCommands.put("swap", new UnixCommand(new String[]{"swap","-s"}, commandTypes.SINGLELINEDIM, defaultignores, 0, swapMapping));
		
		// Not yet written: parsing for netstat
		// allCommands.put("netstat", new UnixCommand(new String[]{"netstat","-n","-f","inet"}, commandTypes.MULTIDIM, defaultignores));
				
		/*
		** SOLARIS-specific Metrics
		*/
		allMetrics.put(mungeString("df", "1024-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "capacity"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("iostat", "r-s"), new MetricDetail("DiskIO", "Reads per Second", "transfers/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "w-s"), new MetricDetail("DiskIO", "Writes per Second", "transfers/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kr-s"), new MetricDetail("DiskIO", "Data Read per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kw-s"), new MetricDetail("DiskIO", "Data Written per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "wait"), new MetricDetail("DiskIO", "Average queue length", "transactions", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "actv"), new MetricDetail("DiskIO", "Active transactions", "transactions", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "svc_t"), new MetricDetail("DiskIO", "Average service time", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%w"), new MetricDetail("DiskIO", "Percentage of Time Non-Empty Queue", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%b"), new MetricDetail("DiskIO", "Percentage of Time Busy", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "wt"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
	
		allMetrics.put(mungeString("swap", "swalloc"), new MetricDetail("MemoryDetailed/Swap", "Allocated", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("swap", "swres"), new MetricDetail("MemoryDetailed/Swap", "Reserved", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("swap", "swused"), new MetricDetail("MemoryDetailed/Swap", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("swap", "swavail"), new MetricDetail("MemoryDetailed/Swap", "Available", "kb", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("top", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "proctot"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "procslp"), new MetricDetail("Processes", "Sleeping", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "proccpu"), new MetricDetail("Processes", "On CPU", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "memphys"), new MetricDetail("MemoryDetailed", "PhysMem/Total", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "memfree"), new MetricDetail("MemoryDetailed", "PhysMem/Free", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "swaptot"), new MetricDetail("MemoryDetailed", "Swap/Total", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free", "mb", metricTypes.NORMAL, 1));

		allMetrics.put(mungeString("vmstat", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "w"), new MetricDetail("KernelThreads", "Swapped", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "swap"), new MetricDetail("Memory", "Swap", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "re"), new MetricDetail("Page", "Reclaimed", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "mf"), new MetricDetail("Page", "Page Faults", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "pi"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "po"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(mungeString("vmstat", "fr"), new MetricDetail("Page", "Freed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "de"), new MetricDetail("Page", "Anticipated Short-term Shortfall", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "sr"), new MetricDetail("Page", "Pages Scanned by Clock Algorithm", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "s0"), new MetricDetail("Disk", "disk0/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "s1"), new MetricDetail("Disk", "disk1/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "s2"), new MetricDetail("Disk", "disk2/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "s3"), new MetricDetail("Disk", "disk3/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "i0"), new MetricDetail("Disk", "disk0/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "i1"), new MetricDetail("Disk", "disk1/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "i2"), new MetricDetail("Disk", "disk2/Operations per Second", "ops/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "i3"), new MetricDetail("Disk", "disk3/Operations per Second", "ops/s", metricTypes.NORMAL, 1));	
		allMetrics.put(mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		// There are 2 "sy" columns in solaris vmstat. Brilliant.
		// Since we're ignoring the 2nd one (see below), its safe to store this one.
		allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("Faults", "System Calls", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		/*
		** Skipping 3 columns of vmstat for CPU measurement - using iostat instead.
		** allMetrics.put(mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		** allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		** allMetrics.put(mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
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
