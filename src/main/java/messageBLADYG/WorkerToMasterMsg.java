package messageBLADYG;

import java.io.Serializable;

public class WorkerToMasterMsg  implements Serializable{
int SenderWorkerID;
String operationInfo;
Object object;
public WorkerToMasterMsg(int workerID, String operationInfo, Object object) {
	super();
	this.SenderWorkerID = workerID;
	this.operationInfo = operationInfo;
	this.object = object;
}

public int getSenderWorkerID() {
	return SenderWorkerID;
}
public void setSenderWorkerID(int workerID) {
	this.SenderWorkerID = workerID;
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
