package com.whl.rpc_common;

/**
 * 封装RPC响应
 */
public class RpcResponse {

    private String responseId;

    private Throwable error;

    private Object result;

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
