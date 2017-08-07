package org.xcom.cat.support;

import java.lang.reflect.Method;

import org.xcom.cat.core.BaseCLNode;
import org.xcom.cat.core.CLNode;
import org.xcom.cat.core.ExtendAnalyzer;

/**
 * WebLogic类加载器分析工具
 * 
 * @author han_feng
 *
 */
public class WebLogicExtendAnalyzer implements ExtendAnalyzer {

    public CLNode process(String id, CLNode parent, ClassLoader classloader) {
        if (classloader.getClass().getName()
                .startsWith("weblogic.utils.classloaders")) {
            return new WebLogicCLNode(id, parent, classloader);
        }
        return null;
    }

    private static class WebLogicCLNode extends BaseCLNode {

        private static final long serialVersionUID = -4874150438291766904L;

        protected WebLogicCLNode(String id, CLNode parent,
                ClassLoader classloader) {
            super(id, parent, classloader);

            Method method;
            try {
                method = classloader.getClass().getMethod("getFinderClassPath");
                String classpath = (String) method.invoke(classloader);
                String[] urls = classpath.split(";");
                for (String url : urls) {
                    url = url.trim();
                    if (!url.equals(""))
                        this.classpath.add(url.replaceAll("\\\\", "\\/"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
