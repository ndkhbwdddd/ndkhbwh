package com.yitianyike.calendar.appserver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapUtil {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> List<K> sortByValueDesc(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, V> entry : list) {
			result.add(entry.getKey());
		}
		return result;
	}
	
	
	public static <K, V extends Comparable<? super V>> List<K> sortByValueAsc(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, V> entry : list) {
			result.add(entry.getKey());
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> List<V> sortByValueDescReturnV(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		List<V> result = new ArrayList<V>();
		for (Map.Entry<K, V> entry : list) {
			result.add(entry.getValue());
		}
		return result;
	}
	
	
	
	public static <K, V extends Comparable<? super V>> List<V> sortByValueAscReturnV(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		List<V> result = new ArrayList<V>();
		for (Map.Entry<K, V> entry : list) {
			result.add(entry.getValue());
		}
		return result;
	}


	public static void main(String[] args) {
		Map<String, Integer> ss = new HashMap<String, Integer>();
		ss.put("1",1);
		ss.put("2",3);
		ss.put("3", 4);
		ss.put("4",6);
		ss.put("5", 12);
		ss.put("6", 99);
		ss.put("7", 24);
		ss.put("8", 1);
		ss.put("9", 2);
		ss.put("10", 3);
		ss.put("11", 4);
		ss.put("12", 0);
		List<Integer> sortByValueDesc = sortByValueAscReturnV(ss);
		for (Integer string : sortByValueDesc) {
			System.out.println(string);
		}

	}

}
