package org.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 暴露秒杀地址DTO（数据传输层)
 * @author fanxs
 * @date 2020/11/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exposer {
    //是否开启秒杀
    private boolean exposed;

    //对秒杀地址加密措施
    private String md5;

    //id为seckillId的商品的秒杀地址
    private long seckillId;

    //系统当前时间(毫秒)
    private long now;

    //秒杀的开启时间
    private long start;

    //秒杀的结束时间
    private long end;

    /**
     * 不同的构造方法方便对象初始化
     * @param exposed
     * @param md5
     * @param seckillId
     */
    public Exposer(boolean exposed, String md5, long seckillId) {
        super();
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public Exposer(boolean exposed,long seckillId,long now,long start,long end){
        super();
        this.exposed = exposed;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public Exposer(boolean exposed, long seckillId) {
        super();
        this.exposed = exposed;
        this.seckillId = seckillId;
    }


}
