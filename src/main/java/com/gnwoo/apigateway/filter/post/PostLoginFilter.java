package com.gnwoo.apigateway.filter.post;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gnwoo.apigateway.data.repo.WsSessionRepo;
import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.ZuulFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;

import java.util.ArrayList;
import java.util.List;

public class PostLoginFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PostLoginFilter.class);

    @Autowired
    private WsSessionRepo wsSessionRepo;

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
        return request.getMethod().equals("POST") && request.getRequestURI().equals("/user/login") &&
               response.getStatus() == 200;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();

        log.info(String.format("[RequestMetrics] RequestMethod: %s, RequestURL: %s, RequestIP: %s.",
                request.getMethod(), request.getRequestURL(), request.getRemoteAddr()));

        // filter uuid header out to get uuid
        Long uuid = null;
        List<Pair<String, String>> filteredResponseHeaders = new ArrayList<>();
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
        if (zuulResponseHeaders != null)
        {
            for (Pair<String, String> header : zuulResponseHeaders)
            {
                if (header.first().equals("uuid"))
                    uuid = Long.parseLong(header.second());
                else
                    filteredResponseHeaders.add(header);
            }
        }
        ctx.put("zuulResponseHeaders", filteredResponseHeaders);

        // establish http session
        HttpSession session = request.getSession(true);
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, String.valueOf(uuid));
        session.setAttribute("uuid", String.valueOf(uuid));
        session.setAttribute("ip_address", request.getRemoteAddr());
        long unix_time = System.currentTimeMillis() / 1000L;
        session.setAttribute("last_update_time", unix_time);
        // session should never be timed out
        session.setMaxInactiveInterval(-1);

        // establish ws session
        String ws_session_token = wsSessionRepo.establishSession(uuid);
        Cookie ws_session_cookie = new Cookie("WSSESSION", ws_session_token);
        ws_session_cookie.setPath("/");
        ws_session_cookie.setHttpOnly(true);
        response.addCookie(ws_session_cookie);

        return null;
    }

}
