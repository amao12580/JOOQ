package com.study.jooq.model;

import com.study.jooq.base.utils.ScopedContext;
import com.study.jooq.common.generated.tables.records.OrderRecord;
import com.study.jooq.common.generated.tables.records.UserRecord;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static com.study.jooq.common.generated.Tables.*;

/**
 * Created by Administrator on 2016/3/31.
 */
public class Example {
    private final static Logger log = LoggerFactory.getLogger(Example.class);

    public static void main(String[] args) throws Exception {
        //base();
        //sunQuery();
        //advance();
        //batch();
        //function();
        //procedure();
        //view();
        //reuseStatement();
    }


    private static void base() throws Exception {
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            //add
            UserRecord userRecord = create.newRecord(USER);
            userRecord.setAge((byte) 18);
            userRecord.setMobile("15985236985");
            userRecord.setName("赵六");
            userRecord.setSex((byte) 1);
            userRecord.setPassword(String.valueOf(System.nanoTime()));
            userRecord.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            int insertRet = userRecord.insert();//执行insert sql
            //userRecord.store();//可能会执行insert，也有可能执行update，文档说明的很清晰
            //userRecord.refresh();//从数据库重新加载该记录
            log.info("insertRet:{}", insertRet);
            log.info("自增长的uid:{}", userRecord.getUid());

            //index
            int createIndexRet = create.createIndex("user_index_mobile_unique")
                    .on(USER, USER.MOBILE)
                    .execute();//为手机号码字段创建唯一索引
            int dropIndexRet = create.dropIndex("user_index_mobile_unique")
                    .on(USER)
                    .execute();//删除索引
            log.info("dropIndexRet:{},createIndexRet:{}", dropIndexRet, createIndexRet);

            //select
            Record record = create.select(USER.NAME, USER.UID)
                    .from(USER)
                    .where(USER.MOBILE.eq("15985236985"))
                    .limit(1)
                    .fetchOne();
            log.info("姓名:{}，uid:{}", record.getValue(USER.NAME), record.getValue(USER.UID));

            Result<UserRecord> userRecords = create.selectFrom(USER)
                    .where(USER.SEX.eq((byte) 1).and(USER.MOBILE.like("159%")))
                    .orderBy(USER.MOBILE.asc()).limit(0, 20).fetch();

            for (UserRecord ur : userRecords) {
                log.info("mobile:{},uid:{},registerTime:{}", ur.getMobile(), ur.getUid(), ur.getRegisterTime().getTime());
            }

            //delete
            int deleteRecordRet = create.deleteFrom(USER).where(USER.UID.eq(userRecord.getUid())).execute();
            log.info("deleteRecordRet:{}", deleteRecordRet);
        }
    }

    private static void sunQuery() throws Exception{
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            //查询指定多个用户的最新一个订单信息
            Set<Integer> uids=new HashSet<>();
            uids.add(10001);
            uids.add(10002);
            uids.add(10003);
            //构建内层查询语句
            Table<OrderRecord> subTable = create.selectFrom(ORDER).
                    where(ORDER.UID.in(uids)).
                    orderBy(ORDER.ORDER_TIME.desc()).
                    asTable("A");

            Result<OrderRecord> oreders = create.selectFrom(subTable).
                    groupBy(subTable.field(0), subTable.field(1)).//按照第一个、第二个字段进行group by
                    fetch();
        }
    }

    private static void advance() throws Exception {
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            final int[] uid = new int[1];
            //transaction
            create.transaction(configuration -> {
                //add
                UserRecord userRecord = create.newRecord(USER);
                userRecord.setAge((byte) 18);
                userRecord.setMobile("18525874884");
                userRecord.setName("赵六");
                userRecord.setSex((byte) 1);
                userRecord.setPassword(String.valueOf(System.nanoTime()));
                userRecord.setRegisterTime(new Timestamp(System.currentTimeMillis()));
                int insertUserRet = userRecord.insert();//执行insert sql
                uid[0] = userRecord.getUid();
                log.info("insertUserRet:{}", insertUserRet);
                //add
                OrderRecord orderRecord = create.newRecord(ORDER);
                orderRecord.setUid(userRecord.getUid());
                orderRecord.setAmout(25000l);
                orderRecord.setOrderId(new BigDecimal(System.nanoTime()).intValue());
                orderRecord.setOrderTime(new Timestamp(System.currentTimeMillis()));
                orderRecord.setStatus((byte) 0);
                int insertOrderRet = orderRecord.insert();//执行insert sql
                log.info("insertOrderRet:{}", insertOrderRet);
            });

            //join select
            Result<Record6<String, String, Byte, Integer, Long, Timestamp>> results = create
                    .select(USER.MOBILE, USER.NAME, USER.AGE, ORDER.ORDER_ID, ORDER.AMOUT, ORDER.ORDER_TIME)
                    .from(USER).leftOuterJoin(ORDER)
                    .on(USER.UID.eq(ORDER.UID))
                    .where(USER.UID.eq(uid[0]).and(ORDER.AMOUT.ge(100l)))
                    .limit(0, 10).fetch();

            //2张表完成左外连接后的Step
            SelectForUpdateStep sfus = create
                    .select(USER.MOBILE, USER.NAME, USER.AGE, ORDER.ORDER_ID, ORDER.AMOUT, ORDER.ORDER_TIME)
                    .from(USER).leftOuterJoin(ORDER)
                    .on(USER.UID.eq(ORDER.UID));

            //2张表查询语句构建结束后的Step
            SelectForUpdateStep sfus1 = create
                    .select(USER.MOBILE, USER.NAME, USER.AGE, ORDER.ORDER_ID, ORDER.AMOUT, ORDER.ORDER_TIME)
                    .from(USER).leftOuterJoin(ORDER)
                    .on(USER.UID.eq(ORDER.UID))
                    .where(USER.UID.eq(uid[0]).and(ORDER.AMOUT.ge(100l)))
                    .limit(0, 10);
            log.info("s:" + sfus.getSQL());
            log.info("s1:" + sfus.getSQL());

            for (Record6<String, String, Byte, Integer, Long, Timestamp> record : results) {
                log.info("姓名:{}，手机号码:{}，年龄:{}，订单号:{}，订单金额:{}，订单时间:{}",
                        record.getValue(USER.NAME), record.getValue(USER.MOBILE), record.getValue(USER.AGE),
                        record.getValue(ORDER.ORDER_ID), record.getValue(ORDER.AMOUT),
                        record.getValue(ORDER.ORDER_TIME).getTime());
            }

            // 使用内置函数
            Record record = create.select(USER.AGE.avg()).from(USER).fetchOne();//求用户的平均年龄
            if (record != null) {
                log.info("平均年龄是:" + record.into(Double.class).toString());
            }
        }
    }

    private static void batch() throws Exception {
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            List<UserRecord> list = new ArrayList<>();
            //batchInsert
            UserRecord userRecord = create.newRecord(USER);
            userRecord.setAge((byte) 18);
            userRecord.setMobile("17058963215");
            userRecord.setName("赵六");
            userRecord.setSex((byte) 1);
            userRecord.setPassword(String.valueOf(System.nanoTime()));
            userRecord.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            list.add(userRecord);

            UserRecord userRecord2 = create.newRecord(USER);
            userRecord2.setAge((byte) 29);
            userRecord2.setMobile("17058963216");
            userRecord2.setName("马七");
            userRecord2.setSex((byte) 1);
            userRecord2.setPassword(String.valueOf(System.nanoTime()));
            userRecord2.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            list.add(userRecord2);
            //使用batchInsert时，无法获取SQL语句
            int insertRetArr[] = create.batchInsert(list).execute();//返回值是一个int数组，长度与输入的集合size有关。

            log.info("insertRetArr:{}", Arrays.toString(insertRetArr));//数组每个元素为1时，执行成功
            //使用batchInsert时，无法获取数据自增长的主键值
            log.info("userRecord:uid:{}", userRecord.getUid());
            log.info("userRecord2:uid:{}", userRecord2.getUid());

            userRecord.refresh();
            userRecord2.refresh();
            log.info("userRecord:uid:{}", userRecord.getUid());
            log.info("userRecord2:uid:{}", userRecord2.getUid());

            //batchUpdate
            userRecord.setAge((byte) 38);
            userRecord2.setAge((byte) 78);
            list.clear();
            list.add(userRecord);
            list.add(userRecord2);
            //使用batchUpdate时，无法获取SQL语句
            int updateRetArr[] = create.batchUpdate(list).execute();//返回值是一个int数组，长度与输入的集合size有关。
            log.info("updateRetArr:{}", Arrays.toString(updateRetArr));//数组每个元素为1时，执行成功

            //batchDelete

            //使用batchDelete时，无法获取SQL语句
            int deleteRetArr[] = create.batchDelete(list).execute();//返回值是一个int数组，长度与输入的集合size有关。
            log.info("deleteRetArr:{}", Arrays.toString(deleteRetArr));//数组每个元素为1时，执行成功
        }
    }

    private static void function() throws Exception {
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            //formatDate是我们在mysql里自定义的函数
            Result<Record> results = create.fetch("SELECT formatDate(NOW()) AS '时间';");
            for (Record record : results) {
                log.info("执行结果:{}", record.getValue(0));
            }
        }
    }

    private static void procedure() throws Exception {
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            //getAllUid是我们在mysql里定义的存储过程
            Result<Record> results = create.fetch("CALL getAllUid()");
            for (Record record : results) {
                log.info("执行结果:{}", record.getValue(0));
            }
        }
    }

    private static void view() throws Exception {
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();
            //创建视图

            //定义视图名称为：userwithorder
            CreateViewFinalStep step = create.createView("userwithorder", USER.UID.getName(), USER.NAME.getName(), ORDER.ORDER_ID.getName(), ORDER.STATUS.getName(), ORDER.AMOUT.getName())
                    .as(
                            create.select(USER.UID, USER.NAME, ORDER.ORDER_ID, ORDER.STATUS, ORDER.AMOUT)
                                    .from(USER)
                                    .leftOuterJoin(ORDER)
                                    .on(USER.UID.eq(ORDER.UID))
                    );
            log.info("SQL:{}", step.getSQL());
            int ret = step.execute();
            log.info("创建视图,执行结果:{}", ret);

            //查询视图
            Result<Record3<Integer, String, Integer>> results = create.select(USERWITHORDER.UID, USERWITHORDER.NAME, USERWITHORDER.ORDER_ID)
                    .from(USERWITHORDER).where(USERWITHORDER.AMOUT.ge(200l)).fetch();
            for (Record3<Integer, String, Integer> record : results) {
                log.info("uid:{}，姓名:{}，订单号:{}",
                        record.getValue(USERWITHORDER.UID), record.getValue(USERWITHORDER.NAME), record.getValue(USERWITHORDER.ORDER_ID));
            }
            //删除视图
            int dropRet = create.dropView("userwithorder").execute();
            log.info("删除视图,执行结果:{}", dropRet);
        }
    }

    private static void reuseStatement() throws Exception {
        //重复使用Statement
        try (ScopedContext scopedContext = new ScopedContext()) {//try with resource
            DSLContext create = scopedContext.getDSLContext();

            // 创建一个被配置保持打开的Statement
            try (ResultQuery<Record1<Integer>> query = create.selectOne().keepStatement(true)) {
                Result<Record1<Integer>> result1 = query.fetch(); // This will lazily create a new PreparedStatement
                log.info("result1:" + result1.toString());
                Result<Record1<Integer>> result2 = query.fetch(); // This will reuse the previous PreparedStatement
                log.info("result2:" + result2.toString());
            }
        }
    }
}
