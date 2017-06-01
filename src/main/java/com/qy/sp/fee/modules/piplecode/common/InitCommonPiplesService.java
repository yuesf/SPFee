package com.qy.sp.fee.modules.piplecode.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dao.TPipleModuleDao;
import com.qy.sp.fee.dto.TPipleModule;
import com.qy.sp.fee.modules.piplecode.base.ChannelManager;
import com.qy.sp.fee.modules.piplecode.common.CommonChannelService.Filter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//@Service
public class InitCommonPiplesService implements BeanFactoryAware {
	private static Logger logger = LoggerFactory.getLogger(InitCommonPiplesService.class);
	private BeanFactory factory;
	@Resource
	private TPipleModuleDao tPipleModuleDao;
//	@PostConstruct
	public void init(){
//		List<TPipleModule> modules = tPipleModuleDao.selectAllOpenPipleModule();
//		for(TPipleModule module : modules){
//			initPiple(module);
//		}
	}
	public void initPiple(TPipleModule module){
		if(module != null){
			CommonChannelService service1 = (CommonChannelService)factory.getBean(module.getModuleName());
//			service1.setPipleId(module.getPipleId());
			service1.setPaySuccessHttpDataType(module.getPaySuccessHttpDataType());
			//请求短信通道
			if(StringUtil.isNotEmptyString(module.getGetSmsPipleExtMap())){
				Map<String,String> getSmsPipleExtMap = new ConcurrentHashMap<String, String>();
				JSONObject getSmsPipleExtJson = JSONObject.fromObject(module.getGetSmsPipleExtMap());
				for(Object key : getSmsPipleExtJson.keySet()){
					if(key instanceof String){
						getSmsPipleExtMap.put((String)key, getSmsPipleExtJson.optString((String)key));
					}
				}
				service1.setGetSmsPipleExtMap(getSmsPipleExtMap);
			}
			if(StringUtil.isNotEmptyString(module.getGetSmsPipleValueMap())){
				Map<String,String> getSmsPipleValueMap = new ConcurrentHashMap<String, String>();
				JSONObject getSmsPipleValueJson = JSONObject.fromObject(module.getGetSmsPipleValueMap());
				for(Object key : getSmsPipleValueJson.keySet()){
					if(key instanceof String){
						getSmsPipleValueMap.put((String)key, getSmsPipleValueJson.optString((String)key));
					}
				}
				service1.setGetSmsPipleValueMap(getSmsPipleValueMap);
			}
			if(StringUtil.isNotEmptyString(module.getGetSmsPipleValueFilterMap())){
				Map<String,List<Filter>> getSmsPipleValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
				JSONObject getSmsPipleValueFilterJson = JSONObject.fromObject(module.getGetSmsPipleValueFilterMap());
				for(Object key : getSmsPipleValueFilterJson.keySet()){
					if(key instanceof String){
						JSONArray filtersArray = getSmsPipleValueFilterJson.optJSONArray((String)key);
						List<Filter> filters = new  ArrayList<Filter>();
						for(int i =0 ;i < filtersArray.size();i++){
							try{
								JSONObject filterJson = filtersArray.getJSONObject(i);
								String clazzStr = filterJson.optString("@class");
								Class clazz = Class.forName(clazzStr);
								Object filter = JSONObject.toBean(filterJson,clazz);
								if(filter != null && filter instanceof Filter){
									filters.add((Filter)filter);
								}
							}catch(Exception e){
								e.printStackTrace();
								logger.error(StringUtil.getExceptionStackString(e));
							}
						}
						getSmsPipleValueFilterMap.put((String)key, filters);
					}
				}
				service1.setGetSmsPipleValueFilterMap(getSmsPipleValueFilterMap);
				
			}
			if(StringUtil.isNotEmptyString(module.getGetSmsPipleExtOrderInfos())){
				List<String> getSmsPipleExtOrderInfos = new ArrayList<String>();
				JSONArray getSmsPipleExtOrderInfosJsonArray = JSONArray.fromObject(module.getGetSmsPipleExtOrderInfos());
				for(int i=0 ;i < getSmsPipleExtOrderInfosJsonArray.size() ;i++){
					Object o = getSmsPipleExtOrderInfosJsonArray.get(i);
					if(o instanceof String){
						getSmsPipleExtOrderInfos.add((String)o);
					}
				}
				service1.setGetSmsPipleExtOrderInfos(getSmsPipleExtOrderInfos);
			}
			//请求短信返回
			if(StringUtil.isNotEmptyString(module.getGetSmsResultPlatformValueMap())){
				Map<String,String> getSmsResultPlatformValueMap = new ConcurrentHashMap<String, String>();
				JSONObject getSmsResultPlatformValueJson = JSONObject.fromObject(module.getGetSmsResultPlatformValueMap());
				for(Object key : getSmsResultPlatformValueJson.keySet()){
					if(key instanceof String){
						getSmsResultPlatformValueMap.put((String)key, getSmsResultPlatformValueJson.optString((String)key));
					}
				}
				service1.setGetSmsResultPlatformValueMap(getSmsResultPlatformValueMap);
			}
			if(StringUtil.isNotEmptyString(module.getGetSmsResultPlatformValueFilterMap())){
				Map<String,List<Filter>> getSmsResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
				JSONObject getSmsResultPlatformValueFilterJson = JSONObject.fromObject(module.getGetSmsResultPlatformValueFilterMap());
				for(Object key : getSmsResultPlatformValueFilterJson.keySet()){
					if(key instanceof String){
						JSONArray filtersArray = getSmsResultPlatformValueFilterJson.optJSONArray((String)key);
						List<Filter> filters = new  ArrayList<Filter>();
						for(int i =0 ;i < filtersArray.size();i++){
							try{
								JSONObject filterJson = filtersArray.getJSONObject(i);
								String clazzStr = filterJson.optString("@class");
								Class clazz = Class.forName(clazzStr);
								Object filter = JSONObject.toBean(filterJson,clazz);
								if(filter != null && filter instanceof Filter){
									filters.add((Filter)filter);
								}
							}catch(Exception e){
								e.printStackTrace();
								logger.error(StringUtil.getExceptionStackString(e));
							}
						}
						getSmsResultPlatformValueFilterMap.put((String)key, filters);
					}
				}
				service1.setGetSmsResultPlatformValueFilterMap(getSmsResultPlatformValueFilterMap);
			}
			if(StringUtil.isNotEmptyString(module.getGetSmsResultExtOrderInfos())){
				List<String> getSmsResultExtOrderInfos = new ArrayList<String>();
				JSONArray getSmsResultExtOrderInfosJsonArray = JSONArray.fromObject(module.getGetSmsResultExtOrderInfos());
				for(int i=0 ;i < getSmsResultExtOrderInfosJsonArray.size() ;i++){
					Object o = getSmsResultExtOrderInfosJsonArray.get(i);
					if(o instanceof String){
						getSmsResultExtOrderInfos.add((String)o);
					}
				}
				service1.setGetSmsResultExtOrderInfos(getSmsResultExtOrderInfos);
			}
			//成功同步
			if(StringUtil.isNotEmptyString(module.getProcessPipleExtMap())){
				Map<String,String> processSuccessPipleExtMap = new ConcurrentHashMap<String, String>();
				JSONObject processSuccessPipleExtJson = JSONObject.fromObject(module.getProcessPipleExtMap());
				for(Object key : processSuccessPipleExtJson.keySet()){
					if(key instanceof String){
						processSuccessPipleExtMap.put((String)key, processSuccessPipleExtJson.optString((String)key));
					}
				}
				service1.setProcessSuccessPipleExtMap(processSuccessPipleExtMap);
			}
			if(StringUtil.isNotEmptyString(module.getProcessPlatformValueMap())){
				Map<String,String> processSuccessResultPlatformValueMap = new ConcurrentHashMap<String, String>();
				JSONObject processSuccessResultPlatformValueJson = JSONObject.fromObject(module.getProcessPlatformValueMap());
				for(Object key : processSuccessResultPlatformValueJson.keySet()){
					if(key instanceof String){
						processSuccessResultPlatformValueMap.put((String)key, processSuccessResultPlatformValueJson.optString((String)key));
					}
				}
				service1.setProcessSuccessResultPlatformValueMap(processSuccessResultPlatformValueMap);
			}
			if(StringUtil.isNotEmptyString(module.getProcessPlatformValueFilterMap())){
				Map<String,List<Filter>> processSuccessResultPlatformValueFilterMap = new ConcurrentHashMap<String, List<Filter>>();
				JSONObject processSuccessResultPlatformValueFilterJson = JSONObject.fromObject(module.getProcessPlatformValueFilterMap());
				for(Object key : processSuccessResultPlatformValueFilterJson.keySet()){
					if(key instanceof String){
						JSONArray filtersArray = processSuccessResultPlatformValueFilterJson.optJSONArray((String)key);
						List<Filter> filters = new  ArrayList<Filter>();
						for(int i =0 ;i < filtersArray.size();i++){
							try{
								JSONObject filterJson = filtersArray.getJSONObject(i);
								String clazzStr = filterJson.optString("@class");
								Class clazz = Class.forName(clazzStr);
								Object filter = JSONObject.toBean(filterJson,clazz);
								if(filter != null && filter instanceof Filter){
									filters.add((Filter)filter);
								}
							}catch(Exception e){
								e.printStackTrace();
								logger.error(StringUtil.getExceptionStackString(e));
							}
						}
						processSuccessResultPlatformValueFilterMap.put((String)key, filters);
					}
				}
				service1.setProcessSuccessPlatformValueFilterMap(processSuccessResultPlatformValueFilterMap);
			}
			
			if(StringUtil.isNotEmptyString(module.getProcessExtOrderInfos())){
				List<String> processSuccessExtOrderInfos = new ArrayList<String>();
				JSONArray processSuccessExtOrderInfosJsonArray = JSONArray.fromObject(module.getProcessExtOrderInfos());
				for(int i=0 ;i < processSuccessExtOrderInfosJsonArray.size() ;i++){
					Object o = processSuccessExtOrderInfosJsonArray.get(i);
					if(o instanceof String){
						processSuccessExtOrderInfos.add((String)o);
					}
				}
				service1.setProcessSuccessExtOrderInfos(processSuccessExtOrderInfos);
			}
			ChannelManager.getInstance().putChannelService(service1.getPipleId(), service1);
		}
	}
	public String initPiple(String pipleId){
		try{
			TPipleModule module = tPipleModuleDao.selectByPrimaryKey(pipleId);
			if(module != null){
				initPiple(module);
				return "ok";
			}
		}catch(Exception e){
			logger.error(StringUtil.getExceptionStackString(e));
		}
		return "error";
		
	}
	public String saveModule(JSONObject object){
		try{
			String pipleId = object.optString("pipleId");
			TPipleModule module = tPipleModuleDao.selectByPrimaryKey(pipleId);
			if(module == null){
				module = (TPipleModule)JSONObject.toBean(object,TPipleModule.class);
				tPipleModuleDao.insertSelective(module);
			}else{
				module = (TPipleModule)JSONObject.toBean(object,TPipleModule.class);
				tPipleModuleDao.updateByPrimaryKeySelective(module);
			}
			return "ok";
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return "error";
	}
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		factory = beanFactory;
	}
}
