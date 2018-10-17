package com.mycompany.app;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.guicey.jdbi3.JdbiBundle;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

    private Server server;

    public HelloWorldApplication() {
        System.out.println("App");
    }

    @Override
    public void run(String... arguments) throws Exception {
        super.run(arguments);
    }

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

        bootstrap.addBundle(new FlywayBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDatabase();
            }

            @Override
            public FlywayFactory getFlywayFactory(HelloWorldConfiguration configuration) {
                return configuration.getFlywayFactory();
            }
        });

        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(getClass().getPackage().getName())
                .bundles(JdbiBundle.<HelloWorldConfiguration>forDatabase((conf, env) -> conf.getDatabase()))
                .build());
    }

    @Override
    public void run(HelloWorldConfiguration config,
                    Environment environment) {

        // https://flywaydb.org/documentation/plugins/dropwizard
        DataSourceFactory dataSourceFactory = config.getDatabase();
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        flyway.migrate();

        // setup health checks
        final TemplateHealthCheck healthCheck = new TemplateHealthCheck(config.getTemplate());
        environment.healthChecks().register("template", healthCheck);

        // Save server so we can shut it down later
        environment.lifecycle().addServerLifecycleListener(server -> HelloWorldApplication.this.server = server);
    }
}
