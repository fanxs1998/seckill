package org.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 要实现对象的IO读写操作，都必须实现Serializable接口
 * <T>创建使用的时候想使用什么类型就指定什么类型
 * 实现Serializable接口,JVM才允许类创建的对象可以通过其IO系统转换为字节数据，从而实现持久化
 * 反序列化的过程中则需要使用serialVersionUID来确定由那个类来加载这个对象
 * @author fanxs
 * @date 2020/12/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    //请求是否成功
    private boolean success;
    private T data;
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
}
