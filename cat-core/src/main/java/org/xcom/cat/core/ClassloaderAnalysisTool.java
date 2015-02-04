package org.xcom.cat.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类加载器分析工具，用于分析当前JVM类加载器结构，辅助解决类加载相关问题
 * 
 * @author han_feng
 *
 */
public class ClassloaderAnalysisTool {

    protected static int MAX_ID = 1;

    private static final Map<String, CLNode> NODE_MAP = new ConcurrentHashMap<String, CLNode>();
    private static final Map<ClassLoader, CLNode> CL_TO_NODE = new ConcurrentHashMap<ClassLoader, CLNode>();
    private static final Set<CLNode> ROOTS = new HashSet<CLNode>();

    static {
        process(ClassloaderAnalysisTool.class.getClassLoader(), "CAT-Core");
    }

    /**
     * 对当前线程上下文类加载器进行分析
     * 
     * @return
     */
    public static CLNode process() {
        return process(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 对当前线程上下文类加载器进行分析，并给对应的节点添加标签
     * 
     * @param tag
     *            标签
     * @return
     */
    public static CLNode process(String tag) {
        return process(Thread.currentThread().getContextClassLoader(), tag);
    }

    /**
     * 对指定的类加载器进行分析
     * 
     * @param classloader
     *            待分析的类加载器
     * @return
     */
    public static CLNode process(ClassLoader classloader) {
        return process(classloader, null);
    }

    /**
     * 对指定的类加载器进行分析，并给对应的节点添加标签
     * 
     * @param classloader
     *            待分析的类加载器
     * @param tag
     *            标签
     * @return
     */
    public static CLNode process(ClassLoader classloader, String tag) {
        CLNode node = CL_TO_NODE.get(classloader);
        if (node != null) {
            // 添加标签
            node.addTag(tag);
            return node;
        }

        ClassLoader pcl = classloader.getParent();
        CLNode parent = null;
        if (pcl != null) {
            parent = process(pcl);
        }

        String id = Integer.toString(MAX_ID++);
        // 使用扩展分析器进行分析
        ServiceLoader<ExtendAnalyzer> serviceloader = ServiceLoader.load(
                ExtendAnalyzer.class, Thread.currentThread()
                        .getContextClassLoader());
        for (ExtendAnalyzer analyzer : serviceloader) {
            node = analyzer.process(id, parent, classloader);
            if (node != null)
                break;
        }

        if (node == null) {
            // 默认分析
            node = new BaseCLNode(id, parent, classloader);
        }

        if (parent != null && parent instanceof BaseCLNode) {
            BaseCLNode bnode = ((BaseCLNode) parent);
            if (!bnode.children.contains(node))
                bnode.children.add(node);
        }

        NODE_MAP.put(node.getId(), node);
        CL_TO_NODE.put(classloader, node);

        if (node.isRoot() && !ROOTS.contains(node))
            ROOTS.add(node);

        // 添加标签
        node.addTag(tag);

        return node;
    }

    /**
     * 获取根（无父）节点信息
     * 
     * @return
     */
    public static Collection<CLNode> getRoots() {
        return Collections.unmodifiableCollection(ROOTS);
    }

    /**
     * 获取指定标识的节点信息
     * 
     * @param id
     * @return
     */
    public static CLNode getCLNode(String id) {
        return NODE_MAP.get(id);
    }

}
