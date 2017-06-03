package com.ldw.music.utils;

public class SourceFactory {

	
	public static IDownloadSource getSource(String name) {
		if("kuwo".equals(name)) {
			return new KuwoSource();
		} else if("kugou".equals(name)) {
			return new KugouSource();
		}
		return null;
	}
	
}
