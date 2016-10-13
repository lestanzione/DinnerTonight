package com.empire.android.dinnertonight;

/**
 * Created by lstanzione on 10/6/2016.
 */
public class Operations {

    private static final String TAG = Operations.class.getSimpleName();

    public interface OperationsCallback{

        public void onOperationSuccess(Object returnObject, int operationCode);
        public void onOperationFail(Object returnObject, int operationCode);
        public void onOperationError(Object returnObject, int operationCode);

    }

    OperationsCallback operationsCallback;
    int operationCode;

    public Operations(OperationsCallback operationsCallback, int operationCode){
        if (operationsCallback instanceof OperationsCallback) {
            this.operationsCallback = operationsCallback;
        } else {
            throw new RuntimeException(operationsCallback.toString()
                    + " must implement OperationsCallback");
        }
        this.operationCode = operationCode;
    }



}
