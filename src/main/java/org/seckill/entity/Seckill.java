package org.seckill.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 用于存放秒杀商品的相关信息
 * @author fanxs
 * @date 2020/10/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seckill {
    /*`seckill_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品库存ID',
            `name` VARCHAR (120) NOT NULL COMMENT '商品名称',
            `number` INT NOT NULL COMMENT '库存数量',
            `start_time` TIMESTAMP NOT NULL DEFAULT  '0000-00-00 00:00:00' COMMENT '秒杀时间开始',
            `end_time` TIMESTAMP NOT NULL DEFAULT  '0000-00-00 00:00:00' COMMENT '秒杀时间结束',
            `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',*/
    private long seckillId;//商品id
    private String name;//商品名称
    private int number;//商品库存
    private Date startTime;//秒杀开启时间
    private Date endTime;//秒杀结束时间
    private Date createTime;//创建时间

}
