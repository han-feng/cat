package org.xcom.cat.web;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.xcom.cat.core.ClassloaderAnalysisTool;
import org.xcom.cat.ejb.CATBeanRemote;

public class CATFilter implements Filter {

    @EJB
    CATBeanRemote cat;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ClassloaderAnalysisTool.process(CATFilter.class.getClassLoader(),
                "Servlet.class");
        ClassloaderAnalysisTool.process("Servlet.initThread");

        // EJB3
        if (cat != null) {
            cat.process();
            System.out.println("############ EJB3 CAT OK !");
        } else {
            System.out.println("############ EJB3 CAT not found !");

        }

        // EJB2
        InitialContext ctx;
        try {
            ctx = new InitialContext();
            Object objRef = ctx.lookup("CATBean2");
            if (objRef != null) {
                org.xcom.cat.ejb2.CATBeanRemoteHome home = (org.xcom.cat.ejb2.CATBeanRemoteHome) PortableRemoteObject
                        .narrow(objRef,
                                org.xcom.cat.ejb2.CATBeanRemoteHome.class);
                org.xcom.cat.ejb2.CATBeanRemote remote = home.create();
                remote.process();
                System.out.println("############ EJB2 CAT OK !");
            } else {
                System.out.println("############ EJB2 CAT not found !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        ClassloaderAnalysisTool.process("Servlet.serviceThread");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
