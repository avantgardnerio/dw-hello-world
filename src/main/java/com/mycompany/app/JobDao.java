package com.mycompany.app;

import com.mycompany.app.models.Job;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;

// https://www.dropwizard.io/1.3.5/docs/manual/jdbi3.html
@JdbiRepository
public interface JobDao {
    @SqlUpdate("insert into job (name) values (:name) returning id")
    @InTransaction
    @GetGeneratedKeys
    int insert(@BindBean Job job);

    @SqlUpdate("update job set name=:name where id=:id")
    @InTransaction
    int update(Job job);

    @SqlQuery("select * from job where id=:id")
    @InTransaction
    Job getById(@Bind("id") int id);

    @SqlUpdate("delete from job where id=:id")
    @InTransaction
    int delete(@Bind int id);

    @SqlUpdate("delete from job")
    @InTransaction
    int deleteAll();
}
