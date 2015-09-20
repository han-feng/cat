package org.xcom.cat.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    private static final Set<String> ignoreResourcePaths = new HashSet<String>();

    static {
        ignoreResourcePaths.add("META-INF/MANIFEST.MF");
        process(ClassloaderAnalysisTool.class.getClassLoader(), "CAT-Core");
    }

    /**
     * 对当前线程上下文类加载器进行分析
     * 
     * @return 当前线程上下文节点
     */
    public static CLNode process() {
        return process(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 对当前线程上下文类加载器进行分析，并给对应的节点添加标签
     * 
     * @param tag
     *            标签
     * @return 当前线程上下文节点
     */
    public static CLNode process(String tag) {
        return process(Thread.currentThread().getContextClassLoader(), tag);
    }

    /**
     * 对指定的类加载器进行分析
     * 
     * @param classloader
     *            待分析的类加载器
     * @return 节点
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
     * @return 节点
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
     * @return 根节点集合
     */
    public static Collection<CLNode> getRoots() {
        return Collections.unmodifiableCollection(ROOTS);
    }

    /**
     * 获取指定标识的节点信息
     * 
     * @param id
     * @return 节点
     */
    public static CLNode getCLNode(String id) {
        return NODE_MAP.get(id);
    }

    /**
     * 查找一个节点中的重复资源
     * 
     * @param nodeId
     * @return
     */
    public static Map<String, List<String>> findDupResouces(String nodeId) {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        CLNode node = getCLNode(nodeId);
        String[] classpaths = node.getClasspath();
        // 目前采用File方式实现，能支持大多数场景，但理论上有不适应的情况需要持续完善。
        for (String resPackage : classpaths) {
            File pack = null;
            try {
                pack = new File(new URL(resPackage).toURI());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
            if (pack == null)
                continue;
            if (pack.isDirectory()) {
                getAllFile(null, pack, result, resPackage);
            } else {
                JarFile jar = null;
                try {
                    jar = new JarFile(pack);
                    Enumeration<JarEntry> enumeration = jar.entries();
                    while (enumeration.hasMoreElements()) {
                        JarEntry entry = enumeration.nextElement();
                        String name = entry.getName();
                        if (!name.endsWith("/"))
                            addToDupResMap(result, name, resPackage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                } finally {
                    if (jar != null) {
                        try {
                            jar.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        Map<String, List<String>> result2 = new HashMap<String, List<String>>();
        for (Entry<String, List<String>> entry : result.entrySet()) {
            if (entry.getValue().size() > 1)
                result2.put(entry.getKey(), entry.getValue());
        }
        result.clear();
        result = null;
        return result2;
    }

    private static void addToDupResMap(Map<String, List<String>> dupResMap,
            String name, String packName) {
        if (ignoreResourcePaths.contains(name))
            return;
        List<String> packs = dupResMap.get(name);
        if (packs == null) {
            packs = new ArrayList<String>();
            dupResMap.put(name, packs);
        }
        packs.add(packName);
    }

    private static void getAllFile(String contextPath, File dir,
            Map<String, List<String>> result, String resPackage) {
        if (contextPath == null) {
            contextPath = "";
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                getAllFile(contextPath + file.getName() + "/", file, result,
                        resPackage);
            } else {
                addToDupResMap(result, contextPath + file.getName(), resPackage);
            }
        }
    }

    public static void main(String[] args) {
        Map<String, List<String>> result = findDupResouces(getRoots()
                .iterator().next().getId());
        for (Entry<String, List<String>> entry : result.entrySet()) {
            String res = entry.getKey();
            System.out.println(res);
            for (String path : entry.getValue()) {
                System.out.println("\t" + path);
            }
            System.out.println();
        }

    }
}
