package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

/**
 * @author fanxs
 * @date 2020/12/1
 */
@Controller
@RequestMapping("/seckill")//url:模块/资源/{id}/细分
public class SeckillController {
    /**
     * 日志
     */
    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SeckillService seckillService;

    @RequestMapping(name="/list",method = RequestMethod.GET)
    public ModelAndView list(){
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        ModelAndView mav = new ModelAndView("list");
        mav.addObject("list",list);//WEB-INF/jsp/list.jsp
        return mav;
    }


    //@PathVariable 可以将 URL 中占位符参数绑定到控制器处理方法的入参中
    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public ModelAndView detail(@PathVariable("seckillId") Long seckillId) {
        //详情页
        ModelAndView  mav = new ModelAndView();
        if (seckillId == null)
        {
            mav.setViewName("redirect:/seckill/list");
            return mav;
        }

        Seckill seckill=seckillService.getById(seckillId);
        if (seckill==null)
        {
            mav.setViewName("forward:/seckill/list");
            return mav;
        }

        mav.setViewName("detail");
        mav.addObject("seckill",seckill);

        return mav;
    }

    //ajax、json暴露秒杀接口的方法
    @RequestMapping(value = "/{seckillId}/exposer",method = RequestMethod.GET,produces =  {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") long seckillId){
        SeckillResult<Exposer> result;
        try{
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<>(true,exposer);

        }catch (Exception e){
            //java中e.printStackTrace()不要使用，产生错误堆栈字符串到字符串池填满内存空间
            e.printStackTrace();
            result=new SeckillResult<>(false,e.getMessage());
        }
        return result;
    }

    /**
     * 执行秒杀
     * @param seckillId
     * @param md5
     * @param userPhone
     * @return
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "userPhone",required = false) Long userPhone){

        if(userPhone == null){
            return new SeckillResult<>(false,"未注册");
        }
        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
            return new SeckillResult<>(true, execution);
        }catch (RepeatKillException e1){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<>(false,execution);
        }catch (SeckillCloseException e2){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<>(false,execution);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<>(false,execution);
        }

    }

    /**
     * 获取系统时间
     * @return
     */
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time()
    {
        Date now=new Date();
        return new SeckillResult<>(true,now.getTime());
    }

}
