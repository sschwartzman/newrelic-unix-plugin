package com.chocolatefactory.newrelic.plugins.unix;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class LinuxMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "Linux";
	public List<Integer> linuxvmstatignores = Arrays.asList(13, 14, 15, 16, 17);
	
	public LinuxMetrics() {
		/*
		 * Parser & declaration for 'df' command
		 */
		HashMap<Pattern, String[]> dfMapping = new HashMap<Pattern, String[]>();
		dfMapping.put(Pattern.compile("\\s*([\\/\\w\\d]+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%.*"),
			new String[]{kColumnMetricPrefix, "1024-blocks", "Used", "Available", "Capacity"});
		allCommands.put("df", new UnixCommand(new String[]{"df","-Pk"}, commandTypes.REGEXDIM, defaultignores, 0, dfMapping));
		allMetrics.put(CommandMetricUtils.mungeString("df", "1024-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Capacity"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
						
		/*
		 * Parsers & declaration for 'iostat' command
		 */
		HashMap<Pattern, String[]> iostatMapping = new HashMap<Pattern, String[]>();
		iostatMapping.put(Pattern.compile("\\s*([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)"),
			new String[]{"%user", "%nice", "%system", "%iowait", "%steal", "%idle"});
		iostatMapping.put(Pattern.compile("(\\w+[-]{0,1}\\d*)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "tps", "kB_read-s", "kB_wrtn-s", "kB_read", "kB_wrtn"});
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat","-k"}, commandTypes.REGEXDIM, defaultignores, 0, iostatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%user"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%nice"), new MetricDetail("CPU", "Nice", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%system"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%iowait"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%steal"), new MetricDetail("CPU", "Stolen", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%idle"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "tps"), new MetricDetail("DiskIO", "Transfers per Second", "transfers/s", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kB_read-s"), new MetricDetail("DiskIO", "Data Read per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kB_read"), new MetricDetail("DiskIO", "Data Read", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kB_wrtn-s"), new MetricDetail("DiskIO", "Data Written per Second", "kb/s", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kB_wrtn"), new MetricDetail("DiskIO", "Data Written", "kb", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for "NetworkIO"
		 */
		HashMap<Pattern, String[]> networkIOMapping = new HashMap<Pattern, String[]>();
		networkIOMapping.put(Pattern.compile("\\/sys\\/class\\/net\\/[\\w\\d]+\\/statistics\\/([\\w_]+):(\\d+)"),
			new String[]{kColumnMetricName, kColumnMetricValue});	
		allCommands.put("NetworkIO", new UnixCommand(new String[]{"grep", "-r", ".", "/sys/class/net/" + kInterfacePlaceholder + "/statistics", "2>&1"}, 
				commandTypes.INTERFACEDIM, defaultignores, 0, networkIOMapping));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "collisions"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "multicast"), new MetricDetail("Network", "Multicast", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_bytes"), new MetricDetail("Network", "Receive/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_compressed"), new MetricDetail("Network", "Receive/Compressed", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_crc_errors"), new MetricDetail("Network", "Receive/CRC Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_dropped"), new MetricDetail("Network", "Receive/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_errors"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_fifo_errors"), new MetricDetail("Network", "Receive/FIFO Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_frame_errors"), new MetricDetail("Network", "Receive/Frame Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_length_errors"), new MetricDetail("Network", "Receive/Length Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_missed_errors"), new MetricDetail("Network", "Receive/Missed Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_over_errors"), new MetricDetail("Network", "Receive/Overrun Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "rx_packets"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_aborted_errors"), new MetricDetail("Network", "Transmit/Aborted Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_bytes"), new MetricDetail("Network", "Transmit/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_carrier_errors"), new MetricDetail("Network", "Transmit/Carrier Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_compressed"), new MetricDetail("Network", "Transmit/Compressed", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_dropped"), new MetricDetail("Network", "Transmit/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_errors"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_fifo_errors"), new MetricDetail("Network", "Transmit/FIFO Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_heartbeat_errors"), new MetricDetail("Network", "Transmit/Heartbeat Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_packets"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("NetworkIO", "tx_window_errors"), new MetricDetail("Network", "Transmit/Window Errors", "errors", metricTypes.DELTA, 1));
		
		/*
		 * Parser & declaration for 'netstat' command
		 * ** NOT USED IN FAVOR OF NETWORKIO **
		 */
		HashMap<Pattern, String[]> netstatMapping = new HashMap<Pattern, String[]>();
		netstatMapping.put(Pattern.compile("(\\w+\\d*)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"),
			new String[]{kColumnMetricPrefix, "MTU", "Met", "RX-OK", "RX-ERR", "RX-DRP", "RX-OVR", "TX-OK", "TX-ERR", "TX-DRP", "TX-OVR"});	
		allCommands.put("netstat", new UnixCommand(new String[]{"netstat", "-i"}, commandTypes.REGEXDIM, defaultignores, 0, netstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "MTU"), new MetricDetail("Network", "MTU", "packets", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Met"), new MetricDetail("Network", "Metric", "metric", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-OK"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-ERR"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-DRP"), new MetricDetail("Network", "Receive/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "RX-OVR"), new MetricDetail("Network", "Receive/Overrun Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-O:q!"
				+ "K"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-ERR"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-DRP"), new MetricDetail("Network", "Transmit/Drops", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "TX-OVR"), new MetricDetail("Network", "Transmit/Overrun Errors", "errors", metricTypes.DELTA, 1));
		
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
		allCommands.put("top", new UnixCommand(new String[]{"top","-b","-n","1"}, commandTypes.REGEXDIM, defaultignores, 5, topMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("top", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proctot"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procrun"), new MetricDetail("Processes", "Running", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proczzz"), new MetricDetail("Processes", "Sleeping", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procstop"), new MetricDetail("Processes", "Stopped", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proczomb"), new MetricDetail("Processes", "Zombie", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memtot"), new MetricDetail("MemoryDetailed", "PhysMem/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memused"), new MetricDetail("MemoryDetailed", "PhysMem/Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memfree"), new MetricDetail("MemoryDetailed", "PhysMem/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "membuff"), new MetricDetail("MemoryDetailed", "PhysMem/Buffer", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swaptot"), new MetricDetail("MemoryDetailed", "Swap/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapused"), new MetricDetail("MemoryDetailed", "Swap/Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapbuff"), new MetricDetail("MemoryDetailed", "Swap/Buffer", "kb", metricTypes.NORMAL, 1));
		
		/*
		 * Parsers & declaration for 'vmstat' command
		 */	
		HashMap<Pattern, String[]> vmstatMapping = new HashMap<Pattern, String[]>();
		vmstatMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\d+"),
			new String[]{"r", "b", "swpd", "free", "buff", "cache", "si", "so", "bi", "bo", "in", "cs"});
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatMapping));

		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "swpd"), new MetricDetail("Memory", "Swap", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "buff"), new MetricDetail("Memory", "Buffer", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cache"), new MetricDetail("Memory", "Cache", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "si"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "so"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "bi"), new MetricDetail("IO", "Sent", "Blocks", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "bo"), new MetricDetail("IO", "Received", "Blocks", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		/*
		 * Skipping last 5 columns of vmstat for CPU measurement - using iostat instead.
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "wa"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		 * allMetrics.put(CommandMetricUtils.mungeString("vmstat", "st"), new MetricDetail("CPU", "Stolen", "%", metricTypes.NORMAL, 1));
	     */

		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEDIM, defaultignores));
	}
}
