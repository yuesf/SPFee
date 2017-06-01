package com.qy.sp.fee.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.impl.PipleModuleDao;
import com.qy.sp.fee.dao.impl.base.BaseDao;
import com.qy.sp.fee.dto.TPipleModule;

@Component
public class TPipleModuleDao extends BaseDao{
	@Resource
	PipleModuleDao pipleModuleDao;
    public int deleteByPrimaryKey(String pipleId){
    	return pipleModuleDao.deleteByPrimaryKey(pipleId);
    }

    public int insertSelective(TPipleModule pipleModule){
    	return pipleModuleDao.insertSelective(pipleModule);
    }

    public TPipleModule selectByPrimaryKey(String pipleId){
    	return pipleModuleDao.selectByPrimaryKey(pipleId);
    }

    public int updateByPrimaryKeySelective(TPipleModule pipleModule){
    	return pipleModuleDao.updateByPrimaryKeySelective(pipleModule);
    }

    public List<TPipleModule> selectAllOpenPipleModule(){
    	return pipleModuleDao.selectAllOpenPipleModule();
    }
    
}