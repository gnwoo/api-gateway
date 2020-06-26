package com.gnwoo.apigateway.filter.pre;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.gnwoo.apigateway.repo.JWTTokenRepo;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class PreAuthenticationFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(PreAuthenticationFilter.class);

    @Autowired
    private JWTTokenRepo jwtTokenRepo;

    private final Map<String, String> mustAuthenticatedList = new HashMap<>() {{
        put("/auth/authentication-status", "GET");
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

        log.info(String.format("Received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // verify uuid and JWT
        String JWT_signature = getCookie(request, "JWT");
        String uuid = getCookie(request, "uuid");
        if(uuid != null && JWT_signature != null &&
           jwtTokenRepo.getJWTTokenBySignature(Long. parseLong(uuid), JWT_signature) != null)
        {
            // authenticated, route the request to the corresponding service
            ctx.setSendZuulResponse(true);
        }
        else
        {
            // invalid, return here
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("API Gateway PreAuthenticationFilter failed");
        }
        return null;
    }

    private String getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if(cookies!=null)
        {
            for (Cookie cookie : cookies)
            {
                if(cookie.getName().equals(name))
                    return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isInMustAuthenticationList(HttpServletRequest req) {
        String method = mustAuthenticatedList.get(req.getRequestURI());
        return method != null && method.equals(req.getMethod());
    }

}
