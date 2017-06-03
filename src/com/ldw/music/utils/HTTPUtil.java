package com.ldw.music.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;


public class HTTPUtil {
	public HttpClient httpClient = HttpClientHelper.getHttpClient();
	public static String cid=null;
	private static Map<String,HTTPUtil> maps=new HashMap<String,HTTPUtil>();
	
	public static HTTPUtil getInstance(String name){
		HTTPUtil instance=maps.get(name);
		if(null==instance){
			instance=new HTTPUtil(name);
			maps.put(name, instance);
		}
		return instance;
	}
	
	private HTTPUtil(String name){
		
	}
	
	public static void remove(Object obj){
		maps.remove(obj);
	}
	
	public void close(){
		httpClient.getConnectionManager().shutdown();
		httpClient = null;
	}
	
	public String getHtml(String url, Map<String, String> params) {
		if(null != params) {
			StringBuffer sb = new StringBuffer();
			sb.append("?");
			for(String key : params.keySet()) {
				sb.append(key).append("=").append(params.get(key)).append("&");
			}
			url += sb.toString();
		}
		return getHtml(url);
	}
	
	public String getHtml(String url){
		StringBuffer result=new StringBuffer();
		try {
			HttpGet httppost = new HttpGet(url);  
			httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			HttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					HttpPost httppost1 = new HttpPost(redirect);  
					HttpResponse response1=httpClient.execute(httppost1);  
					HttpEntity entity = response1.getEntity();  
					InputStream instream=entity.getContent();
					BufferedReader br = new BufferedReader(new InputStreamReader(instream,"UTF-8"));  
		            String temp = "";  
		            while ((temp = br.readLine()) != null) {  
		                result.append(temp);  
		            }  
				}
				return null;
			}else if(200==response.getStatusLine().getStatusCode()){
				HttpEntity entity = response.getEntity();  
				InputStream instream=entity.getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(instream,"UTF-8"));  
	            String temp = "";  
	            while ((temp = br.readLine()) != null) {  
	                result.append(temp);  
	            }  
			}
		}  catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
		return result.toString();
	}
	
	public InputStream getInput(String url){
		HttpGet httppost = new HttpGet(url);  
		httppost.addHeader("Connection", "keep-alive");
		httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
		try {
			HttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					return getInput(redirect);
				}
			}else{
				HttpEntity entity = response.getEntity(); 
				return entity.getContent();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	public SongLocation getInputStream(String url){
		try {
			HttpGet httppost = new HttpGet(url);  
			httppost.addHeader("Connection", "keep-alive");
			httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			HttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					HttpPost httppost1 = new HttpPost(redirect);  
					HttpResponse response1=httpClient.execute(httppost1);  
					HttpEntity entity = response1.getEntity();  
			    	SongLocation location=new SongLocation();
			    	location.length=entity.getContentLength();
			    	location.input=entity.getContent();
			    	return location;
				}
				return null;
			}else{
				HttpEntity entity = response.getEntity();  
				SongLocation location=new SongLocation();
		    	location.length=entity.getContentLength();
		    	location.input=entity.getContent();
				return location;
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public String getFileName(HttpResponse response) throws UnsupportedEncodingException {  
        Header contentHeader = response.getFirstHeader("Content-Disposition");  
        String filename = null;  
        if (contentHeader != null) {  
            HeaderElement[] values = contentHeader.getElements();  
            if (values.length == 1) {  
                NameValuePair param = values[0].getParameterByName("filename");  
                if (param != null) {  
                    try {  
                        filename = new String(param.getValue().getBytes("ISO-8859-1"), "GBK");  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }  
        return URLDecoder.decode(filename, "GBK");  
    }  
	
	public String readJsonfromURL(String url,Map<String,String> params) throws ClientProtocolException, IOException{
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if(null!=params){
			Set<String> keys=params.keySet();
			for(String key:keys){
				formparams.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(formparams, "GB2312");  
        
        //新建Http  post请求  
        HttpPost httppost = new HttpPost(url);  
        httppost.setEntity(entity1);  
  
        //处理请求，得到响应  
        HttpResponse response = httpClient.execute(httppost);  
      
        //打印返回的结果  
        HttpEntity entity = response.getEntity();  
          
        StringBuilder result = new StringBuilder();  
        if (entity != null) {  
            InputStream instream = entity.getContent();  
            BufferedReader br = new BufferedReader(new InputStreamReader(instream,"GB2312"));  
            String temp = "";  
            while ((temp = br.readLine()) != null) {  
                result.append(temp);  
            }  
        }  
		return result.toString();
	}
	
	
	public String readJsonfromURL2(String url,Map<String,String> params) throws ClientProtocolException, IOException{
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if(null!=params){
			Set<String> keys=params.keySet();
			for(String key:keys){
				formparams.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(formparams, "UTF-8");  
		
		//新建Http  post请求  
		HttpPost httppost = new HttpPost(url);  
		httppost.setEntity(entity1);  
		
		//处理请求，得到响应  
		HttpResponse response = httpClient.execute(httppost);  
		
		//打印返回的结果  
		HttpEntity entity = response.getEntity();  
		
		StringBuilder result = new StringBuilder();  
		if (entity != null) {  
			InputStream instream = entity.getContent();  
			BufferedReader br = new BufferedReader(new InputStreamReader(instream,"UTF-8"));  
			String temp = "";  
			while ((temp = br.readLine()) != null) {  
				result.append(temp);  
			}  
		}  
		return result.toString();
	}
	
	public String readJsonfromURL3(String url,Map<String,String> params) throws ClientProtocolException, IOException{
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if(null!=params){
			Set<String> keys=params.keySet();
			for(String key:keys){
				formparams.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(formparams, "UTF-8");  
		
		//新建Http  post请求  
		HttpPost httppost = new HttpPost(url);  
		httppost.setEntity(entity1);  
		httppost.setHeader("Connection", "keep-alive");
        httppost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        httppost.setHeader("Accept-Encoding", "gzip, deflate");
        httppost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httppost.setHeader("Host", "kyfw.12306.cn");
        httppost.setHeader("Origin", "https://kyfw.12306.cn");
        httppost.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.99 Safari/537.36");
        httppost.setHeader("X-Requested-With", "XMLHttpRequest");
		//处理请求，得到响应  
        HttpResponse response = httpClient.execute(httppost);  
		
		//打印返回的结果  
		HttpEntity entity = response.getEntity();  
		
		StringBuilder result = new StringBuilder();  
		if (entity != null) {  
			InputStream instream = entity.getContent();  
			BufferedReader br = new BufferedReader(new InputStreamReader(instream,"UTF-8"));  
			String temp = "";  
			while ((temp = br.readLine()) != null) {  
				result.append(temp);  
			}  
		}  
		return result.toString();
	}
	
	public void saveToStream(String url,OutputStream out){
		try {
			HttpGet httppost = new HttpGet(url);  
			httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			HttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					HttpPost httppost1 = new HttpPost(redirect);  
					HttpResponse response1=httpClient.execute(httppost1);  
					HttpEntity entity = response1.getEntity();  
					InputStream instream=entity.getContent();
					byte[]buff=new byte[10240];
					int len=0;
		            while ((len = instream.read(buff, 0, buff.length))>=0) {  
		            	out.write(buff, 0, len);  
		            }  
		            out.flush();
		            out.close();
				}
			}else if(200==response.getStatusLine().getStatusCode()){
				HttpEntity entity = response.getEntity();  
				InputStream instream=entity.getContent();
				byte[]buff=new byte[10240];
				int len=0;
	            while ((len = instream.read(buff, 0, buff.length))>=0) {  
	            	out.write(buff, 0, len);  
	            }  
	            out.flush();
	            out.close();
			}
		}  catch (Exception e) {
			System.out.println(e.getMessage()); 
		}
	}
	
	public static void main(String[]args){
		HTTPUtil l=HTTPUtil.getInstance("");
		String url="http://mobilecdn.kugou.com/new/app/i/krc.php?keyword=%E5%BC%A0%E9%9D%93%E9%A2%96%E3%80%81%E7%8E%8B%E9%93%AE%E4%BA%AE%20-%20%E5%8F%AA%E6%98%AF%E6%B2%A1%E6%9C%89%E5%A6%82%E6%9E%9C&timelength=295000&type=1&cmd=200&hash=6253cbe1069ac2378c0028e93a1afe3f";
		File file=new File("e:/download/只是没有如果.krc");
		try {
			FileOutputStream out=new FileOutputStream(file);
			l.saveToStream(url, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
