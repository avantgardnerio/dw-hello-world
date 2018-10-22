package com.mycompany.app;

import com.mycompany.app.models.Job;
import org.junit.*;
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;

public class JobRepoTest {
    @Rule
    public GuiceyAppRule<HelloWorldConfiguration> RULE = new GuiceyAppRule<>(HelloWorldApplication.class, "example.yml");
    
    @Test
    public void shouldSaveAndLoad() {
        // Setup
        JobDao jobDao = RULE.getBean(JobDao.class);
        jobDao.deleteAll();
        Job expected = new Job(1, "import");

        // exercise
        int id = jobDao.insert(expected);
        Job actual = jobDao.getById(id);

        // assert
        expected.setId(id);
        Assert.assertEquals("Job should save & load", expected, actual);
    }
}