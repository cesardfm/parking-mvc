package com.g3.parking.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseService {
    protected final ModelMapper modelMapper;
    protected final Logger log;
    
    // Constructor protegido para las clases hijas
    protected BaseService() {
        this.modelMapper = new ModelMapper();
        this.log = LoggerFactory.getLogger(getClass());
    }

    public <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, targetClass);
    }
}
