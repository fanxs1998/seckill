package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置spring和junit整合，这样junit在启动时就会加载spring容器
 * @author fanxs
 * @date 2020/10/28
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit Spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    @Resource
    private  SeckillDao seckillDao;//注入Dao实现类依赖

    //减库存
    @Test
    public void reduceNumber() {
        Date killTime = new Date();
        int updateCount = seckillDao.reduceNumber(1000L, killTime);
        System.out.println("updateCount=" + updateCount);
    }
    //根据id查询秒杀的商品信息
    @Test
    public void queryById() {
        long seckillId = 1000L;
        Seckill seckill = seckillDao.queryById(seckillId);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }
    //根据偏移量查询秒杀商品列表
    @Test
    public void queryAll() {
        /**
         * java没有保存形参的记录，要加上@Param让mybatis知道是哪个参数，
         * queryAll(int offset,int limit)-->queryAll(arg0，arg1)
         * SeckillDao中改为queryAll(@Param("offset")int offset,@Param("limit")int limit)
         * 用来匹配SeckillDao中的limit #{offset},#{limit}
         */
        List<Seckill> seckills = seckillDao.queryAll(0, 100);
        for(Seckill seckill : seckills){
            System.out.println(seckill);
        }
    }
}
