package org.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用于存放秒杀成功的用户信息和相应商品信息
 * @author fanxs
 * @date 2020/10/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessKilled {
    /*`seckill_id` bigint NOT NULL COMMENT '秒杀商品ID',
            `user_phone` bigint NOT NULL COMMENT '用户手机号',
            `state` tinyint NOT NULL DEFAULT -1 COMMENT '状态标识：-1：无效 0：成功 1：已付款 2：已发货',
            `create_time` TIMESTAMP NOT NULL COMMENT '创建时间',*/
    private long seckillId;
    private long userPhone;//bigint-8个字节
    private short state;//2个字节  tinyint-1个字节
    private Date createTime;

    //多对一，因为一件商品在库存中有很多数量，对应的购买明细也有很多。
    //复合属性
    private Seckill seckill;

}
