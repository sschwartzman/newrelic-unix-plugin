package com.chocolatefactory.newrelic.plugins.unix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;

public class SolarisMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "SunOS";
	
	public SolarisMetrics() {

		super();
		
		/*
		 * Parser & declaration for 'df' command
		 */
		HashMap<Pattern, String[]> dfMapping = new HashMap<Pattern, String[]>();
		dfMapping.put(Pattern.compile("\\s*(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%.*"),
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
			new String[]{kColumnMetricPrefix,"r-i","w-i","kr-i","kw-i","wait","actv","svc_t","%w","%b"});
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat","-x","-I"}, commandTypes.REGEXDIM, defaultignores, 0, iostatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "r-i"), new MetricDetail("DiskIO", "Reads per Interval", "transfers", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "w-i"), new MetricDetail("DiskIO", "Writes per Interval", "transfers", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kr-i"), new MetricDetail("DiskIO", "Data Read per Interval", "kb", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kw-i"), new MetricDetail("DiskIO", "Data Written per Interval", "kb", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "wait"), new MetricDetail("DiskIO", "Average queue length", "transactions", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "actv"), new MetricDetail("DiskIO", "Active transactions", "transactions", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "svc_t"), new MetricDetail("DiskIO", "Average service time", "ms", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%w"), new MetricDetail("DiskIO", "Percentage of Time Non-Empty Queue", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "%b"), new MetricDetail("DiskIO", "Percentage of Time Busy", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'iostat' CPU command
		 * ** only used on Solaris 10 (Solaris 11 has 'top') **
		 */
		HashMap<Pattern, String[]> iostatCPUMapping = new HashMap<Pattern, String[]>();
		iostatCPUMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{"us","sy","wt","id"});
		allCommands.put("IostatCPU", new UnixCommand(new String[]{"iostat","-c", "1", "2"}, commandTypes.REGEXDIM, defaultignores, 0, iostatCPUMapping));
		// To make this backwards-compatible with older Solaris plugin.json versions.
		allCommands.put("iostatCPU", new UnixCommand(new String[]{"iostat","-c", "1", "2"}, commandTypes.REGEXDIM, defaultignores, 0, iostatCPUMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("iostatcpu", "us"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostatcpu", "sy"), new MetricDetail("CPU", "System", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostatcpu", "wt"), new MetricDetail("CPU", "Waiting", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostatcpu", "id"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'kstat' command for Network information
		 */
		HashMap<Pattern, String[]> kstatNetworkMapping = new HashMap<Pattern, String[]>();
		kstatNetworkMapping.put(Pattern.compile("(\\w+\\d*)\\s+([0-9\\.]+)"),
			new String[]{kColumnMetricName, kColumnMetricValue});
		allCommands.put("KstatNetwork", new UnixCommand(new String[]{"kstat", "-n", kMemberPlaceholder}, commandTypes.REGEXLISTDIM, defaultignores, 0, kstatNetworkMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "brdcstrcv"), new MetricDetail("Network", "Receive/Broadcast", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "brdcstxmt"), new MetricDetail("Network", "Transmit/Broadcast", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "collisions"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "ierrors"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "ipackets64"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "multircv"), new MetricDetail("Network", "Receive/Multicast", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "multixmt"), new MetricDetail("Network", "Transmit/Multicast", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "norcvbuf"), new MetricDetail("Network", "Receive/Buffer Allocation Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "noxmtbuf"), new MetricDetail("Network", "Transmit/Buffer Allocation Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "obytes64"), new MetricDetail("Network", "Transmit/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "oerrors"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "opackets64"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatNetwork", "rbytes64"), new MetricDetail("Network", "Receive/Bytes", "bytes", metricTypes.DELTA, 1));
		
		/*
		 * Parser & declaration for 'kstat' command for Page information
		 * 
		 */
		HashMap<Pattern, String[]> kstatPagesMapping = new HashMap<Pattern, String[]>();
		kstatPagesMapping.put(Pattern.compile("unix:0:system_pages:([\\w_]+)\\s+([0-9\\.]+)"),
			new String[]{kColumnMetricName, kColumnMetricValue});
		allCommands.put("KstatPages", new UnixCommand(new String[]{"kstat", "-p", "unix::system_pages"}, commandTypes.REGEXDIM, defaultignores, 0, kstatPagesMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("KstatPages", "availrmem"), new MetricDetail("Page", "Available", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatPages", "pagesfree"), new MetricDetail("Page", "Free", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatPages", "pageslocked"), new MetricDetail("Page", "Locked", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatPages", "pagestotal"), new MetricDetail("Page", "Total", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatMemory", "physmem"), new MetricDetail("Page", "Physical Total", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("KstatPages", "pp_kernel"), new MetricDetail("Page", "Used By Kernel", "pages", metricTypes.DELTA, 1));
		
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
		 * Parser & declaration for 'prstat' command
		 * ** only used on Solaris 10 (Solaris 11 has 'top') **
		 */
		HashMap<Pattern, String[]> prstatMapping = new HashMap<Pattern, String[]>();
		prstatMapping.put(Pattern.compile("Total:\\s+(\\d+)\\s+processes,\\s+(\\d+)\\s+lwps,\\s+load averages:\\s+([\\d\\.]+),\\s+([\\d\\.]+),\\s+([\\d\\.]+)"),
			new String[]{"procs", "lwps", "la1", "la5", "la15"});
		allCommands.put("prstat", new UnixCommand(new String[]{"prstat", "-c", "1", "1"}, commandTypes.REGEXDIM, defaultignores, 0, prstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("prstat", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("prstat", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("prstat", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("prstat", "procs"), new MetricDetail("Processes", "Total", "processes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("prstat", "lwps"), new MetricDetail("Processes", "Lightweight", "processes", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'prtconf' command
		 * ** only used on Solaris 10 (Solaris 11 has 'top') **
		 */
		//HashMap<Pattern, String[]> prtconfMapping = new HashMap<Pattern, String[]>();
		//prtconfMapping.put(Pattern.compile("Memory size:\\s+(\\d+)\\s+Megabytes"),
		//	new String[]{"memsize"});
		//allCommands.put("prtconf", new UnixCommand(new String[]{"/usr/sbin/prtconf"}, commandTypes.REGEXDIM, defaultignores, 0, prtconfMapping));
		//
		//allMetrics.put(CommandMetricUtils.mungeString("prtconf", "memsize"), new MetricDetail("Memory", "Total", "kb", metricTypes.NORMAL, 1024));
		
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
		 * ** only used on Solaris 11 (Solaris 10 does not always have 'top') **
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
		allMetrics.put(CommandMetricUtils.mungeString("top", "memphys"), new MetricDetail("MemoryDetailed", "Physical/Total", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "memfree"), new MetricDetail("MemoryDetailed", "Physical/Free", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swaptot"), new MetricDetail("MemoryDetailed", "Swap/Total On Disk", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "swapfree"), new MetricDetail("MemoryDetailed", "Swap/Free On Disk", "kb", metricTypes.NORMAL, 1024));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuidle"), new MetricDetail("CPU", "Idle", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuuser"), new MetricDetail("CPU", "User", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpukern"), new MetricDetail("CPU", "Kernel", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuiowait"), new MetricDetail("CPU", "IOWait", "%", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("top", "cpuswap"), new MetricDetail("CPU", "Swap", "%", metricTypes.NORMAL, 1));
		
		/*
		 * Parsers & declaration for 'vmstat' command to get threads and memory
		 */
		HashMap<Pattern, String[]> vmstatThreadsMemoryMapping = new HashMap<Pattern, String[]>();
		// vmstatThreadsMemoryMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"
		//	+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+[-]{0,1}(\\d+)"
		//	+ "\\s+[-]{0,1}(\\d+)\\s+[-]{0,1}(\\d+)\\s+[-]{0,1}(\\d+)\\s+(\\d+)"
		//	+ "\\s+(\\d+)\\s+(\\d+).*"),
		//	new String[]{"r","b","w","swap","free","re","mf","pi","po","fr",
		//		"de","sr","d0","d1","d2","d3","in","sy","cs"});
		vmstatThreadsMemoryMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"),
			new String[]{"r","b","w","swap","free"});
		vmstatThreadsMemoryMapping.put(Pattern.compile("Memory size:\\s+(\\d+)\\s*Megabytes"),
				new String[]{"memsize"});
		ArrayList<String[]> vmstatCommands = new ArrayList<String[]>();
		vmstatCommands.add(new String[]{"vmstat", "1", "2"});
		vmstatCommands.add(new String[]{"/usr/sbin/prtconf"});
		allCommands.put("VmstatThreadsMemory", new UnixCommand(vmstatCommands, commandTypes.REGEXDIM, defaultignores, 0, vmstatThreadsMemoryMapping));

		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreadsMemory", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreadsMemory", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreadsMemory", "w"), new MetricDetail("KernelThreads", "Swapped", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreadsMemory", "swap"), new MetricDetail("Memory", "Swap", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreadsMemory", "free"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 1));			
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreadsMemory", "memsize"), new MetricDetail("Memory", "Total", "kb", metricTypes.NORMAL, 1024));
		
		/*
		 * Replaced with deltas from vmstat -s (more accurate, no waiting!)
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "re"), new MetricDetail("Page", "Reclaimed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "mf"), new MetricDetail("Page", "Page Faults", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pi"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "po"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "fr"), new MetricDetail("Page", "Freed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "de"), new MetricDetail("Page", "Anticipated Short-term Shortfall", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sr"), new MetricDetail("Page", "Pages Scanned by Clock Algorithm", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d0"), new MetricDetail("Disk", "disk0/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d1"), new MetricDetail("Disk", "disk1/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d2"), new MetricDetail("Disk", "disk2/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "d3"), new MetricDetail("Disk", "disk3/Operations per Second", "ops", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sy"), new MetricDetail("Faults", "System Calls", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		*/
		
		/* 
		 * Parses & declaration for 'vmstat' command to get pages & faults
		 */
		
		HashMap<Pattern, String[]> vmstatPagesFaultsMapping = new HashMap<Pattern, String[]>();
		vmstatPagesFaultsMapping.put(Pattern.compile("\\s*(\\d+)\\s+(.*)"),
			new String[]{kColumnMetricValue, kColumnMetricName});
		allCommands.put("VmstatPagesFaults", new UnixCommand(new String[]{"vmstat", "-s"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatPagesFaultsMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "pages swapped in"), new MetricDetail("Page", "Swapped In", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "pages swapped out"), new MetricDetail("Page", "Swapped Out", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "total address trans. faults taken"), new MetricDetail("Faults", "Address Translation", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "pages paged in"), new MetricDetail("Page", "Paged In", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "pages paged out"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "micro (hat) faults"), new MetricDetail("Faults", "Micro (hat)", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "minor (as) faults"), new MetricDetail("Faults", "Minor (as)", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "major faults"), new MetricDetail("Faults", "Major", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "copy-on-write faults"), new MetricDetail("Faults", "Copy-on-write", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "zero fill page faults"), new MetricDetail("Faults", "Zero Fill Page", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "cpu context switches"), new MetricDetail("Faults", "CPU Context Switches", "Faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "device interrupts"), new MetricDetail("Faults", "Device Interrupts", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "traps"), new MetricDetail("Faults", "Traps", "faults", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatPagesFaults", "system calls"), new MetricDetail("Faults", "System Calls", "Faults", metricTypes.DELTA, 1));

		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat","-s"}, commandTypes.SIMPLEDIM, defaultignores));
	}
}
