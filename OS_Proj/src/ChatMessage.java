import java.io.Serializable;
import java.util.ArrayList;

public class ChatMessage implements Serializable {
	// �޽��� Ÿ�� ����
	// 1���� �޽��� ���� �ʵ�� 3���� String�� �ʵ�.
	// NO_ACT�� ������ �� �ִ� Dummy �޽���. ������ ������ ����ϱ� ���� ����� ����
	// (1) Ŭ���̾�Ʈ�� ������ �޽��� ����
	//	- LOGIN  : CLIENT �α���.
	//		�޽��� ���� : LOGIN, "�۽���", "", ""
	//	- LOGOUT : CLIENT �α׾ƿ�.
	//		�޽��� ���� : LOGOUT, "�۽���", "", ""
	// 	- CLIENT_MSG : �������� ������  ��ȭ .
	// 		�޽�������  : CLIENT_MSG, "�۽���", "������", "����"
	//	- READY : ���� or Ŭ���̾�Ʈ���� �غ� ��ٴ� ��ȣ
	//		�޽������� : READY, "�۽���", "", "����"
	//	- ANSWER : ���� or Ŭ���̾�Ʈ���� �� or ���������� ����
	//		�޽������� : ANSWER, "", "", "��"
	// (2) ������ ������ �޽��� ����
	// 	- LOGIN_FAILURE  : �α��� ����
	//		�޽��� ���� : LOGIN_FAILURE, "", "", "�α��� ���� ����"
	// 	- SERVER_MSG : Ŭ���̾�Ʈ���� �������� ������ ��ȭ 
	//		�޽�������  : SERVER_MSG, "�۽���", "", "����" 
	// 	- LOGIN_LIST : ���� �α����� ����� ����Ʈ.
	//		�޽��� ���� : LOGIN_LIST, "", "", "/�� ���е� ����� ����Ʈ"
	//	- POSITION : Ŭ���̾�Ʈ���� ���� ������ġ�� �˷���.
	//		�޽��� ���� : POSITION, "", "", "", "", "��ġ"
	//	- WAIT : Ŭ���̾�Ʈ���� ��⿭�� �˷���.
	//		�޽��� ���� : WAIT, "", "", "", "", "����Ʈ"
	//	- QUESTION : Ŭ���̾�Ʈ���� ��������
	//		�޽��� ���� : QUESTION, "", "", "����"
	//	- USER : Ŭ���̾�Ʈ���� ����� ����Ʈ�� ����
	//		�޽��� ���� : USER, "", "", "", "", "����Ʈ"
	//	- END : Ŭ���̾�Ʈ���� ������ �������� �˷���.
	//		�޽��� ���� : END, "", "", "", "", "����Ʈ"
	//	- INIT_SCORE : �α��� �� Ŭ���̾�Ʈ���� ���� ������ �˷���
	//	�޽��� ���� : INIT_SCORE, "", "", "", "", "����Ʈ"
	public enum MsgType {NO_ACT, LOGIN, LOGOUT, CLIENT_MSG, READY, ANSWER, LOGIN_FAILURE, SERVER_MSG, LOGIN_LIST, POSITION, WAIT, QUESTION, USER, END, INIT_SCORE};
	public static final String ALL = "��ü";	 // ����� �� �� �ڽ��� ������ ��� �α��εǾ� �ִ�
											 // ����ڸ� ��Ÿ���� �ĺ���
	private MsgType type;
	private String sender;
	private String receiver;
	private String contents;
	private Object ob1;

	public ChatMessage() {
		this(MsgType.NO_ACT, "", "", "", null);
	}
	public ChatMessage(MsgType t, String sID, String rID, String mesg, Object o) {
		type = t;
		sender = sID;
		receiver = rID;
		contents = mesg;
		ob1=o;
	}
	
	public Object getObj(){
		return ob1;
	}
	
	public void setType (MsgType t) {
		type = t;
	}
	public MsgType getType() {
		return type;
	}

	public void setSender (String id) {
		sender = id;
	}
	public String getSender() {
		return sender;
	}
	
	public void setReceiver (String id) {
		receiver = id;
	}
	public String getReceiver() {
		return receiver;
	}
	
	public void setContents (String mesg) {
		contents = mesg;
	}
	public String getContents() {
		return contents;
	}
	
	public String toString() {
		return ("�޽��� ���� : " + type + 
				"\t�۽���         : " + sender + 
				"\t������         : " + receiver + 
				"\t�޽��� ���� : " + contents );
	}
}
