/**
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                 佛祖保佑                                      永无BUG
 * @author xiaokui
 * @版本 ：v1.0
 * @时间：2016-5-2上午10:44:33
 */
package com.ldw.music.utils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ldw.music.lrc.LrcInfo;

/**
 * @项目名称：MusicParser
 * @类名称：SongSeacher.java
 * @类描述：
 * @创建人：xiaokui
 * 时间：2016-5-2上午10:44:33
 */
public class SongSeacher {

	
	public static String fromCharCodes(String[]codes){
		if(null==codes){
			return "";
		}
		StringBuilder builder=new StringBuilder();
		for(String code:codes){
			int intValue=Integer.parseInt(code);
			char chr=(char) intValue;
			builder.append(chr);
		}
		return builder.toString();
	}
	
	public static String getArtistFromKuwo(String name){
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
	
	public static List<SearchInfo> getLrcFromKuwo(String name){
		List<SearchInfo> lrcs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=music&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return lrcs;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("li[class=clearfix]");
			for(Element ele:eles){
				SearchInfo info=new SearchInfo();
				lrcs.add(info);
				Elements names=ele.getElementsByAttributeValue("class", "m_name");
				for(Element nameP:names){
					Elements as=nameP.getElementsByTag("a");
					for(Element a:as){
						info.name=a.attr("title");
						info.url=a.attr("href");
						break;
					}
					break;
				}
				Elements singers=ele.getElementsByAttributeValue("class", "s_name");
				for(Element singer:singers){
					Elements as=singer.getElementsByTag("a");
					for(Element a:as){
						info.singer=a.attr("title");
						break;
					}
					break;
				}
			}
		}
		return lrcs;
		
	}
	
	public static LrcInfo perseFromHTML(String html){
		Document doc=Jsoup.parse(html);
		Elements lrcs=doc.select("p[class=lrcItem]");
		LrcInfo lrc=new LrcInfo();
		Map<Long,String> infos=new HashMap<Long, String>();
		for(Element ele:lrcs){
			String time=ele.attr("data-time");
			double dtime=Double.parseDouble(time);
			long ltime=(long) (dtime*1000);
			String text=ele.text();
			infos.put(ltime, text);
		}
		lrc.setInfos(infos);
		return lrc;
	}
	
	public static List<SearchInfo> getSongFromKuwo(String name){
		return getSongFromKuwo(name, "ape");
	}
	
	public static List<SearchInfo> getSongFromKuwo(String name, String type){
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=music&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku2016";
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		String html=HTTPUtil.getInstance("search").getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("li[class=clearfix]");
			for(Element ele:eles){
				SearchInfo info=new SearchInfo();
				info.type=type;
				songs.add(info);
				Elements names=ele.getElementsByAttributeValue("class", "m_name");
				for(Element nameP:names){
					Elements as=nameP.getElementsByTag("a");
					for(Element a:as){
						info.name=a.attr("title");
						break;
					}
					break;
				}
				Elements albums=ele.getElementsByAttributeValue("class", "a_name");
				for(Element albumP:albums){
					Elements as=albumP.getElementsByTag("a");
					for(Element a:as){
						info.album=a.attr("title");
						break;
					}
					break;
				}
				Elements singers=ele.getElementsByAttributeValue("class", "s_name");
				for(Element singer:singers){
					Elements as=singer.getElementsByTag("a");
					for(Element a:as){
						info.singer=a.attr("title");
						break;
					}
					break;
				}
				Elements numbers=ele.getElementsByAttributeValue("class", "number");
				String download="response=url&type=convert%5Furl&rid=MUSIC%5F{mid}&format="+type;
				String baseHost="http://antiserver.kuwo.cn/anti.s?";
				for(Element number:numbers){
					Elements as=number.getElementsByTag("input");
					for(Element a:as){
						String mid=a.attr("mid");
						String url = baseHost+download.replace("{mid}", mid);
						info.url=url;
						break;
					}
					break;
				}
			}
		}
		
		return songs;
	}
	
	
	public static class SearchInfo implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 314395112714052640L;
		public String url="";
		public String name="";
		public String singer="";
		public String album="";
		public String type = "ape";
	}
	
}
