package com.aliter.http;


/**
 * Created by mango on 16/8/18.
 */
public class ApiException extends RuntimeException {
    private int errorCode;
    private String msg;


    public ApiException(int status,String msg) {
        super(getErrorDesc(status,msg));
        this.errorCode = status;
        this.msg=msg;
    }

    private static String getErrorDesc(int status, String msg){
        return StatusUtils.judgeStatus(status,msg).desc;
    }

}
