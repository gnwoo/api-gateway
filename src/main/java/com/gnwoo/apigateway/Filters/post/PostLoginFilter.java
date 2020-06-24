package com.gnwoo.apigateway.Filters.post;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gnwoo.apigateway.handler.JWTHandler;
import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostLoginFilter extends ZuulFilter {

    @Autowired
    private JWTHandler jwtHandler;

    private static Logger log = LoggerFactory.getLogger(PostLoginFilter.class);

    private final ArrayList<String> mustFilterList = new ArrayList<>(
            Arrays.asList("/auth/login")
    );

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
        String uri = request.getRequestURI();
        HttpServletResponse response = ctx.getResponse();
        return mustFilterList.contains(uri) && response.getStatus() == 200;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();
        String JWT_token = "";
        List<Pair<String, String>> filteredResponseHeaders = new ArrayList<>();
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
        if (zuulResponseHeaders != null)
        {
            for (Pair<String, String> header : zuulResponseHeaders)
            {
                if (header.first().equals("Set-Cookie") && header.second().startsWith("JWT"))
                {
                        JWT_token = header.second().substring(4);
                        log.info(JWT_token);
                }
                else
                    filteredResponseHeaders.add(header);
            }
        }
        ctx.put("zuulResponseHeaders", filteredResponseHeaders);

        String JWT_signature = jwtHandler.extract_JWT_signature(JWT_token);
        Cookie JWT_cookie = new Cookie("JWT", JWT_signature);
//        JWT_cookie.setSecure(true);
//        JWT_cookie.setHttpOnly(true);
        response.addCookie(JWT_cookie);

        return null;
    }

}
