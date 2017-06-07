package com.ldw.music.utils;

public class HttpRequestParam {

	public String key;
	public String value;
	
	public HttpRequestParam(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public static HttpRequestParam put(String key, String value) {
		return new HttpRequestParam(key, value);
	}
	
}
