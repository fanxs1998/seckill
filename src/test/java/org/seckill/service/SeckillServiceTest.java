package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author fanxs
 * @date 2020/11/24
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit Spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list=seckillService.getSeckillList();
        logger.info("list={}", list);
    }

    @Test
    public void getById() {
        long seckillId=1000;
        Seckill seckill=seckillService.getById(seckillId);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long seckillId=1000;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        logger.info("exposer={}", exposer);
        //exposer=Exposer(exposed=true, md5=f8d1fd269fc185219a51f4c4499bfc7e, seckillId=1000, now=0, start=0, end=0)
    }


    @Test
    public void executeSeckill() {
        long seckillId = 1000;
        long userPhone = 13122288888L;
        String md5 = "f8d1fd269fc185219a51f4c4499bfc7e";
        try{
            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId,userPhone,md5);
            logger.info("seckillExecution={}",seckillExecution);
        }catch (RepeatKillException e){
            logger.error(e.getMessage());
        }catch (SeckillCloseException e1){
            logger.error(e1.getMessage());
        }

       // seckillExecution=SeckillExecution(
        // seckillId=1000, state=1, stateInfo=秒杀成功,
        // successKilled=SuccessKilled(seckillId=1000, userPhone=13122288888, state=0, createTime=Tue Nov 24 17:59:31 CST 2020,
        // seckill=Seckill(seckillId=1000, name=1000元秒杀iPhone6, number=99, startTime=Thu Jun 20 00:00:00 CST 2019, endTime=Thu Jun 21 00:00:00 CST 2029, createTime=Tue Oct 27 17:25:27 CST 2020)))
    }

    //集成测试代码完整逻辑，注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        long seckillId=1000;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed())
        {
            logger.info("exposer={}", exposer);
            long userPhone=13476191876L;
            String md5=exposer.getMd5();

            try {
                SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
                logger.info("result={}", execution);
            }catch (RepeatKillException e)
            {
                logger.error(e.getMessage());
            }catch (SeckillCloseException e1)
            {
                logger.error(e1.getMessage());
            }
        }else {
            //秒杀未开启
            logger.warn("exposer={}", exposer);
        }
    }


}
