package com.mycompany.app;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.JobsBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.eclipse.jetty.server.Server;
import org.flywaydb.core.Flyway;
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
        // serve react
        bootstrap.addBundle(new AssetsBundle("/static/", "/"));

        // get config from src/main/resources
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());

        // flyway
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

        // swagger
        bootstrap.addBundle(new SwaggerBundle<HelloWorldConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(HelloWorldConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });

        // quartz
        Job testJob = new TestJob();
        bootstrap.addBundle(new JobsBundle(testJob));

        // add routes
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
