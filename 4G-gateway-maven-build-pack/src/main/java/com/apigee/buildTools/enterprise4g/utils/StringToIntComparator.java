package com.apigee.buildTools.enterprise4g.utils;

import java.util.Comparator;

public class StringToIntComparator implements Comparator<String> {

	public int compare(String o1, String o2) {
		return (Integer.parseInt(o1) > Integer.parseInt(o2) ? -1
				: (o1 == o2 ? 0 : 1));
	}

}
