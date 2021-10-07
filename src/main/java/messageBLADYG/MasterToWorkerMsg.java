package messageBLADYG;

import java.io.Serializable;

public class MasterToWorkerMsg implements Serializable {

    int ReceiverWorkerID;
    String operationInfo;
    Object object;

    public MasterToWorkerMsg(int workerID, String operationInfo, Object object) {
        super();
        this.ReceiverWorkerID = workerID;
        this.operationInfo = operationInfo;
        this.object = object;
    }

    public int getReceiverWorkerID() {
        return ReceiverWorkerID;
    }

    public void setReceiverWorkerID(int workerID) {
        this.ReceiverWorkerID = workerID;
    }

    public String getOperationInfo() {
        return operationInfo;
    }

    public void setOperationInfo(String operationInfo) {
        this.operationInfo = operationInfo;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
