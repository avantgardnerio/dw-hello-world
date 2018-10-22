package com.mycompany.app;

import com.mycompany.app.models.Job;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JobMapper implements RowMapper<Job> {

    @Override
    public Job map(ResultSet r, StatementContext ctx) throws SQLException {
        Job job = new Job();
        job.setId(r.getInt("id"));
        job.setName(r.getString("name"));
        return job;
    }
}