package org.xcom.cat.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassloaderAnalysisTool {

    protected static int MAX_ID = 1;

    private static final Map<String, CLNode> NODE_MAP = new ConcurrentHashMap<String, CLNode>();
    private static final Map<ClassLoader, CLNode> CL_TO_NODE = new ConcurrentHashMap<ClassLoader, CLNode>();
    private static final Set<CLNode> ROOTS = new HashSet<CLNode>();

    /**
     * 对类加载器进行分析
     * 
     * @param classloader
     * @return
     */
    public static CLNode process(ClassLoader classloader) {
        CLNode node = CL_TO_NODE.get(classloader);
        if (node != null)
            return node;

        ClassLoader pcl = classloader.getParent();
        CLNode parent = null;
        if (pcl != null) {
            parent = process(pcl);
        }

        // 使用扩展分析器进行分析
        ServiceLoader<ExtendAnalyzer> serviceloader = ServiceLoader.load(
                ExtendAnalyzer.class, Thread.currentThread()
                        .getContextClassLoader());
        for (ExtendAnalyzer analyzer : serviceloader) {
            node = analyzer.process(Integer.toString(MAX_ID++), parent,
                    classloader);
            if (node != null)
                return node;
        }

        // 默认分析
        node = new BaseCLNode(Integer.toString(MAX_ID++), parent, classloader);

        if (parent != null && parent instanceof BaseCLNode) {
            BaseCLNode bnode = ((BaseCLNode) parent);
            if (!bnode.children.contains(node))
                bnode.children.add(node);
        }

        NODE_MAP.put(node.getId(), node);
        CL_TO_NODE.put(classloader, node);

        if (node.isRoot() && !ROOTS.contains(node))
            ROOTS.add(node);

        return node;
    }

    public static Collection<CLNode> getRoots() {
        return Collections.unmodifiableCollection(ROOTS);
    }

    public static CLNode getCLNode(String id) {
        return NODE_MAP.get(id);
    }

}
