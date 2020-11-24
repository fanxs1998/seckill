package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * @author fanxs
 * @date 2020/11/23
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    //日志对象slf4g
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //md5盐值字符串，用于混淆md5
    private final String slat = "srweirweru83rhwrhv3n3y8*";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDao.queryById(seckillId);
        if(seckill == null){
            return new Exposer(false,seckillId);
        }
        //如果seckill不为空，则拿到它的开始时间和结束时间
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        //Date类型要用getTime()获取时间
        if(nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }
        //转化特定字符串的过程，不可逆（给出md5也用户无法知道如何转化的）
        String md5 = getMD5(seckillId); //getMD5方法写在下面
        return new Exposer(true,md5,seckillId);
    }

    //MD5
    private String getMD5(long seckillId){
        String base = seckillId + "/" + slat;
        //spring的工具包，用于生成md5
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());

        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1.老发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/Http请求，或者剥离到事务方法外
     * 3.不是所有的方法都需要事务，如只有一条修改操作
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        //将用户传来的md5与内部md5进行比较
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw  new SeckillException("seckill data rewrite!");
        }
        //执行秒杀逻辑，减库存+记录购买行为
        Date nowTime = new Date();
        try{
            int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
            if(updateCount <= 0){
                //没有更新到记录，秒杀结束
                throw new SeckillCloseException("seckill is closed！");
            }else{
                //记录购买行为
                int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
                if(insertCount <= 0){
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated!");
                }else{
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            // 所有的编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error" + e.getMessage());
        }
    }
}
