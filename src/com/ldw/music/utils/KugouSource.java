package com.ldw.music.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ldw.music.lrc.LrcInfo;
import com.ldw.music.lrc.LrcParser;
import com.ldw.music.lrc.XRCLine;
import com.ldw.music.utils.SongSeacher.SearchInfo;


public class KugouSource implements IDownloadSource {

	@Override
	public List<SearchInfo> getLrcs(String name) {
		return Collections.emptyList();
	}

	@Override
	public List<SearchInfo> getMV(String name) {
		List<SearchInfo> songs = new ArrayList<SearchInfo>();
		String searchUrl = null;
		String html = null;
		String callBack = "jQuery19108035724928824395_" + System.currentTimeMillis();
		try {
			searchUrl = "http://mvsearch.kugou.com/mv_search";
			Map<String, String> params = new HashMap<String, String>();
			params.put("callback", callBack);
			params.put("keyword", name);
			params.put("page", "1");
			params.put("pagesize", "30");
			params.put("userid", "-1");
			params.put("clientver", "");
			params.put("platform", "WebFilter");
			params.put("tag", "em");
			params.put("filter", "2");
			params.put("iscorrection", "1");
			params.put("privilege_filter", "0");
			params.put("_", String.valueOf(System.currentTimeMillis()));
			html = HTTPUtil.getInstance("search").getHtml(searchUrl, params);
		} catch (Exception e) {
			return songs;
		}
		if(!StringUtil.isBlank(html)){
			String json = html.substring(callBack.length() + 1, html.length() - 1 );
			Map<String, Object> rst = JSONUtil.fromJson(json);
			Map<String, Object> data = (Map<String, Object>) rst.get("data");
			List<Map<String, Object>> lists = (List<Map<String, Object>>) data.get("lists");
			for(Map<String, Object> minfo : lists) {
				SearchInfo info = new SearchInfo();
				songs.add(info);
				info.type= "mv";
				info.album = "";
				info.singer = (String) minfo.get("SingerName");
				info.url = (String) minfo.get("MvHash");
				info.name = ((String) minfo.get("MvName")).replace("<em>", "").replace("</em>", "");
			}
		}
		return songs;
	}

	@Override
	public List<SearchInfo> getSong(String name) {
		return getSong(name, "mp3");
	}

	@Override
	public List<SearchInfo> getSong(String name, String type) {
		List<SearchInfo> songs = new ArrayList<SearchInfo>();
		String searchUrl = null;
		String html = null;
		String callBack = "jQuery191040681639104150236_" + System.currentTimeMillis();
		try {
			searchUrl = "http://songsearch.kugou.com/song_search_v2";
			Map<String, String> params = new HashMap<String, String>();
			params.put("callback", callBack);
			params.put("keyword", name);
			params.put("page", "1");
			params.put("pagesize", "30");
			params.put("userid", "-1");
			params.put("clientver", "");
			params.put("platform", "WebFilter");
			params.put("tag", "em");
			params.put("filter", "2");
			params.put("iscorrection", "1");
			params.put("privilege_filter", "0");
			params.put("_", String.valueOf(System.currentTimeMillis()));
			html = HTTPUtil.getInstance("search").getHtml(searchUrl, params);
		} catch (Exception e) {
			return songs;
		}
		if(!StringUtil.isBlank(html)){
			String json = html.substring(callBack.length() + 1, html.length() - 1 );
			Map<String, Object> rst = JSONUtil.fromJson(json);
			Map<String, Object> data = (Map<String, Object>) rst.get("data");
			List<Map<String, Object>> lists = (List<Map<String, Object>>) data.get("lists");
			for(final Map<String, Object> minfo : lists) {
				SearchInfo info = new SearchInfo();
				songs.add(info);
				info.extra = "" + minfo.get("AlbumID");
				info.type = "mp3";
				info.album = (String) minfo.get("AlbumName");
				info.singer = (String) minfo.get("SingerName");
				info.name = ((String) minfo.get("SongName")).replace("<em>", "").replace("</em>", "");
				info.url = "" + minfo.get("FileHash");
			}
		}
		return songs;
	}

	@Override
	public Map<String, String> fastSearch(String name) {
		Map<String, String> infos = new HashMap<>(); 
		String url = "http://searchtip.kugou.com/getSearchTip";
		Map<String, String> params = new HashMap<>();
		String callBack = "jQuery191040681639104150236_" + System.currentTimeMillis();
		params.put("callback", callBack);
		params.put("MusicTipCount", "5");
		params.put("MVTipCount", "2");
		params.put("albumcount", "2");
		params.put("keyword", name);
		params.put("_", String.valueOf(System.currentTimeMillis()));
		String html = HTTPUtil.getInstance("search").getHtml(url, params);
		if(!StringUtil.isBlank(html)){
			String json = html.substring(callBack.length() + 1, html.length() - 1 );
			Map<String, Object> rst = JSONUtil.fromJson(json);
			List<Map<String, Object>> tips = (List<Map<String, Object>>) rst.get("data");
			for(Map<String, Object> records : tips) {
				if("".equals(records.get("LableName"))) {
					List<Map<String, Object>> datas = (List<Map<String, Object>>) records.get("RecordDatas");
					for(Map<String, Object> obj : datas) {
						infos.put((String)obj.get("HintInfo"), (String)obj.get("HintInfo"));
					}
				}
			}
		}
		return infos;
	}

	@Override
	public String getArtist(String name) {
		String searchUrl=null;
		try {
			searchUrl="http://sou.kuwo.cn/ws/NSearch?type=artist&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements texts=doc.getElementsByAttribute("lazy_src");
			for(Element ele:texts){
				String alt=ele.attr("alt");
				if(null!=alt&&alt.contains(name.replace(" ", "&nbsp;"))){
					return ele.attr("lazy_src");
				}
			}
		}
		return null;
	}

	@Override
	public List<XRCLine> getLrc(SearchInfo info) {
		return JSONUtil.toBean(info.lrcURL, JSONUtil.getCollectionType(List.class, XRCLine.class));
	}

	@Override
	public String getMVUrl(SearchInfo info) {
		if(info.urlFound) {
			return info.url;
		}
		String md5 = ByteUtil.MD5(info.url + "kugoumvcloud");
		String url = "http://trackermv.kugou.com/interface/index/cmd=100&hash=" + info.url + "&key=" + md5 + "&pid=6&ext=mp4&ismp3=0";
		String rst = HTTPUtil.getInstance("search").getHtml(url);
		Map<String, Object> map = JSONUtil.fromJson(rst);
		info.urlFound = true;
		info.url = (String)((Map<String , Map<String, Object>>)map.get("mvdata")).get("sd").get("downurl");
		return info.url;
	}
	
	@Override
	public String getSongUrl(SearchInfo info) {
		if(info.urlFound) {
			return info.url;
		}
		String url = "http://www.kugou.com/yy/index.php";
		List<HttpRequestParam> params = new ArrayList<HttpRequestParam>();
		params.add(HttpRequestParam.put("r", "play/getdata"));
		params.add(HttpRequestParam.put("hash", info.url));
		params.add(HttpRequestParam.put("album_id", info.extra));
		params.add(HttpRequestParam.put("_", String.valueOf(System.currentTimeMillis())));
		String rst = HTTPUtil.getInstance("search").getHtml(url, params);
		Map<String, Object> map = JSONUtil.fromJson(rst);
		info.urlFound = true;
		info.url = (String) ((Map<String, Object>)map.get("data")).get("play_url");
		String lrcText = (String) ((Map<String, Object>)map.get("data")).get("lyrics");
		int length = (int) ((Map<String, Object>)map.get("data")).get("timelength");
		LrcParser parser = new LrcParser((long) length);
		ByteArrayInputStream bin = new ByteArrayInputStream(lrcText.getBytes());
		LrcInfo linfo;
		try {
			linfo = parser.parser(bin);
			List<XRCLine> lines = parser.parseXRC(linfo);
			info.lrcURL = JSONUtil.toJson(lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return info.url;
	}

}
