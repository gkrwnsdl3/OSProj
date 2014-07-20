import java.io.Serializable;
import java.util.ArrayList;

public class ChatMessage implements Serializable {
	// 메시지 타입 정의
	// 1개의 메시지 종류 필드와 3개의 String형 필드.
	// NO_ACT는 무시할 수 있는 Dummy 메시지. 디버깅용 등으로 사용하기 위해 만들어 놓음
	// (1) 클라이언트가 보내는 메시지 형식
	//	- LOGIN  : CLIENT 로그인.
	//		메시지 포맷 : LOGIN, "송신자", "", ""
	//	- LOGOUT : CLIENT 로그아웃.
	//		메시지 포맷 : LOGOUT, "송신자", "", ""
	// 	- CLIENT_MSG : 서버에게 보내는  대화 .
	// 		메시지포맷  : CLIENT_MSG, "송신자", "수신자", "내용"
	//	- READY : 서버 or 클라이언트에게 준비가 됬다는 신호
	//		메시지포맷 : READY, "송신자", "", "내용"
	//	- ANSWER : 서버 or 클라이언트에게 답 or 정답유무를 보냄
	//		메시지포맷 : ANSWER, "", "", "답"
	// (2) 서버가 보내는 메시지 형식
	// 	- LOGIN_FAILURE  : 로그인 실패
	//		메시지 포맷 : LOGIN_FAILURE, "", "", "로그인 실패 원인"
	// 	- SERVER_MSG : 클라이언트에게 원격으로 보내는 대화 
	//		메시지포맷  : SERVER_MSG, "송신자", "", "내용" 
	// 	- LOGIN_LIST : 현재 로그인한 사용자 리스트.
	//		메시지 포맷 : LOGIN_LIST, "", "", "/로 구분된 사용자 리스트"
	//	- POSITION : 클라이언트에게 게임 슬롯위치를 알려줌.
	//		메시지 포맷 : POSITION, "", "", "", "", "위치"
	//	- WAIT : 클라이언트에게 대기열을 알려줌.
	//		메시지 포맷 : WAIT, "", "", "", "", "리스트"
	//	- QUESTION : 클라이언트에게 문제출제
	//		메시지 포맷 : QUESTION, "", "", "문제"
	//	- USER : 클라이언트에게 사용자 리스트를 보냄
	//		메시지 포맷 : USER, "", "", "", "", "리스트"
	//	- END : 클라이언트에게 게임이 끝났음을 알려줌.
	//		메시지 포맷 : END, "", "", "", "", "리스트"
	//	- INIT_SCORE : 로그인 한 클라이언트에게 현재 점수를 알려줌
	//	메시지 포맷 : INIT_SCORE, "", "", "", "", "리스트"
	public enum MsgType {NO_ACT, LOGIN, LOGOUT, CLIENT_MSG, READY, ANSWER, LOGIN_FAILURE, SERVER_MSG, LOGIN_LIST, POSITION, WAIT, QUESTION, USER, END, INIT_SCORE};
	public static final String ALL = "전체";	 // 사용자 명 중 자신을 제외한 모든 로그인되어 있는
											 // 사용자를 나타내는 식별문
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
		return ("메시지 종류 : " + type + 
				"\t송신자         : " + sender + 
				"\t수신자         : " + receiver + 
				"\t메시지 내용 : " + contents );
	}
}
