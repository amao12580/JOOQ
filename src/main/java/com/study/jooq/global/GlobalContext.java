package com.study.jooq.global;

import com.study.jooq.base.connection.ConnectionPoolService;
import com.study.jooq.base.connection.impl.HikariCpImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * 全局上下文.
 */
public enum GlobalContext {


    INSTANCE;

   private final static Logger log = LoggerFactory.getLogger(GlobalContext.class);

    private final ConnectionPoolService connPollService;

    GlobalContext() {
        this.connPollService = new HikariCpImpl(); // 构造 mysql 连接池服务
    }

    public ConnectionPoolService getConnPoolService() {
        return this.connPollService;
    }

    public void evictConnection(Connection connection ){
        this.connPollService.evict(connection);
    }

    public void contextInitialized() {
        log.info("contextInitialized-----------");
    }

    public void contextDestroyed() {
       log.info("contextDestroyed----------");
        //销毁数据库连接池
        connPollService.shutdown();
    }
}
