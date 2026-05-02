package com.seaman.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.io.Serializable;

@Repository
public class CommonRepository implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    @Autowired
    protected NamedParameterJdbcTemplate template;

    @Autowired
    protected DataSource dataSource;
}
