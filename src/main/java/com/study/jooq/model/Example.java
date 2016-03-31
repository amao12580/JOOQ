package com.study.jooq.model;

import com.study.jooq.base.utils.ScopedContext;
import com.study.jooq.common.generated.tables.records.UserRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

import static com.study.jooq.common.generated.Tables.USER;

/**
 * Created by Administrator on 2016/3/31.
 */
public class Example {
    private final static Logger log= LoggerFactory.getLogger(Example.class);

    public static void main(String[] args) throws Exception {
        base();
    }

    private static void base() throws Exception {
        try(ScopedContext scopedContext=new ScopedContext()){//try with resource
            DSLContext create=scopedContext.getDSLContext();
            int uid =180;

            //add
            UserRecord userRecord=create.newRecord(USER);
            userRecord.setAge((byte) 18);
            userRecord.setMobile("15985236985");
            userRecord.setName("赵六");
            userRecord.setUid(uid);
            userRecord.setSex((byte) 1);
            userRecord.setPassword(String.valueOf(System.nanoTime()));
            userRecord.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            int insertRet=userRecord.insert();//执行insert sql
            //userRecord.store();//可能会执行insert，也有可能执行update，文档说明的很清晰
            //userRecord.refresh();//从数据库重新加载该记录
            log.info("insertRet:{}", insertRet);

            //index
            int createIndexRet=create.createIndex("user_index_mobile_unique")
                    .on(USER, USER.MOBILE)
                    .execute();//为手机号码字段创建唯一索引
            int dropIndexRet=create.dropIndex("user_index_mobile_unique")
                    .on(USER)
                    .execute();//删除索引
            log.info("dropIndexRet:{},createIndexRet:{}", dropIndexRet, createIndexRet);

            //select
            Record record=create.select(USER.NAME,USER.UID)
                    .from(USER)
                    .where(USER.MOBILE.eq("15985236985"))
                    .limit(1)
                    .fetchOne();
            log.info("姓名:{}，uid:{}", record.getValue(USER.NAME), record.getValue(USER.UID));

            Result<UserRecord> userRecords=create.selectFrom(USER)
                    .where(USER.SEX.eq((byte) 1).and(USER.MOBILE.like("159%")))
                    .orderBy(USER.MOBILE.asc()).limit(0, 20).fetch();

            for (UserRecord ur:userRecords){
                log.info("mobile:{},uid:{},registerTime:{}", ur.getMobile(), ur.getUid(), ur.getRegisterTime().getTime());
            }

            //delete
            int deleteRecordRet=create.deleteFrom(USER).where(USER.UID.eq(uid)).execute();
            log.info("deleteRecordRet:{}", deleteRecordRet);
        }
    }
}
