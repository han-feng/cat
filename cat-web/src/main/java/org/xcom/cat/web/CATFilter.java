package org.xcom.cat.web;

import java.io.IOException;

import javax.ejb.EJB;
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

        if (cat != null)
            cat.process();
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
