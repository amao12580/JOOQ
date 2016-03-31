package com.study.jooq.base.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC连接池
 */
public interface ConnectionPoolService {
    /**
     * Get a connection from the pool, or timeout after connectionTimeout milliseconds.
     *
     * @return a java.sql.Connection instance
     * @throws SQLException thrown if a timeout occurs trying to obtain a connection
     */
    Connection getConnection() throws SQLException;


    /**
     * Shutdown the pool, closing all idle connections and aborting or closing
     * active connections.
     */
    void shutdown();

    /**
     * evict connection
     * @param connection
     */
    void evict(Connection connection);
}
