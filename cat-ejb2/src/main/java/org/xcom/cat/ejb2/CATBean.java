package org.xcom.cat.ejb2;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.xcom.cat.core.ClassloaderAnalysisTool;

public class CATBean implements SessionBean {

    private static final long serialVersionUID = 635944364298360442L;

    public CATBean() {
        ClassloaderAnalysisTool.process("CATBean2.initThread");
        ClassloaderAnalysisTool.process(CATBean.class.getClassLoader(),
                "CATBean2.class");
    }

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        System.out.println("set session context");
    }

    public void ejbCreate() throws EJBException {
        System.out.println("ejb create");
    }

    public void ejbRemove() throws EJBException, RemoteException {
        System.out.println("ejb remove");
    }

    public void ejbActivate() throws EJBException, RemoteException {
        System.out.println("ejb activate");
    }

    public void ejbPassivate() throws EJBException, RemoteException {
        System.out.println("ejb passivate");
    }

    public void process() throws RemoteException {
        ClassloaderAnalysisTool.process("CATBean2.processThread");
    }

}
