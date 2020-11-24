package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 *  该接口中前面两个方法返回的都是跟我们业务相关的对象，而后两个方法返回的对象与业务不相关，这两个对象我们用于封装service和web层传递的数据
 *  业务接口，站在“使用者”的角度设计接口，而不是如何实现
 *  三个方面：方法定义粒度，参数（越简练越直接越好），返回类型（retrun 类型（要友好）/异常（有的业务允许抛出异常））
 * @author fanxs
 * @date 2020/11/5
 */
public interface SeckillService {
    /**
     * 查询所有的秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     *查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址，
     * 否则输出系统时间和秒杀时间
     * 防止用户提前拼接出秒杀url通过插件进行秒杀
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，如果传入的md5与内部的不相符，说明用户的url被篡改了，此时拒绝执行秒杀
     * 有可能失败，有可能成功，所以要抛出我们允许的异常
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException;
}
