package com.chocolatefactory.newrelic.plugins.unix;

public final class NewNixConstants {
	public static final String kCategoryMetricName="Component";
	public static final String kDeltaMetricName="delta";
	public static final String kOverviewMetricName="overview";
	
	public static final String kDefaultMetricName="default";
	public static final double kDefaultMetricRatio=1;
	public static final String kDefaultMetricType="NORMAL";
	public static final String kDefaultMetricUnits="value";
	public static final String kDefaultMetricCategory="Metrics";
	public static final String kDefaultPagesizeCommand="pagesize";
	public static final int    kDefaultLineLimit = 0;
	public static final char   kMetricTreeDivider='/';

	public static final String kColumnMetricDiskName = "DISK_NAME";
	public static final String kColumnIgnore = "IGNORE_COLUMN";
	public static final String kMemberPlaceholder = "MEMBER_PLACEHOLDER";
	public static final String kColumnMetricName = "METRIC_NAME";
	public static final String kColumnMetricPrefix = "METRIC_PREFIX";
	public static final String kColumnMetricValue = "METRIC_VALUE";
	public static final String kColumnMetricProcessName = "PROCESS_NAME";
	public static final String kColumnPageSize = "PAGE_SIZE";
}
