package com.yitianyike.calendar.appserver.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FilterUtil {

	
	
	/**
	 * 手机号验证
	 * @param param
	 * @return
	 */
	public static boolean telFilter(String param){
		boolean matches = param.matches("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		return matches;
	}
	
	/**
	 * token,uid等服务器生产字符串验证
	 * @param param
	 * @return
	 */
	public static boolean uuidFilter(String param){
		boolean matches = param.matches("((u)|(t))([0-9]{4,50})");
		return matches;
	}
	
	
	/**
	 * 常规非法字符注入判断验证
	 * @param param
	 * @return
	 */
	public static boolean baseFilter(String param) {
		boolean flag = false;
		String a = PropertiesUtil.specialChar;
		String[] split = a.split(",");
		Set<String> wordSet = new HashSet<String>();
		for (String string : split) {
			wordSet.add(string);
		}
		FilterUtil filterUtil = new FilterUtil(wordSet);
		Map<String, Object> map = filterUtil.getDictionaryMap();
//		System.out.println(map);
		int beginIndex = 0;
		int wordLength = filterUtil.checkWord(param, beginIndex);
//		System.out.println(wordLength);
//		System.out.println(filterUtil.getWords(param));
		if(wordLength == 0){
			flag = true;
		}
		return flag;
	}

	private Map<String, Object> dictionaryMap;

	public FilterUtil(Set<String> wordSet) {
		this.dictionaryMap = handleToMap(wordSet);
	}

	public Map<String, Object> getDictionaryMap() {
		return dictionaryMap;
	}

	public void setDictionaryMap(Map<String, Object> dictionaryMap) {
		this.dictionaryMap = dictionaryMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> handleToMap(Set<String> wordSet) {
		if (wordSet == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>(wordSet.size());
		Map<String, Object> curMap = null;
		Iterator<String> ite = wordSet.iterator();
		while (ite.hasNext()) {
			String word = ite.next();
			curMap = map;
			int len = word.length();
			for (int i = 0; i < len; i++) {
				String key = String.valueOf(word.charAt(i));
				Map<String, Object> wordMap = (Map<String, Object>) curMap
						.get(key);
				if (wordMap == null) {
					wordMap = new HashMap<String, Object>();
					wordMap.put("isEnd", "0");
					curMap.put(key, wordMap);
					curMap = wordMap;
				} else {
					curMap = wordMap;
				}
				if (i == len - 1) {
					curMap.put("isEnd", "1");
				}
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public int checkWord(String text, int beginIndex) {
		if (dictionaryMap == null) {
			throw new RuntimeException("字典不能为空！");
		}
		boolean isEnd = false;
		int wordLength = 0;
		Map<String, Object> curMap = dictionaryMap;
		int len = text.length();
		for (int i = beginIndex; i < len; i++) {
			String key = String.valueOf(text.charAt(i));
			curMap = (Map<String, Object>) curMap.get(key);
			if (curMap == null) {
				break;
			} else {
				wordLength++;
				if ("1".equals(curMap.get("isEnd"))) {
					isEnd = true;
				}
			}
		}
		if (!isEnd) {
			wordLength = 0;
		}
		return wordLength;
	}

	public Set<String> getWords(String text) {
		Set<String> wordSet = new HashSet<String>();
		int len = text.length();
		for (int i = 0; i < len; i++) {
			int wordLength = checkWord(text, i);
			if (wordLength > 0) {
				String word = text.substring(i, i + wordLength);
				wordSet.add(word);
				i = i + wordLength - 1;
			}
		}
		return wordSet;
	}

}
