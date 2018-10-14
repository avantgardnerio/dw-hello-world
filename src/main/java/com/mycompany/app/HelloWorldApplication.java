package com.mycompany.app;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.jdbi.v3.core.Jdbi;

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
    public void run(HelloWorldConfiguration config,
                    Environment environment) {

        // setup database
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, config.getDataSourceFactory(), "postgresql");

        // setup health checks
        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(config.getTemplate());
        environment.healthChecks().register("template", healthCheck);

        // setup resources
        final JobsResource resource = new JobsResource(
                config.getTemplate(),
                config.getDefaultName(),
                jdbi
        );
        environment.jersey().register(resource);

        // Save server so we can shut it down later
        environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
            @Override
            public void serverStarted(Server server) {
                HelloWorldApplication.this.server  = server;
            }
        });
    }
}
