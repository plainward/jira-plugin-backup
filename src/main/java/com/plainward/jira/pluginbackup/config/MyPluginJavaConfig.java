package com.plainward.jira.pluginbackup.config;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
        ModuleFactoryBean.class,
        PluginAccessorBean.class
})
public class MyPluginJavaConfig {

    @Bean
    public TemplateRenderer templateRenderer() {
        return importOsgiService(TemplateRenderer.class);
    }

    @Bean
    public GroupManager groupManager() {
        return importOsgiService(GroupManager.class);
    }
}
