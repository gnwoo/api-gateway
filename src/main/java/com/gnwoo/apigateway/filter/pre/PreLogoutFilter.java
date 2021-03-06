package com.gnwoo.apigateway.filter.pre;

import com.corundumstudio.socketio.SocketIOClient;
import com.gnwoo.apigateway.data.repo.WsCommunicationRepo;
import com.gnwoo.apigateway.data.repo.WsSessionRepo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public class PreLogoutFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PreLogoutFilter.class);

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessions;

    @Autowired
    private WsSessionRepo wsSessionRepo;

    @Autowired
    private WsCommunicationRepo wsCommunicationRepo;

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
               (request.getRequestURI().equals("/user/logout") ||
                request.getRequestURI().equals("/user/logout-everywhere"));
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("[RequestMetrics] RequestMethod: %s, RequestURL: %s, RequestIP: %s.",
                request.getMethod(), request.getRequestURL(), request.getRemoteAddr()));

        String request_uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // if it is a single logout request, only invalidate and delete this session
        if(session != null && request_uri.equals("/user/logout"))
        {
            // logout ws session & delete this session's communication info
            String ws_session_token = "";
            wsSessionRepo.deleteSessionByToken(ws_session_token);


            String session_to_logout_id = request.getHeader("session-to-logout-id");
            this.sessions.deleteById(session_to_logout_id);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody("API Gateway PreLogout logout OK");
        }
        // otherwise, it is a logout everywhere request, invalidate and delete all this uuid's sessions
        else if (session != null)
        {
            String uuid = (String)session.getAttribute("uuid");
            Map<String, ? extends Session> sessions = this.sessions.findByPrincipalName(uuid);
            for (Session s : sessions.values()) {
                this.sessions.deleteById(s.getId());
            }

            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody("API Gateway PreLogout logout everywhere OK");
        }
        // otherwise, it is an unauthorized request
        else
        {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("API Gateway PreLogout logout everywhere failed");
        }

        return null;
    }

    private String getWsSessionFromCookie(String cookies) {
        String[] cookie_arr = cookies.split("; ");
        for(String cookie : cookie_arr) {
            if(cookie.startsWith("WSSESSION=")) {
                return cookie.substring(10);
            }
        }
        return null;
    }
}
