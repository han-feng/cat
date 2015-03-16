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
     * @return 唯一标识
     */
    public String getId();

    /**
     * 获取描述信息
     * 
     * @return 描述信息
     */
    public String getDescription();

    /**
     * 获取节点类型，取值为对应ClassLoader的className
     * 
     * @return 节点类型
     */
    public String getType();

    /**
     * 获取父节点
     * 
     * @return 父节点
     */
    public CLNode getParent();

    /**
     * 获取对应的ClassLoader
     * 
     * @return ClassLoader
     */
    public ClassLoader getClassLoader();

    /**
     * 是否根节点
     * 
     * @return 是否根节点
     */
    public boolean isRoot();

    /**
     * 是否叶子节点
     * 
     * @return 是否叶子节点
     */
    public boolean isLeaf();

    /**
     * 获取根节点
     * 
     * @return 根节点
     */
    public CLNode getRootNode();

    /**
     * 获取子节点
     * 
     * @return 子节点
     */
    public CLNode[] getChildren();

    /**
     * 获取该节点管理的classpath
     * 
     * @return 该节点管理的classpath
     */
    public String[] getClasspath();

    /**
     * 获取标签集
     * 
     * @return 标签集
     */
    public String[] getTags();

    /**
     * 添加标签
     * 
     * @param tag
     */
    public void addTag(String tag);

    /**
     * 获取扩展属性
     * 
     * @return 不可变Map对象
     */
    public Map<String, Object> getAttribute();
}
