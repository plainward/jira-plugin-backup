package com.plainward.jira.pluginbackup.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PluginBackupServlet extends HttpServlet {

    private static final String TEMPLATE = "/vm/plugin-backup.vm";

    private final TemplateRenderer templateRenderer;

    @Inject
    public PluginBackupServlet(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        JiraAuthenticationContext authCtx = ComponentAccessor.getJiraAuthenticationContext();
        ApplicationUser user = authCtx.getLoggedInUser();

        if (user == null) {
            URI loginUri = URI.create("/login.jsp?os_destination=" +
                    java.net.URLEncoder.encode(req.getRequestURI(), "UTF-8"));
            resp.sendRedirect(loginUri.toString());
            return;
        }

        GroupManager gm = ComponentAccessor.getComponent(GroupManager.class);
        if (!gm.isUserInGroup(user, "jira-administrators")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("baseUrl", ComponentAccessor.getApplicationProperties().getString("jira.baseurl"));

        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render(TEMPLATE, context, resp.getWriter());
    }
}
