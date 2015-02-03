package org.xcom.cat.core;

import java.util.Map;

/**
 * 类加载器节点信息
 * 
 * @author han_feng
 *
 */
public interface CLNode {

    /**
     * 获取唯一标识
     * 
     * @return
     */
    public String getId();

    /**
     * 获取节点类型，取值为对应ClassLoader的className
     * 
     * @return
     */
    public String getType();

    /**
     * 获取父节点
     * 
     * @return
     */
    public CLNode getParent();

    /**
     * 获取对应的ClassLoader
     * 
     * @return
     */
    public ClassLoader getClassLoader();

    /**
     * 是否是根节点
     * 
     * @return
     */
    public boolean isRoot();

    /**
     * 获取根节点
     * 
     * @return
     */
    public CLNode getRoot();

    /**
     * 获取子节点
     * 
     * @return
     */
    public CLNode[] getChildren();

    /**
     * 获取该节点管理的classpath路径
     * 
     * @return
     */
    public String[] getClasspath();

    /**
     * 获取扩展属性
     * 
     * @return 不可变Map对象
     */
    public Map<String, Object> getAttribute();
}
