package com.ldw.music.utils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.ldw.music.lrc.XRCLine;
import com.ldw.music.utils.SongSeacher.SearchInfo;


public interface IDownloadSource {

	public List<SearchInfo> getLrcs(String name);
	
	public String getSongUrl(SearchInfo info);
	
	public String getMVUrl(SearchInfo info);
	
	public List<XRCLine> getLrc(String url);
	
	public List<SearchInfo> getMV(String name);
	
	public List<SearchInfo> getSong(String name);
	
	public List<SearchInfo> getSong(String name, String type);
	
	public Map<String, String> fastSearch(String name);
	
	public String getArtist(String name);
	
	
	
}
