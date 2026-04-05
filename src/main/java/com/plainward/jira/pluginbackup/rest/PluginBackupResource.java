package com.plainward.jira.pluginbackup.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/")
public class PluginBackupResource {

    private Response forbidden() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(Collections.singletonMap("error", "Admin access required"))
                .build();
    }

    private boolean isAdmin() {
        JiraAuthenticationContext authCtx = ComponentAccessor.getJiraAuthenticationContext();
        ApplicationUser user = authCtx.getLoggedInUser();
        if (user == null) return false;
        GroupManager gm = ComponentAccessor.getComponent(GroupManager.class);
        return gm.isUserInGroup(user, "jira-administrators");
    }

    private File getPluginsDir() {
        JiraHome jiraHome = ComponentAccessor.getComponent(JiraHome.class);
        if (jiraHome == null) return null;
        File dir = new File(jiraHome.getHomePath(), "plugins/installed-plugins");
        return dir.isDirectory() ? dir : null;
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_JSON})
    public Response listPluginJars(@Context HttpServletRequest request) {
        if (!isAdmin()) return forbidden();

        File pluginsDir = getPluginsDir();
        if (pluginsDir == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("error", "Plugins directory not found"))
                    .build();
        }

        File[] jars = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) {
            return Response.ok(Collections.emptyList()).build();
        }

        Arrays.sort(jars, Comparator.comparing(File::getName));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        List<Map<String, Object>> result = new ArrayList<>();
        for (File jar : jars) {
            Map<String, Object> info = new HashMap<>();
            info.put("name", jar.getName());
            info.put("size", jar.length());
            info.put("modified", sdf.format(new Date(jar.lastModified())));
            result.add(info);
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/download")
    @Produces({"application/java-archive", "application/octet-stream"})
    public Response downloadPluginJar(@Context HttpServletRequest request,
                                      @QueryParam("name") String fileName) {
        if (!isAdmin()) return forbidden();

        if (fileName == null || fileName.isEmpty()
                || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid file name").build();
        }

        File pluginsDir = getPluginsDir();
        if (pluginsDir == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        File jar = new File(pluginsDir, fileName);
        if (!jar.exists() || !jar.isFile() || !jar.getParentFile().equals(pluginsDir)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("JAR not found: " + fileName).build();
        }

        return Response.ok(jar, "application/java-archive")
                .header("Content-Disposition", "attachment; filename=\"" + jar.getName() + "\"")
                .header("Content-Length", jar.length())
                .build();
    }

    @GET
    @Path("/download-all")
    @Produces("application/zip")
    public Response downloadAllPluginJars(@Context HttpServletRequest request) {
        if (!isAdmin()) return forbidden();

        File pluginsDir = getPluginsDir();
        if (pluginsDir == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        File[] jars = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No plugins found").build();
        }

        String zipName = "jira-plugins-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".zip";

        StreamingOutput stream = output -> {
            try (ZipOutputStream zos = new ZipOutputStream(output)) {
                byte[] buffer = new byte[8192];
                for (File jar : jars) {
                    zos.putNextEntry(new ZipEntry(jar.getName()));
                    try (FileInputStream fis = new FileInputStream(jar)) {
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
            }
        };

        return Response.ok(stream, "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + zipName + "\"")
                .build();
    }
}
