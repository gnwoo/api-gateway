package com.gnwoo.apigateway.filter.post;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class PostChangePasswordFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PostChangePasswordFilter.class);

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessions;

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        // check if the request needs to be filtered
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        return request.getMethod().equals("PUT") && request.getRequestURI().equals("/user/change-password") &&
               response.getStatus() == 200;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpSession session = request.getSession();

        log.info(String.format("Received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        String uuid = (String)session.getAttribute("uuid");
        Map<String, ? extends Session> sessions = this.sessions.findByPrincipalName(uuid);
        for (Session s : sessions.values()) {
            this.sessions.deleteById(s.getId());
        }
        session.invalidate();
        session.setMaxInactiveInterval(0);

        return null;
    }
}
