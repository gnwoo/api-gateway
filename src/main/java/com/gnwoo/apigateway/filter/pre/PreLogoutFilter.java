package com.gnwoo.apigateway.filter.pre;

import com.gnwoo.apigateway.repo.JWTTokenRepo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class PreLogoutFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PreLogoutFilter.class);

    @Autowired
    private JWTTokenRepo jwtTokenRepo;

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
               (request.getRequestURI().equals("/auth/logout") ||
                request.getRequestURI().equals("/auth/logout-everywhere"));
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("Received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // get uuid and JWT token, both should not be null here since PreJWTTokenFilter has authenticated them.
        String request_uri = request.getRequestURI();
        String uuid = getCookie(request, "uuid");
        String JWT_signature = getCookie(request, "JWT");
        String JWT_token = jwtTokenRepo.getJWTTokenBySignature(Long. parseLong(uuid), JWT_signature);

        // if it is a single logout request, only remove the token the request carries
        if(request_uri.equals("/auth/logout"))
        {
            jwtTokenRepo.remove(Long.parseLong(uuid), JWT_token);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody("API Gateway PreLogout logout OK");
        }
        // otherwise, it is a logout everywhere request, remove all this uuid's tokens
        else
        {
            jwtTokenRepo.removeAll(Long.parseLong(uuid));
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody("API Gateway PreLogout logout everywhere OK");
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
}
