package com.gnwoo.apigateway.Filters.pre;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.gnwoo.apigateway.handler.JWTHandler;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthenticationFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    @Autowired
    private JWTHandler jwtHandler;

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
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info(String.format("API Gateway received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // TODO: check if the JWT is in Redis
        String JWT_token = getCookie(request, "JWT");
        String uuid = getCookie(request, "uuid");

        // verify uuid and JWT
        if(JWT_token != null && uuid != null && jwtHandler.verifyJWT(Long. parseLong(uuid), JWT_token))
        {
            // authenticated, proxy the request to the user service
            ctx.setSendZuulResponse(true);
            ctx.setResponseStatusCode(200);
        }
        else
        {
            // invalid, return here
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("API Gateway auth failed");
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
