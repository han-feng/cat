package org.xcom.cat.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

    public static final Set<String> DEFAULT_IGNORE_PATH = new HashSet<String>();

    static {
        DEFAULT_IGNORE_PATH.add("META-INF/MANIFEST.MF");
        DEFAULT_IGNORE_PATH.add("META-INF/NOTICE");
        DEFAULT_IGNORE_PATH.add("META-INF/LICENSE");
        DEFAULT_IGNORE_PATH.add("META-INF/DEPENDENCIES");
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

    public static List<ResourceInfos> findDupResouces(String nodeId) {
        return findDupResouces(nodeId, DEFAULT_IGNORE_PATH);
    }

    /**
     * 查找一个节点中的重复资源
     * 
     * @param nodeId
     * @return Map
     */
    public static List<ResourceInfos> findDupResouces(String nodeId,
            Set<String> ignoreResourcePaths) {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        CLNode node = getCLNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("node not found (id=" + nodeId
                    + ")");
        }
        String[] classpaths = node.getClasspath();
        // 目前采用File方式实现，能支持大多数场景，但理论上有不适应的情况需要持续完善。
        for (String resPackage : classpaths) {
            while (resPackage.endsWith("/")) {
                resPackage = resPackage.substring(0, resPackage.length() - 1);
            }
            File pack = new File(resPackage);
            if (pack.isDirectory()) {
                getAllFile(null, pack, result, resPackage, ignoreResourcePaths);
            } else {
                JarFile jar = null;
                try {
                    jar = new JarFile(pack);
                    Enumeration<JarEntry> enumeration = jar.entries();
                    while (enumeration.hasMoreElements()) {
                        JarEntry entry = enumeration.nextElement();
                        String name = entry.getName();
                        if (!name.endsWith("/"))
                            addToDupResMap(result, name, resPackage,
                                    ignoreResourcePaths);
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
        // 只有存在重复资源的才进行更详细的比较
        List<ResourceInfos> result2 = new LinkedList<ResourceInfos>();
        for (Entry<String, List<String>> entry : result.entrySet()) {
            if (entry.getValue().size() > 1) {
                String name = entry.getKey();
                ResourceInfos infos = new ResourceInfos();
                for (String resPackage : entry.getValue()) {
                    long size = 0;
                    String md5 = "";
                    InputStream in = null;
                    File pack = new File(resPackage);
                    if (pack.isDirectory()) {
                        File res = new File(resPackage + "/" + name);
                        size = res.length();
                        try {
                            in = new FileInputStream(res);
                            md5 = md5(in);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        JarFile jar = null;
                        try {
                            jar = new JarFile(pack);
                            JarEntry jarEntry = jar.getJarEntry(name);
                            size = jarEntry.getSize();
                            in = jar.getInputStream(jarEntry);
                            md5 = md5(in);
                        } catch (Exception e) {
                            e.printStackTrace();
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
                    ResourceInfo info = new ResourceInfo(name, resPackage,
                            size, md5);
                    infos.add(info);
                }
                if (!infos.isSame()) {
                    result2.add(0, infos);
                } else {
                    result2.add(infos);
                }
            }
        }
        result.clear();
        result = null;

        return result2;
    }

    private static String md5(InputStream in) throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8196];
        int length;
        while ((length = in.read(buffer)) != -1) {
            md5.update(buffer, 0, length);
        }
        BigInteger bi = new BigInteger(1, md5.digest());
        return bi.toString(16);
    }

    private static void addToDupResMap(Map<String, List<String>> dupResMap,
            String name, String packName, Set<String> ignoreResourcePaths) {
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
            Map<String, List<String>> result, String resPackage,
            Set<String> ignoreResourcePaths) {
        if (contextPath == null) {
            contextPath = "";
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                getAllFile(contextPath + file.getName() + "/", file, result,
                        resPackage, ignoreResourcePaths);
            } else {
                addToDupResMap(result, contextPath + file.getName(),
                        resPackage, ignoreResourcePaths);
            }
        }
    }

    public static class ResourceInfos implements Iterable<ResourceInfo> {

        private List<ResourceInfo> list = new ArrayList<ResourceInfo>();
        private String name;
        private boolean same = true;
        private boolean first = true;
        private long size;
        private String md5;

        public String getName() {
            return name;
        }

        public boolean isSame() {
            return same;
        }

        void add(ResourceInfo resourceInfo) {
            list.add(resourceInfo);
            if (first) {
                name = resourceInfo.getName();
                size = resourceInfo.getSize();
                md5 = resourceInfo.getMd5();
                first = false;
                return;
            }
            if (!same)
                return;
            if (size != resourceInfo.getSize()
                    || !md5.equals(resourceInfo.getMd5())) {
                same = false;
            }
            return;
        }

        public Iterator<ResourceInfo> iterator() {
            return list.iterator();
        }
    }

    public static class ResourceInfo {
        private String name;
        private String parent;
        private long size;
        private String md5;

        ResourceInfo(String name, String parent, long size, String md5) {
            this.name = name;
            this.parent = parent;
            this.size = size;
            this.md5 = md5;
        }

        public String getName() {
            return name;
        }

        public String getParent() {
            return parent;
        }

        public long getSize() {
            return size;
        }

        public String getMd5() {
            return md5;
        }

        public String toString() {
            return name + "\t" + parent + "\t" + size + "\t" + md5;
        }
    }

    public static void main(String[] args) {
        List<ResourceInfos> result = findDupResouces(getRoots().iterator()
                .next().getId());
        for (ResourceInfos infos : result) {
            String res = infos.getName();
            System.out.println(res);
            System.out.println("same : " + infos.isSame());
            for (ResourceInfo info : infos) {
                System.out.println("\t" + info);
            }
            System.out.println();
        }
    }

}
