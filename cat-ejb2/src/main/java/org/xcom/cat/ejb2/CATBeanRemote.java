package org.xcom.cat.ejb2;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface CATBeanRemote extends EJBObject {

    void process() throws RemoteException;

}
