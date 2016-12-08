package com.ldw.music.utils;

public class PackageInfo {
	private Long to;
	private String msg;
	private Long from;
	private String type;
	private String app;

	public PackageInfo() {
	}
	
	public PackageInfo(Long to, String msg, Long from,String type,String app) {
		super();
		this.to = to;
		this.msg = msg;
		this.from = from;
		this.type = type;
		this.setApp(app);
	}
	public Long getTo() {
		return to;
	}
	public void setTo(Long to) {
		this.to = to;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Long getFrom() {
		return from;
	}
	public void setFrom(Long from) {
		this.from = from;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	
}
