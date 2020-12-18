-- 秒杀执行储存过程
DELIMITER $$ -- 将定界符从;转换为$$
-- 定义储存过程
-- 参数： in输入参数   out输出参数
-- row_count() 返回上一条修改类型sql(delete,insert,update)的影响行数
-- row_count:0:未修改数据 ; >0:表示修改的行数； <0:sql错误
-- 为一个名为seckill的数据库定义一个名为execute_seckill的存储过程，
-- 如果你在连接数据库后使用了这个数据库（即use seckill;），那么这里的定义句子就不能这样写了，会报错（因为存储过程是依赖于数据库的）
CREATE PROCEDURE `seckill`.`execute_seckill`
  (IN v_seckill_id BIGINT, IN v_phone BIGINT,
   IN v_kill_time  TIMESTAMP, OUT r_result INT)
  BEGIN
    DECLARE insert_count INT DEFAULT 0;
    START TRANSACTION;
    INSERT IGNORE INTO success_killed
    (seckill_id, user_phone, create_time)
    VALUES (v_seckill_id, v_phone, v_kill_time);
    SELECT row_count() INTO insert_count;
    IF (insert_count = 0) THEN
      ROLLBACK;
      SET r_result = -1;
    ELSEIF (insert_count < 0) THEN
        ROLLBACK;
        SET r_result = -2;
    ELSE
      UPDATE seckill
      SET number = number - 1
      WHERE seckill_id = v_seckill_id
            AND end_time > v_kill_time
            AND start_time < v_kill_time
            AND number > 0;
      SELECT row_count() INTO insert_count;
      IF (insert_count = 0) THEN
        ROLLBACK;
        SET r_result = 0;
      ELSEIF (insert_count < 0) THEN
          ROLLBACK;
          SET r_result = -2;
      ELSE
        COMMIT;
        SET r_result = 1;
      END IF;
    END IF;
  END;
$$
-- 储存过程定义结束
-- 将定界符重新改为;
DELIMITER ;

-- 查看有哪些存储过程
show procedure status ;
-- 查看存储过程定义（要先选择数据库use seckill）
show create procedure execute_seckill;

-- 定义一个用户变量r_result
SET @r_result = -3;
-- 查看变量
select @r_result;
-- 执行储存过程
CALL execute_seckill(1000, 13417063004, now(), @r_result);
-- 获取结果
SELECT @r_result;

-- 存储过程优化：事务行级锁持有时间
-- QPS 一个秒杀单6000/qps
