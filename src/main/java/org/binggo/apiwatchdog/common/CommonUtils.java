package org.binggo.apiwatchdog.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public final class CommonUtils {
	
	public static final List<Object> EMPTY_LIST = Lists.newArrayList();
	
	
	public static final DateFormat DATE_COMPACT_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final DateFormat DATE_NORMAL_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	private static Pattern linePattern = Pattern.compile("_(\\w)");
	
	// transform a string from line format to hump format
	public static String lineToHump(String str){  
        str = str.toLowerCase();  
        Matcher matcher = linePattern.matcher(str);  
        StringBuffer sb = new StringBuffer();  
        while(matcher.find()){  
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());  
        }  
        matcher.appendTail(sb);  
        return sb.toString();  
    } 

}
