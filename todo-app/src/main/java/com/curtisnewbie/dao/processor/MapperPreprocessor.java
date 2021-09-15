package com.curtisnewbie.dao.processor;

import com.curtisnewbie.dao.Mapper;

/**
 * Preprocessor of mapper
 *
 * @author yongjie.zhuang
 */
public interface MapperPreprocessor {

    /**
     * Preprocess the given mapper and return it
     *
     * @param mapper mapper
     */
    void preprocessMapper(Mapper mapper);

    /**
     * Check whether this preprocessor supports the given mapper
     *
     * @param mapper mapper
     */
    boolean supports(Mapper mapper);

}
