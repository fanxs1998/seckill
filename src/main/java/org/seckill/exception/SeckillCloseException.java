package org.seckill.exception;

/**
 * 秒杀关闭异常（关闭了还执行秒杀）
 * @author fanxs
 * @date 2020/11/23
 */
public class SeckillCloseException extends SeckillException{
    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeckillCloseException(String message) {
        super(message);
    }
}
