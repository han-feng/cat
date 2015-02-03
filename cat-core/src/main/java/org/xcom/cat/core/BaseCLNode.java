package org.xcom.cat.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BaseCLNode implements CLNode {

    protected static int MAX_ID = 0;

    protected String id;
    protected String type;
    protected CLNode parent;
    protected ClassLoader classLoader;
    protected Set<CLNode> children = new HashSet<CLNode>();
    protected List<String> classpath = new ArrayList<String>();
    protected Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    protected BaseCLNode(String id, CLNode parent, ClassLoader classloader) {
        this.id = id;
        this.classLoader = classloader;
        this.type = classloader.getClass().getName();
        this.parent = parent;

        if (classLoader instanceof URLClassLoader) {
            URL[] urlObjs = ((URLClassLoader) classLoader).getURLs();
            int len = urlObjs.length;

            for (int i = 0; i < len; i++) {
                classpath.add(urlObjs[i].toString());
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public CLNode getParent() {
        return parent;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public CLNode getRoot() {
        if (isRoot())
            return this;
        else
            return getParent().getRoot();
    }

    public CLNode[] getChildren() {
        return (CLNode[]) children.toArray(new CLNode[children.size()]);
    }

    public String[] getClasspath() {
        return (String[]) classpath.toArray(new String[classpath.size()]);
    }

    public Map<String, Object> getAttribute() {
        return Collections.unmodifiableMap(attributes);
    }

    public boolean isRoot() {
        return parent == null;
    }

}
