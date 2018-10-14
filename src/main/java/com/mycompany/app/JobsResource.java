package com.mycompany.app;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.app.models.Job;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class JobsResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;
    private final List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

    public JobsResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public List<Job> getAll() {
        return jobs;
    }

    @POST
    @Timed
    public Job save(Job job) {
        Job newJob = new Job(counter.incrementAndGet(), job.getName());
        jobs.add(newJob);
        return newJob;
    }
}