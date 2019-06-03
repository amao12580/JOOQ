package com.study.jooq.model;

import com.mysql.jdbc.StringUtils;
import com.study.jooq.base.utils.ScopedContext;
import com.study.jooq.common.generated.Tables;
import com.study.jooq.common.generated.tables.User;
import com.study.jooq.common.generated.tables.records.UserRecord;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author guang
 * @since 2019-04-02 12:50
 */
public class MyTest {

    private final static Logger log = LoggerFactory.getLogger(MyTest.class);


    private DSLContext dslContext;

    @Before
    public void init() {

        ScopedContext scopedContext = new ScopedContext();
        dslContext = scopedContext.getDSLContext();

    }


    @Test
    public void test() {

        Result<UserRecord> userRecords = dslContext.selectFrom(Tables.USER).fetch();
        Optional<UserRecord> userRecordOptional = dslContext.selectFrom(Tables.USER).fetchOptional();

        boolean present = userRecordOptional.isPresent();


        log.info(userRecords.format());

    }


    @Test
    public void query() {


        Result<UserRecord> userRecords = dslContext.selectFrom(Tables.USER).
                where(User.USER.NAME.eq("张三")).
                fetch();

        log.info(userRecords.format());


    }


    /**
     * 动态sql
     */
    @Test
    public void dymaicSql() {


        SelectQuery<UserRecord> query = dslContext.selectQuery(User.USER);

        query.addConditions(User.USER.NAME.like("%李%"));

        query.addConditions(User.USER.SEX.eq((byte) 1));

        Result<UserRecord> userRecords = query.fetch();

        log.info(userRecords.format());


    }


    /**
     * 动态 jooq sql
     */
    @Test
    public void dynmaicSql2() {

        Condition condition = DSL.trueCondition();

        if (true) {
            condition = condition.and(User.USER.NAME.like("%李%"));
        }

        if (true) {
            condition = condition.andNot(User.USER.SEX.eq((byte) 2));
        }

        Result<UserRecord> recordResult = dslContext.selectFrom(User.USER).
                where(condition).
                fetch();

        log.info(recordResult.format());

    }


}
