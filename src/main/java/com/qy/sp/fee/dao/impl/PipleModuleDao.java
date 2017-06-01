package com.qy.sp.fee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qy.sp.fee.dao.MyBatisRepository;
import com.qy.sp.fee.dto.TPipleModule;

@Component @MyBatisRepository
public interface PipleModuleDao {
    int deleteByPrimaryKey(String pipleId);

    int insertSelective(TPipleModule pipleModule);

    TPipleModule selectByPrimaryKey(String pipleId);

    int updateByPrimaryKeySelective(TPipleModule pipleModule);

    List<TPipleModule> selectAllOpenPipleModule();
    
}