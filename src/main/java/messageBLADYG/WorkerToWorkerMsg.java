package messageBLADYG;

import java.io.Serializable;

public class WorkerToWorkerMsg implements Serializable {

    int senderID;
    int receiverID;
    String operationInfo;
    Object object;

    public WorkerToWorkerMsg(int senderID, int receiverID, String operationInfo, Object object) {
        super();
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.operationInfo = operationInfo;
        this.object = object;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
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
