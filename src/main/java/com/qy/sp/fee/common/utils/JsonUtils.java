package com.qy.sp.fee.common.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.JsonSyntaxException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * JSON处理工具�?.
 *
 * @author <a href="http://www.jiangzezhou.com">jiangzezhou</a>
 * @version 1.0.0.0, 6/16/15 09::55
 */
public final class JsonUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonUtils.class.getName());

    private JsonUtils() {
    }

    public static Map<String, String> jsonMapString(final String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        try {
        	JSONObject json = JSONObject.fromObject(jsonStr);
            Iterator it = json.keys();
            while (it.hasNext()) {
                final String key = (String) it.next();
                map.put(key, String.valueOf(json.get(key)));
            }
        } catch (Exception e) {
            LOGGER.info("json to map String error!!");
        }
        return map;
    }
    public static<T> String list2Json(final List<T> list){
    	return JSONArray.fromObject(list).toString();
    }
    public static<T> List<T> json2List(String jsonStr,Class<T> clazz){
    	 List<T> retList = new ArrayList<T>();
         try {
        	 JSONArray array = JSONArray.fromObject(jsonStr);
        	 for(int i=0 ;i < array.size() ;i ++){
        		 JSONObject object = array.getJSONObject(i);
        		 T t = (T)JSONObject.toBean(object,clazz);
        		 retList.add(t);
        	 }
         } catch (JsonSyntaxException e) {
             LOGGER.info("json2Bean error" + e.getMessage());
         }
         return retList;
    }
    public static <T> T json2Bean(final String jsonStr, Class<T> clazz) {
        T t = null;
        try {
            t = (T)JSONObject.toBean(JSONObject.fromObject(jsonStr), clazz);
        } catch (JsonSyntaxException e) {
            LOGGER.info("json2Bean error" + e.getMessage());
        }
        return t;
    }

    public static <T> String bean2Json(final T t) {
        String jsonStr = null;
        try {
            jsonStr = JSONObject.fromObject(t).toString();
        } catch (JsonSyntaxException e) {
            LOGGER.info("json2Bean error" + e.getMessage());
        }
        return jsonStr;
    }

    /**
     * 将map结构转为key=value&key=value格式.
     *
     * @param params map
     * @return format str
     */
    public static String map2KVStr(final Map<String, String> params,
                                   final boolean urlEncode) {
        final StringBuilder sb = new StringBuilder();
        final Set<Map.Entry<String, String>> entrySet = params.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String value = entry.getValue();
            if (urlEncode) {
                value = StringUtil.urlEncodeWithUtf8(value);
            }
            sb.append(entry.getKey() + "=" + value)
                    .append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }
	public static JSONArray createJsonArray(JSONObject... objs){
		JSONArray jsonArray = new JSONArray();
		for(JSONObject o : objs){
			jsonArray.add(o);
		}
		return jsonArray;
	}
	public static class JSONBuilder {
	    private JSONObject jsonObject = new JSONObject();
	    public JSONBuilder(){

	    }
	    public JSONBuilder(JSONObject jsonObject){
	    	Iterator iterator = jsonObject.keys();                       // joData是JSONObject对象  
	    	while(iterator.hasNext()){  
	    	    String key = iterator.next() + "";  
	    	    try {
					this.jsonObject.put(key,jsonObject.get(key));
				} catch (Exception e) {
				}
	    	} 	
	    }
	    public JSONBuilder(Map<String,?> map){
	        for(String key :map.keySet()){
	            try {
					this.jsonObject.put(key,map.get(key));
				} catch (Exception e) {
				}
	        }
	    }
	    public JSONBuilder put(String key,Object value){
	        try {
				jsonObject.put(key,value);
			} catch (Exception e) {
			}
	        return this;
	    }
	    public JSONObject build(){
	        return jsonObject;
	    }
	}

}
