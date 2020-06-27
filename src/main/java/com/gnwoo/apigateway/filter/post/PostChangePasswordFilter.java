package com.gnwoo.apigateway.filter.post;

import com.gnwoo.apigateway.repo.JWTTokenRepo;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class PostChangePasswordFilter extends ZuulFilter {
    @Autowired
    private JWTTokenRepo jwtTokenRepo;

    private static Logger log = LoggerFactory.getLogger(PostChangePasswordFilter.class);

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

        log.info(String.format("Received %s request to %s", request.getMethod(), request.getRequestURL().toString()));

        // filter uuid cookie out
        Long uuid = null;
        List<Pair<String, String>> filteredResponseHeaders = new ArrayList<>();
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
        if (zuulResponseHeaders != null)
        {
            for (Pair<String, String> header : zuulResponseHeaders)
            {
                if (header.first().equals("Set-Cookie"))
                {
                    if(header.second().startsWith("uuid"))
                    {
                        uuid = Long.parseLong(header.second().substring(5));
                        log.info(String.valueOf(uuid));
                    }
                }
                else
                    filteredResponseHeaders.add(header);
            }
        }
        ctx.put("zuulResponseHeaders", filteredResponseHeaders);

        // once password changed, logout everywhere
        jwtTokenRepo.removeAll(uuid);

        return null;
    }
}
