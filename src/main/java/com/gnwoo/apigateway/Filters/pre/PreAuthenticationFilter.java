package com.gnwoo.apigateway.Filters.pre;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.gnwoo.apigateway.handler.JWTHandler;
import com.gnwoo.apigateway.repo.JWTTokenRepo;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreAuthenticationFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(PreAuthenticationFilter.class);
    @Autowired
    private JWTHandler jwtHandler;
    @Autowired
    private JWTTokenRepo jwtTokenRepo;

    private final ArrayList<String> mustFilterList = new ArrayList<>(
            Arrays.asList("/auth/health")
    );

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
        return mustFilterList.contains(request.getRequestURI());
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("API Gateway received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // TODO: check if the JWT is in Redis
        String JWT_signature = getCookie(request, "JWT");
        String uuid = getCookie(request, "uuid");

        // verify uuid and JWT
        // jwtHandler.verifyJWT(Long. parseLong(uuid), JWT_token)
        //  && jwtTokenRepo.get(Long. parseLong(uuid), JWT_signature) != null
        if(uuid != null && JWT_signature != null)
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
            ctx.setResponseBody("API Gateway AuthenticationFilter failed");
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
