package com.gnwoo.apigateway.filter.pre;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PreSessionFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PreSessionFilter.class);

    private final Map<String, String> mustSessionList = new HashMap<>() {{
        put("/user/authentication-status", "GET");
        put("/user/user-info", "GET");
        put("/user/change-2FA-status", "POST");
    }};

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return isInMustAuthenticationList(request);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("[RequestMetrics] RequestMethod: %s, RequestURL: %s, RequestIP: %s.",
                request.getMethod(), request.getRequestURL(), request.getRemoteAddr()));

        // verify session
        HttpSession session = request.getSession(false);
        if(session != null)
        {
            // authenticated, route the request to the corresponding service
            String uuid = session.getAttribute("uuid").toString();
            ctx.addZuulRequestHeader("uuid", uuid);
            ctx.setSendZuulResponse(true);
        }
        else
        {
            // invalid, return here
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("API Gateway PreSessionFilter Unauthorized");
        }
        return null;
    }

    private boolean isInMustAuthenticationList(HttpServletRequest req) {
        String method = mustSessionList.get(req.getRequestURI());
        return method != null && method.equals(req.getMethod());
    }

}
