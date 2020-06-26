package com.gnwoo.apigateway.filter.post;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gnwoo.apigateway.handler.JWTHandler;
import com.gnwoo.apigateway.repo.JWTTokenRepo;
import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class PostLoginFilter extends ZuulFilter {

    @Autowired
    private JWTHandler jwtHandler;
    @Autowired
    private JWTTokenRepo jwtTokenRepo;

    private static Logger log = LoggerFactory.getLogger(PostLoginFilter.class);

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        // check if the request needs to be filtered
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        return request.getMethod().equals("POST") && request.getRequestURI().equals("/auth/login") &&
               response.getStatus() == 200;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();

        log.info(String.format("Received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // filter JWT token cookie out
        Long uuid = null;
        String JWT_token = "";
        List<Pair<String, String>> filteredResponseHeaders = new ArrayList<>();
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
        if (zuulResponseHeaders != null)
        {
            for (Pair<String, String> header : zuulResponseHeaders)
            {
                if (header.first().equals("Set-Cookie"))
                {
                    if(header.second().startsWith("JWT"))
                    {
                        JWT_token = header.second().substring(4);
//                        log.info(JWT_token);
                    }
                    if(header.second().startsWith("uuid"))
                    {
                        uuid = Long.parseLong(header.second().substring(5));
                        log.info(String.valueOf(uuid));
                        filteredResponseHeaders.add(header);
                    }
                }
                else
                    filteredResponseHeaders.add(header);
            }
        }
        ctx.put("zuulResponseHeaders", filteredResponseHeaders);

        // save uuid, JWT signature and JWT token to Redis
        String JWT_signature = jwtHandler.extract_JWT_signature(JWT_token);
        jwtTokenRepo.saveJWTToken(uuid, JWT_token);
        log.info(jwtTokenRepo.getJWTTokenBySignature(uuid, JWT_signature));

        // response with JWT signature in cookie
        Cookie JWT_cookie = new Cookie("JWT", JWT_signature);
//        JWT_cookie.setSecure(true);
//        JWT_cookie.setHttpOnly(true);
        response.addCookie(JWT_cookie);

        return null;
    }

}
