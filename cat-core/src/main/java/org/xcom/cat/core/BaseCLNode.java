package org.xcom.cat.core;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BaseCLNode implements CLNode, Serializable {

    private static final long serialVersionUID = -2758132972451801019L;

    protected static int MAX_ID = 0;

    protected Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
    protected Set<CLNode> children = new HashSet<CLNode>();
    protected transient ClassLoader classLoader;
    protected List<String> classpath = new ArrayList<String>();
    protected String description;
    protected String id;
    protected CLNode parent;
    protected Set<String> tags = new HashSet<String>();
    protected String type;

    protected BaseCLNode(String id, CLNode parent, ClassLoader classloader) {
        this.id = id;
        this.description = classloader.toString();
        this.classLoader = classloader;
        this.type = classloader.getClass().getName();
        this.parent = parent;

        if (classLoader instanceof URLClassLoader) {
            URL[] urlObjs = ((URLClassLoader) classLoader).getURLs();
            int len = urlObjs.length;

            for (int i = 0; i < len; i++) {
                try {
                    classpath.add(urlObjs[i].toURI().getPath());
                } catch (URISyntaxException e) {
                }
            }
        }
    }

    @Override
    public void addTag(String tag) {
        if (tag != null) {
            tag = tag.trim();
            if (!tag.equals(""))
                tags.add(tag);
        }
    }

    public Map<String, Object> getAttribute() {
        return Collections.unmodifiableMap(attributes);
    }

    public CLNode[] getChildren() {
        return (CLNode[]) children.toArray(new CLNode[children.size()]);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String[] getClasspath() {
        return (String[]) classpath.toArray(new String[classpath.size()]);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public CLNode getParent() {
        return parent;
    }

    public CLNode getRootNode() {
        if (isRoot())
            return this;
        else
            return getParent().getRootNode();
    }

    @Override
    public String[] getTags() {
        return (String[]) tags.toArray(new String[tags.size()]);
    }

    public String getType() {
        return type;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public boolean isLeaf() {
        return children.size() == 0;
    }

}
