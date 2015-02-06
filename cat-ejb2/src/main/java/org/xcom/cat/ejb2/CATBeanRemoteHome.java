package org.xcom.cat.ejb2;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface CATBeanRemoteHome extends EJBHome {

    CATBeanRemote create() throws RemoteException, CreateException;

}
