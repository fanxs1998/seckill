package org.seckill.exception;

/**
 * 秒杀相关业务异常
 * @author fanxs
 * @date 2020/11/23
 */
public class SeckillException extends RuntimeException{
    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeckillException(String message) {
        super(message);
    }
}