package com.gnwoo.apigateway.filter.pre;

import com.gnwoo.apigateway.repo.JWTTokenRepo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;

public class PreLogoutFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PreAuthenticationFilter.class);

    @Autowired
    private JWTTokenRepo jwtTokenRepo;

    private final ArrayList<String> mustFilterList = new ArrayList<>(
            Arrays.asList("/auth/logout", "/auth/logout-everywhere")
    );

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
        return mustFilterList.contains(request.getRequestURI());
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("API Gateway received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // remove the JWT is in Redis to logout
        String request_uri = request.getRequestURI();
        String JWT_signature = getCookie(request, "JWT");
        String uuid = getCookie(request, "uuid");

        // verify uuid and JWT
        String JWT_token = jwtTokenRepo.getJWTTokenBySignature(Long.parseLong(uuid), JWT_signature);
        if(uuid != null && JWT_signature != null && JWT_token != null)
        {
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
        }
        else
        {
            // invalid, return here
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(500);
            ctx.setResponseBody("API Gateway PreLogout Internal Server Error");
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
