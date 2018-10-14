package com.mycompany.app;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

    private Server server;

    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    public void stop() throws Exception {
        server.stop();
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/static/", "/"));
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {
        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        
        final HelloWorldResource resource = new HelloWorldResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        environment.jersey().register(resource);

        environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
            @Override
            public void serverStarted(Server server) {
                HelloWorldApplication.this.server  = server;
            }
        });
    }
}
