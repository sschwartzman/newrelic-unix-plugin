package com.chocolatefactory.newrelic.plugins.unix;

import java.util.HashMap;
import java.util.regex.Pattern;

import com.chocolatefactory.newrelic.plugins.utils.MetricDetail;
import com.chocolatefactory.newrelic.plugins.utils.MetricDetail.metricTypes;
import com.chocolatefactory.newrelic.plugins.utils.CommandMetricUtils;

public class AIXMetrics extends UnixMetrics {
	
	public static final String kDefaultAgentName = "AIX";
	
	public AIXMetrics() {

		super();
		
		/*
		 * Parser & declaration for 'df' command
		 */
		HashMap<Pattern, String[]> dfMapping = new HashMap<Pattern, String[]>();
		dfMapping.put(Pattern.compile("\\s*(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)%.*"),
			new String[]{kColumnMetricDiskName, "1K-blocks", "Used", "Available", "Use%", "Iused", "Ifree", "Iuse%"});
		allCommands.put("df", new UnixCommand(new String[]{"df","-v","-k","-T","local"}, commandTypes.REGEXDIM, defaultignores, 0, dfMapping));
		allMetrics.put(CommandMetricUtils.mungeString("df", "1K-blocks"), new MetricDetail("Disk", "Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Used"), new MetricDetail("Disk", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Available"), new MetricDetail("Disk", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Use%"), new MetricDetail("Disk", "Used", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Iused"), new MetricDetail("Disk", "Inodes Used", "inodes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Ifree"), new MetricDetail("Disk", "Inodes Free", "inodes", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("df", "Iuse%"), new MetricDetail("Disk", "Inodes Used", "percent", metricTypes.NORMAL, 1));

		/*
		 * Parser & declaration for 'entstat' command
		 */
		HashMap<Pattern, String[]> entstatMapping = new HashMap<Pattern, String[]>();
		entstatMapping.put(Pattern.compile(".*Packets:\\s+([0-9\\.]+)\\s+Packets:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Packets", "Receive/Packets"});
		entstatMapping.put(Pattern.compile(".*Bytes:\\s+([0-9\\.]+)\\s+Bytes:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Bytes", "Receive/Bytes"});
		entstatMapping.put(Pattern.compile(".*Interrupts:\\s+([0-9\\.]+)\\s+Interrupts:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Interrupts", "Receive/Interrupts"});
		entstatMapping.put(Pattern.compile(".*Transmit\\s+Errors:\\s+([0-9\\.]+)\\s+Receive\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Errors", "Receive/Errors"});
		entstatMapping.put(Pattern.compile(".*Packets\\s+Dropped:\\s+([0-9\\.]+)\\s+Packets\\s+Dropped:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Dropped Packets", "Receive/Dropped Packets"});
		entstatMapping.put(Pattern.compile(".*Max\\s+Packets\\s+on\\s+S/W\\s+Transmit\\s+Queue:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Max Packets on SW Transmit Queue"});
		entstatMapping.put(Pattern.compile(".*S/W\\s+Transmit\\s+Queue\\s+Overflow:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Transmit Queue Overflow"});
		entstatMapping.put(Pattern.compile(".*Current\\s+S/W\\+H/W\\s+Transmit\\s+Queue\\s+Length:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Current SW+HW Transmit Queue Length"});
		entstatMapping.put(Pattern.compile(".*Broadcast\\s+Packets:\\s+([0-9\\.]+)\\s+Broadcast\\s+Packets:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Broadcast Packets", "Receive/Broadcast Packets"});
		entstatMapping.put(Pattern.compile(".*Multicast\\s+Packets:\\s+([0-9\\.]+)\\s+Multicast\\s+Packets:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Multicast Packets", "Receive/Multicast Packets"});
		entstatMapping.put(Pattern.compile(".*No Carrier\\s+Sense:\\s+([0-9\\.]+)\\s+CRC\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/No Carrier Sense", "Receive/CRC Errors"});
		entstatMapping.put(Pattern.compile(".*DMA\\s+Underrun:\\s+([0-9\\.]+)\\s+DMA\\s+Overrun:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/DMA Underrun", "Receive/DMA Overrun"});
		entstatMapping.put(Pattern.compile(".*Lost CTS\\s+Errors:\\s+([0-9\\.]+)\\s+Alignment\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Lost CTS Errors", "Receive/Alignment Errors"});
		entstatMapping.put(Pattern.compile(".*Max Collision\\s+Errors:\\s+([0-9\\.]+)\\s+No Resource\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Max Collision Errors", "Receive/No Resource Errors"});
		entstatMapping.put(Pattern.compile(".*Late Collision\\s+Errors:\\s+([0-9\\.]+)\\s+Receive Collision\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Late Collision Errors", "Receive/Receive Collision Errors"});
		entstatMapping.put(Pattern.compile(".*Deferred:\\s+([0-9\\.]+)\\s+Packet Too Short\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Deferred", "Receive/Packet Too Short Errors"});
		entstatMapping.put(Pattern.compile(".*SQE\\s+Test:\\s+([0-9\\.]+)\\s+Packet Too Long\\s+Errors:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/SQE Test", "Receive/Packet Too Long Errors"});
		entstatMapping.put(Pattern.compile(".*Timeout\\s+Errors:\\s+([0-9\\.]+)\\s+Packets Discarded by\\s+Adapter:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Timeout Errors", "Receive/Packets Discarded by Adapter"});
		entstatMapping.put(Pattern.compile(".*Single Collision\\s+Count:\\s+([0-9\\.]+)\\s+Receiver Start\\s+Count:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Single Collision Count", "Receive/Receiver Start Count"});
		entstatMapping.put(Pattern.compile(".*Multiple Collision\\s+Count:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Multiple Collision Count"});
		entstatMapping.put(Pattern.compile(".*Current HW Transmit Queue\\s+Length:\\s+([0-9\\.]+)"),
			new String[]{"Transmit/Current HW Transmit Queue Length"});
		allCommands.put("entstat", new UnixCommand(new String[]{"entstat", UnixMetrics.kMemberPlaceholder}, commandTypes.REGEXLISTDIM, defaultignores, 0, entstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Packets"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Bytes"), new MetricDetail("Network", "Transmit/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Interrupts"), new MetricDetail("Network", "Transmit/Interrupts", "interrupts", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Errors"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Packets Dropped"), new MetricDetail("Network", "Transmit/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Max Packets on SW Transmit Queue"), new MetricDetail("Network", "Transmit/Max Packets on SW Transmit Queue", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/SW Transmit Queue Overflow"), new MetricDetail("Network", "Transmit/SW Transmit Queue Overflow", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Current SW+HW Transmit Queue Length"), new MetricDetail("Network", "Transmit/Current SW+HW Transmit Queue Length", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Broadcast Packets"), new MetricDetail("Network", "Transmit/Broadcast Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Multicast Packets"), new MetricDetail("Network", "Transmit/Multicast Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/No Carrier Sense"), new MetricDetail("Network", "Transmit/No Carrier Sense", "transmissions", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/DMA Underrun"), new MetricDetail("Network", "Transmit/DMA Underrun", "transmissions", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Lost CTS Errors"), new MetricDetail("Network", "Transmit/Lost CTS Errors", "transmissions", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Max Collision Errors"), new MetricDetail("Network", "Transmit/Max Collision Errors", "transmissions", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Late Collision Errors"), new MetricDetail("Network", "Transmit/Late Collision Errors", "transmissions", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Deferred"), new MetricDetail("Network", "Transmit/Deferred", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/SQE Test"), new MetricDetail("Network", "Transmit/SQE Test", "tests", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Timeout Errors"), new MetricDetail("Network", "Transmit/Timeout Errors", "transmissions", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Single Collision Count"), new MetricDetail("Network", "Transmit/Single Collision Count", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Multiple Collision Count"), new MetricDetail("Network", "Transmit/Multiple Collision Count", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Transmit/Current HW Transmit Queue Length"), new MetricDetail("Network", "Transmit/Current HW Transmit Queue Length", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Packets"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Bytes"), new MetricDetail("Network", "Receive/Bytes", "bytes", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Interrupts"), new MetricDetail("Network", "Receive/Interrupts", "interrupts", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Errors"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Packets Dropped"), new MetricDetail("Network", "Receive/Dropped", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Bad Packets"), new MetricDetail("Network", "Receive/Bad Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Broadcast Packets"), new MetricDetail("Network", "Receive/Broadcast Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Multicast Packets"), new MetricDetail("Network", "Receive/Multicast Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/CRC Errors"), new MetricDetail("Network", "Receive/CRC Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/DMA Overrun"), new MetricDetail("Network", "Receive/DMA Overrun", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Alignment Errors"), new MetricDetail("Network", "Receive/Alignment Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/No Resource Errors"), new MetricDetail("Network", "Receive/No Resource Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Receive Collision Errors"), new MetricDetail("Network", "Receive/Receive Collision Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Packet Too Short Errors"), new MetricDetail("Network", "Receive/Packet Too Short Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Packet Too Long Errors"), new MetricDetail("Network", "Receive/Packet Too Long Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Packets Discarded by Adapter"), new MetricDetail("Network", "Receive/Packets Discarded by Adapter", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("entstat", "Receive/Receiver Start Count"), new MetricDetail("Network", "Receive/Receiver Start Count", "count", metricTypes.DELTA, 1));

		/*
		 * Parser & declaration for 'iostat' command
		 */
		HashMap<Pattern, String[]> iostatMapping = new HashMap<Pattern, String[]>();
		iostatMapping.put(Pattern.compile("\\s*(\\w+\\d*)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)"),
			new String[]{kColumnMetricPrefix, "tm_act", "Kbps", "tps", "Kb_read", "Kb_wrtn"});
		allCommands.put("iostat", new UnixCommand(new String[]{"iostat", "-d"}, commandTypes.REGEXDIM, defaultignores, 0, iostatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kbps"), new MetricDetail("DiskIO", "Average Data Transferred per Second", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "tps"), new MetricDetail("DiskIO", "Average Transfers per Second", "transfers", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kb_read"), new MetricDetail("DiskIO", "Data Read per Interval", "kb", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "kb_wrtn"), new MetricDetail("DiskIO", "Data Written per Interval", "kb", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("iostat", "tm_act"), new MetricDetail("DiskIO", "Average Percentage of Time Busy", "percent", metricTypes.NORMAL, 1));
	
		/*
		 * Parser & declaration for 'lparstat' command
		 */
		HashMap<Pattern, String[]> lparstatMapping = new HashMap<Pattern, String[]>();
		lparstatMapping.put(Pattern.compile("\\s*([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+).*"),
			new String[]{"user", "sys", "wait", "idle"});
		allCommands.put("lparstat", new UnixCommand(new String[]{"lparstat", "1", "1"}, commandTypes.REGEXDIM, defaultignores, 0, lparstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("lparstat", "user"), new MetricDetail("CPU", "User", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("lparstat", "sys"), new MetricDetail("CPU", "System", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("lparstat", "wait"), new MetricDetail("CPU", "IOWait", "percent", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("lparstat", "idle"), new MetricDetail("CPU", "Idle", "percent", metricTypes.NORMAL, 1));

		/*
		 * Parser & declaration for 'netstat' command
		 * ** NOT USED IN FAVOR OF ENTSTAT **
		 */
		HashMap<Pattern, String[]> netstatMapping = new HashMap<Pattern, String[]>();
		netstatMapping.put(Pattern.compile("\\w+\\d*\\s+(\\d+)\\s+[\\d\\.]+\\s+[\\d\\.]+"
			+ "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{"MTU", "Iptks", "Ierrs", "Optks", "Oerrs", "Coll"});
		allCommands.put("netstat", new UnixCommand(new String[]{"netstat", "-i", "-n"}, commandTypes.REGEXDIM, defaultignores, 0, netstatMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "MTU"), new MetricDetail("Network", "MTU", "packets", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ipkts"), new MetricDetail("Network", "Receive/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Ierrs"), new MetricDetail("Network", "Receive/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Opkts"), new MetricDetail("Network", "Transmit/Packets", "packets", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Oerrs"), new MetricDetail("Network", "Transmit/Errors", "errors", metricTypes.DELTA, 1));
		allMetrics.put(CommandMetricUtils.mungeString("netstat", "Coll"), new MetricDetail("Network", "Collisions", "packets", metricTypes.DELTA, 1));
		
		/*
		 * Parser & declaration for 'ps' command
		 */
		HashMap<Pattern, String[]> psMapping = new HashMap<Pattern, String[]>();
		psMapping.put(Pattern.compile("(\\d+)\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+"
				+ "(\\d+)\\s+\\d+\\s+\\d+\\s+\\d+([0-9\\.]+)\\s+([0-9\\.]+)\\s+(.*)"),
				new String[]{"RSS", "%CPU", "%MEM", kColumnMetricProcessName});
			
		// psMapping.put(Pattern.compile("([^\\s]+)\\s+([0-9\\.]+)\\s+([0-9\\.]+)\\s+(\\d+)"),
		//	new String[]{kColumnMetricPrefixCount, "%CPU", "%MEM", "RSS"});
		allCommands.put("ps", new UnixCommand(new String[]{"ps", "vewww"}, 
			commandTypes.REGEXDIM, defaultignores, 0, psMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("ps", kColumnMetricProcessName), new MetricDetail("Processes", "Instance Count", "processes", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%CPU"), new MetricDetail("Processes", "Aggregate CPU", "percent", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "%MEM"), new MetricDetail("Processes", "Aggregate Memory", "percent", metricTypes.INCREMENT, 1));
		allMetrics.put(CommandMetricUtils.mungeString("ps", "RSS"), new MetricDetail("Processes", "Aggregate Resident Size", "kb", metricTypes.INCREMENT, 1));
		
		/*
		 * Parser & declaration for 'svmon' command
		 */
		HashMap<Pattern, String[]> svmonMapping = new HashMap<Pattern, String[]>();
		svmonMapping.put(Pattern.compile("\\s*(memory)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"),
			new String[]{kColumnMetricPrefix, "size", "inuse", "free", "pin", "virtual", "available"});
		svmonMapping.put(Pattern.compile("\\s*(pg\\s{1}space)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "size", "inuse"});
		svmonMapping.put(Pattern.compile("\\s*(pin)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "pin_work", "pin_pers", "pin_clnt", "pin_other"});
		svmonMapping.put(Pattern.compile("\\s*(in\\s{1}use)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)"),
			new String[]{kColumnMetricPrefix, "inuse_work", "inuse_pers", "inuse_clnt"});
		allCommands.put("svmon", new UnixCommand(new String[]{"svmon","-Ounit=KB"}, commandTypes.REGEXDIM, defaultignores, 0, svmonMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "size"), new MetricDetail("MemoryDetailed", "Physical/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "inuse"), new MetricDetail("MemoryDetailed", "Physical/Total In Use", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "free"), new MetricDetail("MemoryDetailed", "Physical/Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "pin"), new MetricDetail("MemoryDetailed", "Pinned/Total", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "virtual"), new MetricDetail("Memory", "Used", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "available"), new MetricDetail("Memory", "Free", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "pin_work"), new MetricDetail("MemoryDetailed", "Pinned/Working", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "pin_pers"), new MetricDetail("MemoryDetailed", "Pinned/Persistent", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "pin_clnt"), new MetricDetail("MemoryDetailed", "Pinned/Client", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "pin_other"), new MetricDetail("MemoryDetailed", "Pinned/Other", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "inuse_work"), new MetricDetail("MemoryDetailed", "Physical/Working", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "inuse_pers"), new MetricDetail("MemoryDetailed", "Physical/Persistent", "kb", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("svmon", "inuse_clnt"), new MetricDetail("MemoryDetailed", "Physical/Client", "kb", metricTypes.NORMAL, 1));
		
		/*
		 * Parser & declaration for 'uptime' command
		 */
		HashMap<Pattern, String[]> uptimeMapping = new HashMap<Pattern, String[]>();
		uptimeMapping.put(Pattern.compile(".*load average:\\s+([0-9\\.]+),\\s+([0-9\\.]+),\\s+([0-9\\.]+)"),
			new String[]{"la1", "la5", "la15"});
		allCommands.put("uptime", new UnixCommand(new String[]{"uptime"}, commandTypes.REGEXDIM, defaultignores, 0, uptimeMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("uptime", "la1"), new MetricDetail("LoadAverage", "1 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("uptime", "la5"), new MetricDetail("LoadAverage", "5 Minute", "load", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("uptime", "la15"), new MetricDetail("LoadAverage", "15 Minute", "load", metricTypes.NORMAL, 1));
		
		HashMap<Pattern, String[]> vmstatMapping = new HashMap<Pattern, String[]>();
		vmstatMapping.put(Pattern.compile("\\s*(\\d+)\\s+(.*)"), new String[]{kColumnMetricName, kColumnMetricValue});	
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat", "-s"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatMapping));
		
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "total address trans. faults"),new MetricDetail("Faults", "Address Translation Faults", "faults", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "page ins"),new MetricDetail("Page", "VMM Page-Ins", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "page outs"),new MetricDetail("Page", "VMM Page-Outs", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "zero filled pages faults"),new MetricDetail("Faults", "Zero-filled Pages Faults", "faults", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "executable filled pages faults"),new MetricDetail("Faults", "Executable Filled Pages Faults", "faults", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pages examined by clock"),new MetricDetail("Page", "Examined by clock", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pages freed by the clock"),new MetricDetail("Page", "Freed by clock", "pages", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cpu context switches"),new MetricDetail("Faults", "CPU Context Switches", "switches", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "device interrupts"),new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "software interrupts"),new MetricDetail("Faults", "Software Interrupts", "interrupts", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "decrementer interrupts"),new MetricDetail("Faults", "Decrementer Interrupts", "interrupts", metricTypes.DELTA, 1));
        allMetrics.put(CommandMetricUtils.mungeString("vmstat", "syscalls"),new MetricDetail("Faults", "System Calls", "calls", metricTypes.DELTA, 1));
		
        HashMap<Pattern, String[]> vmstatThreadMapping = new HashMap<Pattern, String[]>();
        vmstatThreadMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"),
				 new String[]{"r","b","p","w"});
		allCommands.put("VmstatThreads", new UnixCommand(new String[]{"vmstat", "-I", "-W", "1", "1"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatThreadMapping));
		
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreads", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreads", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreads", "p"), new MetricDetail("KernelThreads", "Waiting for Device IO", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("VmstatThreads", "w"), new MetricDetail("KernelThreads", "Waiting for File IO", "threads", metricTypes.NORMAL, 1));
		
        /*
         * OLD vmstat command
         * replaced by "vmstat -s" with deltas to find per-interval values
         * and "vmstat -IW 1 1" for Kernel Thread values
        HashMap<Pattern, String[]> vmstatMapping = new HashMap<Pattern, String[]>();
		vmstatMapping.put(Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)" +
			"\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"),
			new String[]{"r","b","avm","fre","re","pi","po","fr","sr","cy","in","sy","cs"});
		allCommands.put("vmstat", new UnixCommand(new String[]{"vmstat", kExecutionDelay, "1"}, commandTypes.REGEXDIM, defaultignores, 0, vmstatMapping));
	
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "r"), new MetricDetail("KernelThreads", "Runnable", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "b"), new MetricDetail("KernelThreads", "In Wait Queue", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "avm"), new MetricDetail("Memory", "Active", "pages", metricTypes.NORMAL, getPageSize()));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "fre"), new MetricDetail("Memory", "Free", "pages", metricTypes.NORMAL, getPageSize()));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "re"), new MetricDetail("Page", "Reclaimed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "pi"), new MetricDetail("Page", "Paged In", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "po"), new MetricDetail("Page", "Paged Out", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "fr"), new MetricDetail("Page", "Freed", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sr"), new MetricDetail("Page", "Scanned By Page-Replacement", "pages", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cy"), new MetricDetail("Page", "Clock Cycles By Page-Replacement", "cycles", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "in"), new MetricDetail("Faults", "Device Interrupts", "interrupts", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "sy"), new MetricDetail("Faults", "System Calls", "threads", metricTypes.NORMAL, 1));
		allMetrics.put(CommandMetricUtils.mungeString("vmstat", "cs"), new MetricDetail("Faults", "Context Switches", "switches", metricTypes.NORMAL, 1));
		*/
		
		allCommands.put("VmstatTotals", new UnixCommand(new String[]{"vmstat", "-s"}, commandTypes.SIMPLEDIM, defaultignores));
	}
}
