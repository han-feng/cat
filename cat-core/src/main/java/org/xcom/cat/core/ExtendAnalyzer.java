package org.xcom.cat.core;

/**
 * 类加载器分析器接口
 * 
 * @author han_feng
 *
 */
public interface ExtendAnalyzer {

    public CLNode process(String id, CLNode parent, ClassLoader classloader);

}
