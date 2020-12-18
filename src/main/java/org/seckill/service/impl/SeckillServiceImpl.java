package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fanxs
 * @date 2020/11/23
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    //日志对象slf4g
    private static final Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

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
        // 优化点:缓存优化:超时的基础上维护一致性
        //访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill==null){
                return new Exposer(false,seckillId);
            }else{
                redisDao.putSeckill(seckill);
            }
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

        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    /**
     * 使用注解控制事务方法的优点：
     * 1.老发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/Http请求，或者剥离到事务方法外
     * 3.不是所有的方法都需要事务，如只有一条修改操作
     */
    @Override
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException {
        //将用户传来的md5与内部md5进行比较
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw  new SeckillException("seckill data rewrite!");
        }
        //执行秒杀逻辑，减库存+记录购买行为
        Date nowTime = new Date();
        try{
            //记录购买行为
            //唯一：seckillId,userPhone
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            if(insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated!");
            }else{
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
                if(updateCount <= 0){
                    //没有更新到记录，秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed！");
                }else{
                    //秒杀成功 commit
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

    /**
     * 原本没有调用存储过程的执行秒杀操作之所以要抛出RuntimException，
     * 是为了让Spring事务管理器能够在秒杀不成功的时候进行回滚操作。
     * 而现在我们使用了存储过程，有关事务的提交或回滚已经在procedure里完成了，无需抛出
     *
     * 执行秒杀 by存储过程
     * @param seckillId 商品id
     * @param userPhone 秒杀手机号
     * @param md5 秒杀地址md5加密
     * @return 执行秒杀结果
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        //将用户传来的md5与内部md5进行比较
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return  new SeckillExecution(seckillId,SeckillStatEnum.DATE_REWRITE);
        }
        //执行秒杀逻辑，减库存+记录购买行为
        Date killTime = new Date();
        Map map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);


        try{
            //执行存储过程完，result被赋值
            seckillDao.killByProcedure(map);
            //获取result,需要引入common依赖
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,sk);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
        }
    }
}
