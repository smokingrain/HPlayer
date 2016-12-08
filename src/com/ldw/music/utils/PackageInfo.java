package com.ldw.music.utils;

public class PackageInfo {
	private String to;
	private String msg;
	private String from;
	private String type;

	public PackageInfo() {
		super();
	}
	public PackageInfo(String to, String msg, String from) {
		super();
		this.to = to;
		this.msg = msg;
		this.from = from;
	}
	public PackageInfo(String to, String msg, String from,String type) {
		super();
		this.to = to;
		this.msg = msg;
		this.from = from;
		this.type = type;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
