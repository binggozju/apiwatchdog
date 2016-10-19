package org.binggo.apiwatchdog.common;

import java.util.List;

import com.github.pagehelper.PageInfo;

/**
 * PageInfoExt is used to work as the data container with paging information
 * @author Administrator
 *
 */
public class PageInfoExt<T> extends PageInfo<T> {
	
	private static final long serialVersionUID = 2L;

	public PageInfoExt(List<T> list) {
		super(list);
		
	}
	
	// add other fields needed, which will displayed in the response

}
