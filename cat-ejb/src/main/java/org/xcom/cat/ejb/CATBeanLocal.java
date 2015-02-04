package org.xcom.cat.ejb;

import javax.ejb.Local;

@Local
public interface CATBeanLocal {

    void process();

}
