package com.ldw.music.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ByteUtil {  
	  
    /** 
     * 正向索引 
     * */  
    public static int indexOf(byte[] tag, byte[] src) {  
        return indexOf(tag, src, 1);  
    }  
  
    /** 
     * 获取第index个的位置<br /> 
     * index从1开始 
     * */  
    public static int indexOf(byte[] tag, byte[] src, int index) {  
        return indexOf(tag, src, 1, src.length);  
    }  
  
    /** 
     * 获取第index个的位置<br /> 
     * index从1开始 
     *  
     * */  
    public static int indexOf(byte[] tag, byte[] src, int index, int len) {  
        if (len > src.length) {  
            try {  
                throw new Exception("大于总个数");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        int tagLen = tag.length;  
        byte[] tmp = new byte[tagLen];  
        for (int j = 0; j < len - tagLen + 1; j++) {  
            for (int i = 0; i < tagLen; i++) {  
                tmp[i] = src[j + i];  
            }  
            // 判断是否相等  
            for (int i = 0; i < tagLen; i++) {  
                if (tmp[i] != tag[i])  
                    break;  
                if (i == tagLen - 1) {  
                    return j;  
                }  
            }  
  
        }  
        return -1;  
    }  
  
    /** 
     * 倒序索引<br /> 
     *  
     * */  
    public static int lastIndexOf(byte[] tag, byte[] src) {  
  
        return lastIndexOf(tag, src, 1);  
    }  
  
    /** 
     * 倒序获取第index个的位置<br /> 
     * index从1开始 
     * */  
    public static int lastIndexOf(byte[] tag, byte[] src, int index) {  
        return lastIndexOf(tag, src, src.length);  
    }  
  
    /** 
     * 倒序获取第index个的位置<br /> 
     * index从1开始 
     * */  
    public static int lastIndexOf(byte[] tag, byte[] src, int index, int len) {  
        if (len > src.length) {  
            try {  
                throw new Exception("大于总个数");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        int tagLen = tag.length;  
        byte[] tmp = new byte[tagLen];  
        for (int j = len - tagLen; j >= 0; j--) {  
            for (int i = 0; i < tagLen; i++) {  
                tmp[i] = src[j + i];  
  
            }  
            for (int i = 0; i < tagLen; i++) {  
                if (tmp[i] != tag[i])  
                    break;  
                if (i == tagLen - 1) {  
                    return j;  
                }  
            }  
  
        }  
        return -1;  
    }  
  
    /** 
     * 统计个数 
     * */  
    public static int size(byte[] tag, byte[] src) {  
        int size = 0;  
        int tagLen = tag.length;  
        int srcLen = src.length;  
        byte[] tmp = new byte[tagLen];  
        for (int j = 0; j < srcLen - tagLen + 1; j++) {  
            for (int i = 0; i < tagLen; i++) {  
                tmp[i] = src[j + i];  
            }  
            for (int i = 0; i < tagLen; i++) {  
                if (tmp[i] != tag[i])  
                    break;  
                if (i == tagLen - 1) {  
                    size++;  
                }  
            }  
            // 速度较慢  
            // if (Arrays.equals(tmp, tag)) {  
            // size++;  
            // }  
        }  
        return size;  
    }  
  
    /** 
     * 截取byte[] 
     * */  
    public static byte[] cutBytes(int start, int end, byte[] src) {  
        if (end <= start || start < 0 || end > src.length) {  
            try {  
                throw new Exception("参数错误");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        byte[] tmp = new byte[end - start];  
        for (int i = 0; i < end - start; i++) {  
            tmp[i] = src[start + i];  
        }  
        return tmp;  
    }  
    
    /** 
     * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。 
     * @param src byte[] data 
     * @return hex string 
     */     
    public static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }  
    /** 
     * Convert hex string to byte[] 
     * @param hexString the hex string 
     * @return byte[] 
     */  
    public static byte[] hexStringToBytes(String hexString) {  
        if (hexString == null || hexString.equals("")) {  
            return null;  
        }  
        hexString = hexString.toUpperCase();  
        int length = hexString.length() / 2;  
        char[] hexChars = hexString.toCharArray();  
        byte[] d = new byte[length];  
        for (int i = 0; i < length; i++) {  
            int pos = i * 2;  
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
        }  
        return d;  
    }  
    
    /**
	 * MD5加密算法
	 * 
	 * 说明：32位加密算法
	 * 
	 * @param 待加密的数据
	 * @return 加密结果，全小写的字符串
	 */
	public static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = s.getBytes("utf-8");
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
    
    /** 
     * Convert char to byte 
     * @param c char 
     * @return byte 
     */  
     private static byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
     } 
  
     public static void main(String[] args) {
		String text = null;
		try {
			text = new String(hexStringToBytes("30500201000449304702010002047e2f956702033d14b9020496e503b702045852b3880425617570696d675f373066336439643233333231623332335f313438313831343932303834340201000201000400"), "GB2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(text);
	}
} 