package org.xcom.cat.ejb;

import javax.ejb.Stateless;

import org.xcom.cat.core.ClassloaderAnalysisTool;

/**
 * Session Bean implementation class CATBean
 */
@Stateless
public class CATBean implements CATBeanRemote, CATBeanLocal {

    /**
     * Default constructor.
     */
    public CATBean() {
        ClassloaderAnalysisTool.process("CATBean.initThread");
        ClassloaderAnalysisTool.process(CATBean.class.getClassLoader(),
                "CATBean.class");
    }

    @Override
    public void process() {
        ClassloaderAnalysisTool.process("CATBean.processThread");
    }

}
