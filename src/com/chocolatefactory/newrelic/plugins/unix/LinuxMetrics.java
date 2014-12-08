package com.chocolatefactory.newrelic.plugins.unix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class LinuxMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "Linux";
	public List<Integer> linuxvmstatignores = Arrays.asList(13, 14, 15, 16, 17);
	
	public LinuxMetrics() {
		/*
		** LINUX Commands
		*/
		allCommands.put("df", new UnixCommand(new String[]{"df","-Pk"}, commandTypes.COMPLEXDIM, defaultignores));
		// Free is unnecessary if using 'top', memory figures are the same
		allCommands.put("free", new UnixCommand(new String[]{"free"}, commandTypes.COMPLEXDIM, defaultignores));
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat","-k"}, commandTypes.COMPLEXDIM, defaultignores));
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat"}, commandTypes.MULTIDIM, linuxvmstatignores));
		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEMULTIDIM, defaultignores));
		// Not yet written: parsing for netstat
		// allCommands.put("netstat", new UnixCommand(new String[]{"netstat","-n"}, commandTypes.MULTIDIM, defaultignores));
		
		/*
		 * Parsers & declaration for 'top' command
		*/		
		HashMap<Pattern, String[]> topMapping = new HashMap<Pattern, String[]>();
		topMapping.put(Pattern.compile("top.*load average:\\s+([0-9\\.]+),\\s+([0-9\\.]+),\\s+([0-9\\.]+)"), 
				new String[]{"la1", "la5", "la15"});
		topMapping.put(Pattern.compile("Tasks:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+running,\\s+(\\d+)\\s+sleeping,\\s+(\\d+)\\s+stopped,\\s+(\\d+)\\s+zombie"), 
				new String[]{"proctot", "procrun", "proczzz", "procstop", "proczomb"});
		topMapping.put(Pattern.compile("Mem:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,\\s+(\\d+)k\\s+buffers"), 
				new String[]{"memtot", "memused", "memfree", "membuff"});
		topMapping.put(Pattern.compile("Swap:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,\\s+(\\d+)k\\s+cached"), 
				new String[]{"swaptot", "swapused", "swapfree", "swapbuff"});
		allCommands.put("top", new UnixCommand(new String[]{"top","-b","-n","1"}, commandTypes.SINGLELINEDIM, defaultignores, 5, topMapping));
		
		/*
		** LINUX-specific Metrics
		*/
		allMetrics.put(mungeString("df", "1k-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "1024-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "use%"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("df", "capacity"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
		
		// free is unnecessary if using 'top'
		allMetrics.put(mungeString("free", "total"), new MetricDetail("MemoryDetailed", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "used"), new MetricDetail("MemoryDetailed", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "free"), new MetricDetail("MemoryDetailed", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "shared"), new MetricDetail("MemoryDetailed", "Shared", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "buffers"), new MetricDetail("MemoryDetailed", "Buffers", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("free", "cached"), new MetricDetail("MemoryDetailed", "Cached", "kb", metricTypes.NORMAL, 1));
				
		allMetrics.put(mungeString("iostat", "%user"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%nice"), new MetricDetail("CPU", "Nice", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%system"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%iowait"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%steal"), new MetricDetail("CPU", "Stolen", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "%idle"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "tps"), new MetricDetail("DiskIO", "Transfers per Second", "transfers/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kb_read-s"), new MetricDetail("DiskIO", "Data Read per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kb_read"), new MetricDetail("DiskIO", "Data Read", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kb_wrtn-s"), new MetricDetail("DiskIO", "Data Written per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("iostat", "kb_wrtn"), new MetricDetail("DiskIO", "Data Written", "kb", metricTypes.NORMAL, 1));

		// free is unnecessary if using 'top'
		allMetrics.put(mungeString("top", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "proctot"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "procrun"), new MetricDetail("Processes", "Running", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "proczzz"), new MetricDetail("Processes", "Sleeping", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "procstop"), new MetricDetail("Processes", "Stopped", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "proczomb"), new MetricDetail("Processes", "Zombie", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "memtot"), new MetricDetail("MemoryDetailed", "PhysMem/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "memused"), new MetricDetail("MemoryDetailed", "PhysMem/Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "memfree"), new MetricDetail("MemoryDetailed", "PhysMem/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "membuff"), new MetricDetail("MemoryDetailed", "PhysMem/Buffer", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "swaptot"), new MetricDetail("MemoryDetailed", "Swap/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "swapused"), new MetricDetail("MemoryDetailed", "Swap/Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("top", "swapbuff"), new MetricDetail("MemoryDetailed", "Swap/Buffer", "kb", metricTypes.NORMAL, 1));
		
		allMetrics.put(mungeString("vmstat", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "swpd"), new MetricDetail("Memory", "Swap", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "buff"), new MetricDetail("Memory", "Buffer", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "cache"), new MetricDetail("Memory", "Cache", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(mungeString("vmstat", "si"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "so"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "bi"), new MetricDetail("IO", "Sent", "Blocks", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "bo"), new MetricDetail("IO", "Received", "Blocks", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		
		/*
		 * Skipping last 5 columns of vmstat for CPU measurement - using iostat instead.
		 * allMetrics.put(mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "wa"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(mungeString("vmstat", "st"), new MetricDetail("CPU", "Stolen", "%", metricTypes.NORMAL, 1));
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
