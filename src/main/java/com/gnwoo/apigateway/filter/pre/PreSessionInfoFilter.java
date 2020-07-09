package com.gnwoo.apigateway.filter.pre;

import com.google.gson.Gson;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreSessionInfoFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(PreLogoutFilter.class);

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessions;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 3;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return request.getMethod().equals("GET") && request.getRequestURI().equals("/user/session-info");
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info(String.format("[RequestMetrics] RequestMethod: %s, RequestURL: %s, RequestIP: %s.",
                request.getMethod(), request.getRequestURL(), request.getRemoteAddr()));

        HttpSession session = request.getSession(false);
        if(session != null)
        {
            String uuid = (String)session.getAttribute("uuid");
            String current_session_id = session.getId();
            // put current_session_id in the first position
            List<String> session_id_list = new ArrayList<>();
            session_id_list.add(current_session_id);
            Map<String, ? extends Session> sessions = this.sessions.findByPrincipalName(uuid);
            for (Session s : sessions.values()) {
                if(!s.getId().equals(current_session_id))
                    session_id_list.add(s.getId());
            }
            String session_id_list_json = new Gson().toJson(session_id_list);

            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(200);
            ctx.setResponseBody(session_id_list_json);
        }
        else
        {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("API Gateway PreSessionInfo Unauthorized");
        }

        return null;
    }
}
