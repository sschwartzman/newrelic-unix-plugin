package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class SolarisMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "SunOS";
	
	public SolarisMetrics() {
		
		/*
		 * Parser & declaration for 'df' command
		 */
		HashMap<Pattern, String[]> dfMapping = new HashMap<Pattern, String[]>();
		dfMapping.put(Pattern.compile("\\s*([\\/\\w\\d]+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%.*"),
			new String[]{kColumnMetricPrefix, "1K-blocks", "Used", "Available", "Use%"});
		allCommands.put("df", new UnixCommand(new String[]{"df","-k"}, commandTypes.REGEXDIM, defaultignores, 0, dfMapping));
		allMetrics.put(CommandMetricUtils.mungeString("df", "1K-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Use%"), new MetricDetail("Disk", "Used", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'iostat' command
		 */
		HashMap<Pattern, String[]> iostatMapping = new HashMap<Pattern, String[]>();
		iostatMapping.put(Pattern.compile("\\s*([\\/\\w\\d]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)" +
			"\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix,"r-s","w-s","kr-s","kw-s","wait","actv","svc_t","%w","%b"});
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat","-x", "1", "2"}, commandTypes.REGEXDIM, defaultignores, 0, iostatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "r-s"), new MetricDetail("DiskIO", "Reads per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "w-s"), new MetricDetail("DiskIO", "Writes per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kr-s"), new MetricDetail("DiskIO", "Data Read per Second", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kw-s"), new MetricDetail("DiskIO", "Data Written per Second", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "wait"), new MetricDetail("DiskIO", "Average queue length", "transactions", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "actv"), new MetricDetail("DiskIO", "Active transactions", "transactions", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "svc_t"), new MetricDetail("DiskIO", "Average service time", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%w"), new MetricDetail("DiskIO", "Percentage of Time Non-Empty Queue", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%b"), new MetricDetail("DiskIO", "Percentage of Time Busy", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'iostat' CPU command
		 * ** NOT USED IN FAVOR OF TOP **
		 */
		HashMap<Pattern, String[]> iostatCPUMapping = new HashMap<Pattern, String[]>();
		iostatCPUMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{"us","sy","wt","id"});
		allCommands.put("iostatCPU", new UnixCommand(new String[]{"iostat","-c", "1", "2"}, commandTypes.REGEXDIM, defaultignores, 0, iostatCPUMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("iostatCPU", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostatCPU", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostatCPU", "wt"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostatCPU", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'kstat' command
		 */
		HashMap<Pattern, String[]> kstatMapping = new HashMap<Pattern, String[]>();
		kstatMapping.put(Pattern.compile("(\\w+\\d*)\\s+([0-9\\.]+)"),
			new String[]{kColumnMetricName, kColumnMetricValue});
		allCommands.put("kstat", new UnixCommand(new String[]{"kstat", "-n", kInterfacePlaceholder}, commandTypes.INTERFACEDIM, defaultignores, 0, kstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "brdcstrcv"), new MetricDetail("Network", "Receive/Broadcast", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "brdcstxmt"), new MetricDetail("Network", "Transmit/Broadcast", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "collisions"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "ierrors"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "ipackets64"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "multircv"), new MetricDetail("Network", "Receive/Multicast", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "multixmt"), new MetricDetail("Network", "Transmit/Multicast", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "norcvbuf"), new MetricDetail("Network", "Receive/Buffer Allocation Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "noxmtbuf"), new MetricDetail("Network", "Transmit/Buffer Allocation Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "obytes64"), new MetricDetail("Network", "Transmit/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "oerrors"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "opackets64"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("kstat", "rbytes64"), new MetricDetail("Network", "Receive/Bytes", "bytes", metricTypes.DELTA, 1));
		
		/*
		 * Parser & declaration for 'netstat' command
		 * ** NOT USED IN FAVOR OF KSTAT **
		 */
		HashMap<Pattern, String[]> netstatMapping = new HashMap<Pattern, String[]>();
		netstatMapping.put(Pattern.compile("(\\w+\\d*)\\s+(\\d+)\\s+[\\d\\.]+\\s+[\\d\\.]+"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "MTU", "Iptks", "Ierrs", "Optks", "Oerrs", "Collis", "Queue"});
		allCommands.put("netstat", new UnixCommand(new String[]{"netstat", "-i", "-n"}, commandTypes.REGEXDIM, defaultignores, 0, netstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ipkts"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ierrs"), new MetricDetail("Network", "Receive/Errors", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Opkts"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Oerrs"), new MetricDetail("Network", "Transmit/Errors", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Collis"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Queue"), new MetricDetail("Network", "Queue", "packets", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'ps' command
		 */
		HashMap<Pattern, String[]> psMapping = new HashMap<Pattern, String[]>();
		psMapping.put(Pattern.compile("([0-9\\.]+)\\s+([0-9\\.]+)\\s+(\\d+)\\s+(.+)"),
				new String[]{"%CPU", "%MEM", "RSS", kColumnMetricPrefixCount});
		allCommands.put("ps", new UnixCommand(new String[]{"ps", "-eo", "pcpu,pmem,rss,comm"}, commandTypes.REGEXDIM, defaultignores, 0, psMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("ps", kColumnMetricPrefixCount), new MetricDetail("Processes", "Instance Count", "processes", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%CPU"), new MetricDetail("Processes", "Aggregate CPU", "%", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%MEM"), new MetricDetail("Processes", "Aggregate Memory", "%", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "RSS"), new MetricDetail("Processes", "Aggregate Resident Size", "kb", metricTypes.INCREMENT, 1));
			
		/*
		 * Parser & declaration for 'swap' command
		 */
		HashMap<Pattern, String[]> swapMapping = new HashMap<Pattern, String[]>();
		swapMapping.put(Pattern.compile("total:\\s+(\\d+)k\\s+bytes allocated\\s+\\+\\s+(\\d+)k\\s+reserved\\s+=\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+available"),
			new String[]{"swalloc", "swres", "swused", "swavail"});
		allCommands.put("swap", new UnixCommand(new String[]{"/usr/sbin/swap","-s"}, commandTypes.REGEXDIM, defaultignores, 0, swapMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("swap", "swalloc"), new MetricDetail("MemoryDetailed/Swap", "Allocated", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("swap", "swres"), new MetricDetail("MemoryDetailed/Swap", "Reserved", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("swap", "swused"), new MetricDetail("MemoryDetailed/Swap", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("swap", "swavail"), new MetricDetail("MemoryDetailed/Swap", "Available", "kb", metricTypes.NORMAL, 1));
		
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
		topMapping.put(Pattern.compile("CPU states:\\s+([0-9\\.]+)%\\s+idle,\\s+([0-9\\.]+)%\\s+user,\\s+([0-9\\.]+)%\\s+kernel,\\s+([0-9\\.]+)%\\s+iowait,\\s+([0-9\\.]+)%\\s+swap"),
			new String[]{"cpuidle", "cpuuser", "cpukern", "cpuiowait", "cpuswap"});
		allCommands.put("top", new UnixCommand(new String[]{"top","-b"}, commandTypes.REGEXDIM, defaultignores, 5, topMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("top", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proctot"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "procslp"), new MetricDetail("Processes", "Sleeping", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "proccpu"), new MetricDetail("Processes", "On CPU", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memphys"), new MetricDetail("MemoryDetailed", "PhysMem/Total", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memfree"), new MetricDetail("MemoryDetailed", "PhysMem/Free", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swaptot"), new MetricDetail("MemoryDetailed", "Swap/Total", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free", "mb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuidle"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuuser"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpukern"), new MetricDetail("CPU", "Kernel", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuiowait"), new MetricDetail("CPU", "IOWait", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuswap"), new MetricDetail("CPU", "Swap", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parsers & declaration for 'vmstat' command
		 */
		HashMap<Pattern, String[]> vmstatMapping = new HashMap<Pattern, String[]>();
		vmstatMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+[-]{0,1}(\\d+)"
			+ "\\s+[-]{0,1}(\\d+)\\s+[-]{0,1}(\\d+)\\s+[-]{0,1}(\\d+)\\s+(\\d+)"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+\\d+\\s+\\d+\\s+\\d+"),
			new String[]{"r","b","w","swap","free","re","mf","pi","po","fr",
				"de","sr","d0","d1","d2","d3","in","sy","cs"});
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat", "1", "2"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatMapping));

		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "w"), new MetricDetail("KernelThreads", "Swapped", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "swap"), new MetricDetail("Memory", "Swap", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "re"), new MetricDetail("Page", "Reclaimed", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "mf"), new MetricDetail("Page", "Page Faults", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pi"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "po"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "fr"), new MetricDetail("Page", "Freed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "de"), new MetricDetail("Page", "Anticipated Short-term Shortfall", "kb", metricTypes.NORMAL, 4096));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sr"), new MetricDetail("Page", "Pages Scanned by Clock Algorithm", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d0"), new MetricDetail("Disk", "disk0/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d1"), new MetricDetail("Disk", "disk1/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d2"), new MetricDetail("Disk", "disk2/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d3"), new MetricDetail("Disk", "disk3/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sy"), new MetricDetail("Faults", "System Calls", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		/*
		** Skipping 3 columns of vmstat for CPU measurement - using iostat instead.
		** allMetrics.put(CommandMetricUtils.mungeString("vmstat", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		** allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		** allMetrics.put(CommandMetricUtils.mungeString("vmstat", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		*/
		
		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEDIM, defaultignores));
	}
}
