package com.dracode.andrdce.ct;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TypeUtil {

	public static int ObjectToInt(Object o) {
		return NumberConverter.ObjectToInt(o);
	}

	public static int ObjectToIntDef(Object o, int def) {
		try {
			if (o == null)
				return def;
			else
				return NumberConverter.ObjectToInt(o);
		} catch (Exception e) {
			return def;
		}
	}

	public static long ObjectToLong(Object o) {
		return NumberConverter.ObjectToLong(o);
	}

	public static double ObjectToDouble(Object o) {
		return NumberConverter.ObjectToDouble(o);
	}
	public static String ObjectToString(Object o) {
		if (o == null)
			return null;
		else if (o instanceof String)
			return (String) o;
		else if (o instanceof Date) {
			return DateTimeConverter.DateTimeToStr((Date) o);
		} else
			return o.toString();
	}
	
	public static Date StrToDate(String s){
		return DateTimeConverter.StrToDateTime(s);
	}

	public static java.util.Date CalendarToDate(Calendar cal) {
		int YY = cal.get(Calendar.YEAR);
		int MM = cal.get(Calendar.MONTH) + 1;
		int DD = cal.get(Calendar.DATE);
		int HH = cal.get(Calendar.HOUR);
		int NN = cal.get(Calendar.MINUTE);
		int SS = cal.get(Calendar.SECOND);
		String s = Integer.toString(YY) + "-" + Integer.toString(MM) + "-"
				+ Integer.toString(DD) + " " + Integer.toString(HH) + ":"
				+ Integer.toString(NN) + ":" + Integer.toString(SS);
		return DateTimeConverter.StrToDateTime(s);
	}

	public static class NumberConverter {
		public static String IntToStr(int i) {
			return Integer.toString(i);
		}

		public static double ObjectToDouble(Object o) {
			if (o == null)
				return 0;
			else if ("".equals(o))
				return 0;
			else if ("null".equals(o))
				return 0;
			else if (o instanceof String)
				return Double.parseDouble((String) o);
			else if (o instanceof Number)
				return ((Number) o).doubleValue();
			else
				return Double.parseDouble(o.toString());
		}

		public static int StrToInt(String s) {
			if (s == null)
				return 0;
			else if ("".equals(s))
				return 0;
			else if ("null".equals(s))
				return 0;
			else
				return Integer.parseInt(s);
		}

		public static int ObjectToInt(Object o) {
			if (o == null)
				return 0;
			else if ("".equals(o))
				return 0;
			else if ("null".equals(o))
				return 0;
			else if (o instanceof String)
				return Integer.parseInt((String) o);
			else if (o instanceof Number)
				return ((Number) o).intValue();
			else
				return Integer.parseInt(o.toString());
		}

		public static long ObjectToLong(Object o) {
			if (o == null)
				return 0;
			else if ("".equals(o))
				return 0;
			else if ("null".equals(o))
				return 0;
			else if (o instanceof String)
				return Long.parseLong((String) o);
			else if (o instanceof Number)
				return ((Number) o).longValue();
			else
				return Long.parseLong(o.toString());
		}

		public static String LongToStr(long i) {
			return Long.toString(i);
		}

		public static long StrToLong(String s) {
			if (s == null)
				return 0;
			return Long.parseLong(s);
		}

		public static String FloatToStr(double d) {
			return Double.toString(d);
		}

		public static double StrToFloat(String s) {
			if (s == null)
				return 0;
			return Double.parseDouble(s);
		}
	}

	public static class DateTypeConverter {
		private static final String DATE_FORMAT = "yyyy-MM-dd";

		private static final String DATE_FORMAT2 = "yyyy年MM月dd日";

		private static final String DATE_FORMAT3 = "yyyyMMdd";

		private static final String DATE_FORMAT4 = "yyyy年MM月";

		private static final String DATE_FORMAT5 = "yyyyMM";

		private static final String DATE_FORMAT6 = "yyyy/MM/dd";

		private static final DateFormat format = new SimpleDateFormat(
				DATE_FORMAT);

		private static final DateFormat format2 = new SimpleDateFormat(
				DATE_FORMAT2);

		private static final DateFormat format3 = new SimpleDateFormat(
				DATE_FORMAT3);

		private static final DateFormat format4 = new SimpleDateFormat(
				DATE_FORMAT4);

		private static final DateFormat format5 = new SimpleDateFormat(
				DATE_FORMAT5);

		private static final DateFormat format6 = new SimpleDateFormat(
				DATE_FORMAT6);

		public static Date StrToDate(String s) {
			if (s == null)
				return null;
			else if ("".equals(s))
				return null;
			else if ("null".equals(s))
				return null;
			try {
				java.util.Date date = format.parse(s);
				return new Date(date.getTime());
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format2.parse(s);
				return new Date(date.getTime());
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format3.parse(s);
				return new Date(date.getTime());
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format4.parse(s);
				return new Date(date.getTime());
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format5.parse(s);
				return new Date(date.getTime());
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format6.parse(s);
				return new Date(date.getTime());
			} catch (ParseException e) {
				throw new RuntimeException(s + " 日期格式错误：日期格式应为“'" + DATE_FORMAT
						+ "”、“" + DATE_FORMAT2 + "”、“" + DATE_FORMAT4 + "或“"
						+ DATE_FORMAT3
						+ "，例如“2008-01-01”，“2008年01月01日”，“2008年01月”，“20080101”");
			}
		}

		public static String DateToStr(Date dt) {
			return format.format(dt);
		}
	}

	public static class DateTimeConverter {

		private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

		private static final String DATE_FORMAT2 = "yyyy/MM/dd HH:mm:ss";

		private static final String DATE_FORMAT3 = "yyyy年MM月dd日 HH时mm分ss秒";

		private static final String DATE_FORMAT4 = "yyyy年MM月dd日 HH时mm分";

		private static final String DATE_FORMAT5 = "yyyy年MM月dd日 HH:mm";

		private static final String DATE_FORMAT6 = "yyyy-MM-dd HH:mm";

		private static final String DATE_FORMAT7 = "yyyy年MM月dd日";

		private static final String DATE_FORMAT8 = "yyyy-MM-dd";

		private static final String DATE_FORMAT9 = "yyyy/MM/dd";

		private static final String DATE_FORMAT10 = "yyyyMMdd";

		private static final String DATE_FORMAT11 = "yyyyMMdd HH:mm";

		private static final String DATE_FORMAT12 = "yyyyMMdd HH:mm:ss";

		private static final String DATE_FORMAT13 = "HH:mm";

		private static final String DATE_FORMAT14 = "HH:mm:ss";

		private static final String DATE_FORMAT15 = "yyyy年MM月";

		private static final String DATE_FORMAT16 = "yyyyMM";

		private static final DateFormat format = new SimpleDateFormat(
				DATE_FORMAT);

		private static final DateFormat format2 = new SimpleDateFormat(
				DATE_FORMAT2);

		private static final DateFormat format3 = new SimpleDateFormat(
				DATE_FORMAT3);

		private static final DateFormat format4 = new SimpleDateFormat(
				DATE_FORMAT4);

		private static final DateFormat format5 = new SimpleDateFormat(
				DATE_FORMAT5);

		private static final DateFormat format6 = new SimpleDateFormat(
				DATE_FORMAT6);

		private static final DateFormat format7 = new SimpleDateFormat(
				DATE_FORMAT7);

		private static final DateFormat format8 = new SimpleDateFormat(
				DATE_FORMAT8);

		private static final DateFormat format9 = new SimpleDateFormat(
				DATE_FORMAT9);

		private static final DateFormat format10 = new SimpleDateFormat(
				DATE_FORMAT10);

		private static final DateFormat format11 = new SimpleDateFormat(
				DATE_FORMAT11);

		private static final DateFormat format12 = new SimpleDateFormat(
				DATE_FORMAT12);

		private static final DateFormat format13 = new SimpleDateFormat(
				DATE_FORMAT13);

		private static final DateFormat format14 = new SimpleDateFormat(
				DATE_FORMAT14);

		private static final DateFormat format15 = new SimpleDateFormat(
				DATE_FORMAT15);

		private static final DateFormat format16 = new SimpleDateFormat(
				DATE_FORMAT16);

		private static final DateFormat[] formatArray = new DateFormat[] {
				format, format2, format3, format4, format5, format6, format7,
				format8, format9, format10, format11, format12, format13,
				format14, format15, format16 };

		public static String DateTimeToStr(Date t) {
			if (t == null)
				return null;
			return format.format(t);
		}

		public static String DateTimeToStr(Date t, String fmt) {
			if (t == null)
				return null;
			for (int i =0;i< formatArray.length;i++){
				if (((SimpleDateFormat)formatArray[i]).toPattern().equalsIgnoreCase(fmt))
					return formatArray[i].format(t);
			}	
			DateFormat datefmt = new SimpleDateFormat(fmt);
			return datefmt.format(t);
		}

		public static Date StrToDateTime(String s) {
			if (s == null)
				return null;
			s = s.trim();
			if ("".equals(s))
				return null;
			else if ("null".equals(s))
				return null;
			try {
				java.util.Date date = format.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format2.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format3.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format4.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format5.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format6.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format7.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format8.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format9.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format10.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format11.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format12.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format13.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format14.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format15.parse(s);
				return date;
			} catch (ParseException e) {
			}

			try {
				java.util.Date date = format16.parse(s);
				return date;
			} catch (ParseException e) {
				throw new RuntimeException(
						s
								+ " 日期格式错误，日期格式应如以下所示: “2008-01-01 12:00:00”，“2008年01月01日 12时00分00秒”，“2008-01-01”，“20080101 12:00”等。");
				/*throw new RuntimeException(
						s
								+ " 日期格式错误：日期格式应为“'"
								+ DATE_FORMAT
								+ "”、“"
								+ DATE_FORMAT3
								+ "”、“"
								+ DATE_FORMAT5
								+ "”或“"
								+ DATE_FORMAT11
								+ "'”等，例如“2008-01-01 12:00:00”，“2008年01月01日 12时00分00秒”，“2008-01-01”，“20080101 12:00”。");*/
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> CastToMap_SO(Object o)
	{
		return (Map<String, Object>)o;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> CastToMap_SI(Object o)
	{
		return (Map<String, Integer>)o;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> CastToMap_SS(Object o)
	{
		return (Map<String, String>)o;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> CastToList_SO(Object o)
	{
		return (List<Map<String, Object>>)o;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> CastToList_SS(Object o)
	{
		return (List<Map<String, String>>)o;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<?,?>> CastToList_M(Object o)
	{
		return (List<Map<?,?>>)o;
	}

	@SuppressWarnings("unchecked")
	public static List<String> CastToList_S(Object o)
	{
		return (List<String>)o;
	}
	
	/**
	 * 根据格式，获取日期字符串.
	 * @param dateTime 日期
	 * @param pattern 格式，如yyyy-MM-dd
	 */
	public static String formatDate(Date dateTime, String pattern) {
		SimpleDateFormat f = new SimpleDateFormat(pattern);
		return f.format(dateTime);
	}
	
	/**
	 * 根据格式，字符串转换为日期类型.
	 * @param dateTime
	 * @param pattern 格式，如yyyy-MM-dd HH:mm:ss
	 */
	public static Date parseDate(String dateTime, String pattern) {
		Date curdate = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			curdate = sdf.parse(dateTime);
		} catch (Exception ex) {
		}
		return curdate;
	}
	
	/**
	 * 获取时间相差秒数.
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return
	 */
	public static long getMarginSec(Date startDate, Date endDate) {
		long margin = 0;
		
		margin = (endDate.getTime() - startDate.getTime()) / 1000;
		
		return margin;
	}
	/**
	 * 根据有效位获取值
	 * @param value 数值
	 * @param places 小数点后有效位数
	 */
	public static float getNumByPlaces(float value, int places) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
		
		return bd.floatValue();
	}
	
	/**
	 * 对double数据进行精度控制
	 * @param value
	 * @param scale 精确小数位后几位
	 * @param roundingMode 如BigDecimal.ROUND_HALF_UP
	 */
	public static double round(double value, int scale, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        double d = bd.doubleValue();
        bd = null;
        return d;
    }
	
	/**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     */
	public static double getDisByCoord(double lng1, double lat1, double lng2, double lat2) {
		// 把经纬度转成SANSON投影坐标
		double Lo = lng1;
		double La = lat1;
		La = La / 180 * 3.1415926535;
		Lo = Lo - 113;
		if (Lo < -180) {
			Lo = Lo + 360;
		}
		Lo = Lo / 180 * 3.1415926535;
		double x1 = 6378140 * Lo * Math.cos(La);
		double y1 =  6378140 * La;
		// 把经纬度转成SANSON投影坐标
		Lo = lng2;
		La = lat2;
		La = La / 180 * 3.1415926535;
		Lo = Lo - 113;
		if (Lo < -180) {
			Lo = Lo + 360;
		}
		Lo = Lo / 180 * 3.1415926535;
		double x2 = 6378140 * Lo * Math.cos(La);
		double y2 =  6378140 * La;
		// 用勾股定理d = sqrt(x * x + y * y)求距离
		double dis = Math.sqrt((x2 - x1)* (x2 - x1) + (y2 - y1) * (y2 - y1));
		
		return dis;
	}
	
}
