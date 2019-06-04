package com.qc.cursoa.exception;

/**
 * @author qc
 * @date 2019/6/3
 * 异常类
 */

public class NotAnnotation extends  RuntimeException {
    /**
     * Don't let anyone else instantiate this class
     */
    public NotAnnotation(String msg) {
        super(msg);
    }
}
