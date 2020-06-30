package com.gnwoo.apigateway.filter.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class PreLogoutFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PreLogoutFilter.class);

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessions;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return request.getMethod().equals("POST") &&
               (request.getRequestURI().equals("/user/logout") ||
                request.getRequestURI().equals("/user/logout-everywhere"));
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("Received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        String request_uri = request.getRequestURI();
        HttpSession session = request.getSession();

        // if it is a single logout request, only invalidate and delete this session
        if(request_uri.equals("/user/logout"))
        {
            this.sessions.deleteById(session.getId());
            session.invalidate();
            session.setMaxInactiveInterval(0);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody("API Gateway PreLogout logout OK");
        }
        // otherwise, it is a logout everywhere request, invalidate and delete all this uuid's sessions
        else
        {
            String uuid = (String)session.getAttribute("uuid");
            Map<String, ? extends Session> sessions = this.sessions.findByPrincipalName(uuid);
            for (Session s : sessions.values()) {
                this.sessions.deleteById(s.getId());
            }
            session.invalidate();
            session.setMaxInactiveInterval(0);

            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody("API Gateway PreLogout logout everywhere OK");
        }

        return null;
    }
}
