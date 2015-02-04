package org.xcom.cat.ejb;

import javax.ejb.Remote;

@Remote
public interface CATBeanRemote {

    void process();

}
