package org.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;

/**
 * 封装秒杀执行后的结果
 * 用于判断秒杀是否成功，成功就返回秒杀成功的所有信息(秒杀的商品id、秒杀成功状态、成功信息、用户明细)，
 * 失败就抛出一个我们允许的异常(重复秒杀异常、秒杀结束异常)
 * @author fanxs
 * @date 2020/11/6
 */
@Data
@NoArgsConstructor
public class SeckillExecution {

    private long seckillId;

    //秒杀执行结果的状态
    private int state;

    //状态的明文标识
    private String stateInfo;

    //当秒杀成功时，需要传递秒杀成功的对象回去
    private SuccessKilled successKilled;

    //不同的构造方法，秒杀成功返回所有信息
    public SeckillExecution(long seckillId, SeckillStatEnum seckillStatEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = seckillStatEnum.getState();
        this.stateInfo = seckillStatEnum.getStateInfo();
        this.successKilled = successKilled;
    }

    //秒杀失败
    public SeckillExecution(long seckillId, SeckillStatEnum seckillStatEnum) {
        this.seckillId = seckillId;
        this.state = seckillStatEnum.getState();
        this.stateInfo = seckillStatEnum.getStateInfo();
    }
}
