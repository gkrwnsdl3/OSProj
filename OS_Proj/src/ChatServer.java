import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

public class ChatServer {
	// 접속한 클라이언트의 사용자 이름과 출력 스트림을 해쉬 테이블에 보관
	// 나중에 특정 사용자에게 메시지를 보낼때 사용. 현재 접속해 있는 사용자의 전체 리스트를 구할때도 사용
	HashMap<String, ObjectOutputStream> clientOutputStreams =
			new HashMap<String, ObjectOutputStream>();

	final int PARTY_MAX=6, TIME_LIMIT=16000, CYCLE=10;
	int gameCounter=0, correct=0, count=0, server_id;
	String pos[] = new String[PARTY_MAX];
	long score[] = new long[PARTY_MAX];
	ArrayList<String> wait = new ArrayList<String>();
	ArrayList<String> ready = new ArrayList<String>();
	boolean gaming=false, db=true;
	QuestionMaker q;
	Timer timer;
	JFrame f;
	JButton button;
	JTextField tfd_hostname, tfd_ip, tfd_port;
	Connection conn;

	public static void main (String[] args) {
		new ChatServer().go();
	}

	private void go () {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {}

		f = new JFrame();
		f.setTitle("Server");
		f.setResizable(false);
		f.setLayout(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(195, 220);
		f.setLayout(null);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension di1 = tk.getScreenSize(); // 스크린사이즈의 사이즈
		Dimension di2 = f.getSize(); // 프레임의 사이즈
		f.setLocation(di1.width / 2 - di2.width / 2, di1.height / 2 - di2.height / 2);

		JLabel host_name = new JLabel("HostName :");
		host_name.setBounds(10, 10, 100, 30);
		f.add(host_name);

		tfd_hostname = new JTextField("");
		tfd_hostname.setEditable(false);
		tfd_hostname.setBounds(80, 10, 100, 30);
		f.add(tfd_hostname);

		JLabel ip = new JLabel("IP :");
		ip.setBounds(10, 50, 20, 30);
		f.add(ip);

		tfd_ip = new JTextField();
		tfd_ip.setEditable(false);
		tfd_ip.setBounds(35, 50, 145, 30);
		f.add(tfd_ip);

		JLabel port = new JLabel("★ set Port :");
		port.setBounds(10, 90, 70, 30);
		f.add(port);

		tfd_port = new JTextField("5000");
		tfd_port.setBackground(Color.yellow);
		tfd_port.setBounds(75, 90, 105, 30);
		f.add(tfd_port);

		button = new JButton("서버 실행");
		button.addActionListener(new ButtonListener());
		button.setBounds(10, 130, 170, 50);
		f.add(button);

		f.setVisible(true);

	}

	public class ButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			int port = Integer.valueOf(tfd_port.getText());
			if (port<5000 || port>=6000){
				JOptionPane.showMessageDialog(null, "port는 5000~5999 사이의 값을 입력해야합니다.");
			}
			else if (button.getText().equals("서버 실행")){
				dbConnectionInit("localhost", "ozg", "root", "a");
				//dbConnectionInit("os2.sch.ac.kr", "d20105199", "20105199", "rlagkrwns");
				Thread th = new Thread(new thTh());
				th.start();
			}
			else{
				int choice = JOptionPane.showConfirmDialog(null, "서버를 끝내시겠습니까?");
				if (choice == JOptionPane.YES_OPTION) {
					if (db){
						try {
							Statement stmt = conn.createStatement();
							stmt.executeUpdate("delete from osProj_server where no=" + server_id);
							stmt.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.exit(0);
				}
			}
		}

	}

	public void dbConnectionInit(String address, String dbName, String id, String pw) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + address + "/" + dbName, id, pw); // DB 연결하기
		} catch (ClassNotFoundException | SQLException e) {
			//e.printStackTrace();
			int choice = JOptionPane.showConfirmDialog(null, "서버DB에 접속할 수 없습니다.\n계속하시겠습니까?");
			if (choice == JOptionPane.YES_OPTION) {
				db=false;
			}
			else{
				JOptionPane.showMessageDialog(null, "프로그램을 종료합니다.");
				System.exit(0);
			}
		} // JDBC드라이버를 JVM영역으로 가져오기
	}

	public class thTh implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String hostname=InetAddress.getLocalHost().getHostName(), ip=InetAddress.getLocalHost().getHostAddress();
				int port=Integer.valueOf(tfd_port.getText());
				ServerSocket serverSock = new ServerSocket(Integer.valueOf(tfd_port.getText()));	// 채팅을 위한 소켓 포트 5000 사용
				//System.out.println(InetAddress.getLocalHost().getHostName());
				tfd_hostname.setText(hostname);
				//System.out.println(InetAddress.getLocalHost().getHostAddress());
				tfd_ip.setText(ip);

				for (int c1=0; c1<PARTY_MAX; c1++){
					pos[c1]="";
				}

				if (db){
					Statement stmt = conn.createStatement();
					stmt.executeUpdate("INSERT INTO osProj_server (name, ip, socket) VALUES ('" + hostname + "','" + ip + "'," + port + ");");
					ResultSet rs = stmt.executeQuery("select * from osProj_server");
					while (rs.next()){
						if (rs.getString("name").equals(hostname) && rs.getString("ip").equals(ip) && rs.getInt("socket")==port){
							server_id=rs.getInt("no");
							break;
						}
					}
					stmt.close();
				}

				button.setText("서버를 종료하려면 클릭");
				tfd_port.setEditable(false);

				while(true) {
					Socket clientSocket = serverSock.accept();		// 새로운 클라이언트 접속 대기

					// 클라이언트를 위한 입출력 스트림 및 스레드 생성
					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();									
					System.out.println("S : 클라이언트 연결 됨");		// 상태를 보기위한 출력 메시지
				}
			}
			catch(Exception ex) {
				System.out.println("S : 클라이언트  연결 중 이상발생");	// 상태를 보기위한 출력  메시지
				ex.printStackTrace();
				if (ex.getMessage().equals(("Address already in use: JVM_Bind"))){
					JOptionPane.showMessageDialog(null, "같은 포트를 사용하는 서버가 존재합니다.");	
				}
			}
		}

	}

	// Client 와 1:1 대응하는 메시지 수신 스레드
	private class ClientHandler implements Runnable {
		Socket sock;					// 클라이언트 연결용 소켓
		ObjectInputStream reader;		// 클라이언트로 부터 수신하기 위한 스트림
		ObjectOutputStream writer;		// 클라이언트로 송신하기 위한 스트림

		// 구성자. 클라이언트와의 소켓에서 읽기와 쓰기 스트림 만들어 냄
		// 스트림을 만들때 InputStream을 먼저 만들면 Hang함. 그래서 OutputStream먼저 만들었음.
		// 이유는 클라이언트가 InputStream을 먼저 만들기 떄문임.
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				writer = new ObjectOutputStream(clientSocket.getOutputStream());
				reader = new ObjectInputStream(clientSocket.getInputStream());
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		// 클라이언트에서 받은 메시지에 따라 상응하는 작업을 수행
		public void run() {
			ChatMessage message;
			ChatMessage.MsgType type;
			try {
				while (true) {
					// 읽은 메시지의 종류에 따라 각각 할일이 정해져 있음
					message = (ChatMessage) reader.readObject();	  // 클라이언트의 전송 메시지 받음
					type = message.getType();
					if (type == ChatMessage.MsgType.LOGIN) {		  // 클라이언트 로그인 요청
						handleLogin(message.getSender(),writer);	  // 클아이언트 이름과 그에게 메시지를
						// 보낼 스트림을 등록
					}
					else if (type == ChatMessage.MsgType.LOGOUT) {	  // 클라이언트 로그아웃 요청
						if (message.getContents()!=null){
							pos[Integer.valueOf(message.getContents())]="";
						}
						handleLogout(message.getSender());			  // 등록된 이름 및 이와 연결된 스트림 삭제
						writer.close(); reader.close(); sock.close(); // 이 클라이언트와 관련된 스트림들 닫기
						System.out.println("S : '" + message.getSender() + "' Logout");
						return;										  // 스레드 종료
					}
					else if (type == ChatMessage.MsgType.CLIENT_MSG) {
						handleMessage(message.getSender(), message.getReceiver(), message.getContents());
					}
					else if (type==ChatMessage.MsgType.READY) {
						handleReady(message);
					}
					else if (type==ChatMessage.MsgType.ANSWER) {
						handleAnswer(message);
					}

					else if (type == ChatMessage.MsgType.NO_ACT) {
						//  무시해도 되는 메시지
						continue;
					}
					else {
						// 정체가 확인되지 않는 이상한 메시지?
						throw new Exception("S : 클라이언트에서 알수 없는 메시지 도착했음");
					}
				}
			} catch(Exception ex) {
				System.out.println("S : 클라이언트 접속 종료");			// 연결된 클라이언트 종료되면 예외발생
				// 이를 이용해 스레드 종료시킴
				//ex.printStackTrace();
			}
		} // close run
	} // close inner class

	private synchronized void handleAnswer(ChatMessage message) {
		long obtain=TIME_LIMIT-(long)message.getObj();
		if (message.getContents().equals(q.answer)){
			//broadcastMessage(new ChatMessage(ChatMessage.MsgType.ANSWER, "", "", message.getSender(), null));
			for (int c1=0; c1<PARTY_MAX; c1++){
				if (pos[c1].equals(message.getSender())){
					score[c1]+=obtain;
					broadcastMessage(new ChatMessage(ChatMessage.MsgType.ANSWER, "", message.getSender(), "T", obtain));
					correct++;
					if (correct==ready.size()){
						//System.out.println("모두맞춤!");
						correct=0;
						broadcastMessage(new ChatMessage(ChatMessage.MsgType.ANSWER, "", "", "AT", obtain));
						if (gameCounter<CYCLE){
							timer.stop();
							gameStart();
						}
						else if (gameCounter==CYCLE){
							timer.stop();
							gameEnd();
						}
					}
					break;
				}
			}
		}
		else{
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.ANSWER, "", message.getSender(), "F", obtain));
		}
	}

	private synchronized void handleReady(ChatMessage message) {
		if (message.getContents().equals("T")){
			ready.add(message.getSender());
		}
		else if (message.getContents().equals("F")){
			for (int c1=0; c1<ready.size(); c1++){
				if (ready.get(c1).equals(message.getSender())){
					ready.remove(c1);
				}
			}
		}
		readyCheck();
	}

	void readyCheck(){
		count=0;
		for (int c1=0; c1<pos.length; c1++){
			if (!pos[c1].equals("")){
				count++;
			}
		}
		if (count==ready.size() && count>0){
			if (count==1){
				ready.remove(0);
				broadcastMessage(new ChatMessage(ChatMessage.MsgType.READY, "", "", "혼자레디", null));
			}
			else{
				//System.out.println("All Ready!");
				broadcastMessage(new ChatMessage(ChatMessage.MsgType.READY, "", "", String.valueOf(count), null));
				broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "◈", "", "게임이 시작되었습니다.", null));
				gaming=true;
				gameStart();
			}
		}
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.READY, "", "", "", ready));
	}

	void gameStart(){
		gameCounter++;
		System.out.println("gameCounter : " + gameCounter);
		correct=0;
		q = new QuestionMaker((gameCounter/9)+1);
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.QUESTION, "", "", q.answer, q.list));
		timer = new Timer(TIME_LIMIT+5000, new Times());
		timer.start();
	}

	class Times implements ActionListener{
		public void actionPerformed (ActionEvent event){
			timer.stop();
			if (gameCounter<CYCLE){
				gameStart();
			}
			else{
				gameEnd();
			}
		}
	}

	void gameEnd(){
		gaming=false;
		gameCounter=0;
		ready.clear();
		for (int c1=0; c1<PARTY_MAX; c1++){
			score[c1]=0;
		}
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.END, "", "", "", null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "◈", "", "게임이 종료되었습니다.", null));
		System.out.println("wait : " + wait);
		while (wait.size()>0){
			positioning(wait.remove(0));
		}
		bcMSG();
	}

	void positioning(String user){
		int c1;
		boolean flag=false;
		if (!gaming){
			for (c1=0; c1<pos.length; c1++){
				if (pos[c1]==""){
					pos[c1]=user;
					flag=true;
					//System.out.println(user + "에게 자리배정 : " + pos[c1]);
					broadcastMessage(new ChatMessage(ChatMessage.MsgType.POSITION, "", "", user+"/"+String.valueOf(c1), null));
					break;
				}
			}
		}
		if (!flag){
			//System.out.println("슬롯초과로인한 대기열추가");
			wait.add(user);
		}
	}

	void bcMSG(){
		// 새로운 로그인 리스트를 전체에게 보내 줌
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.LOGIN_LIST, "", "", makeClientList(), null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.WAIT, "", "", "", wait));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.USER, "", "", "", pos));

		if (!gaming){
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "", "", "모든 유저가 준비하면 게임이 시작됩니다.", null));				
		}
		else{
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "", "", "게임이 진행 중입니다.", null));
		}
	}

	// 사용자 이름과 클라이언트로의 출력 스트림과 연관지어 해쉬 테이블에 넣어줌.
	// 이미 동일한 이름의 사용자가 있다면, 현재의 로그인은 실패 한것으로 클라이언트에게 알림
	// 그리고 새로운 접속자 리스트를 모든 접속자에게 보내줌
	// 해쉬 테이블의 접근에서는 경쟁조건 생기면 곤란 (not Thread-Safe. Synchronized로 상호배제 함.
	private synchronized void handleLogin(String user, ObjectOutputStream writer) {

		//System.out.println("handleLogin() Login : " + user);
		try {
			// 이미 동일한 이름의 사용자가 있다면, 현재의 로그인은 실패 한것으로 클라이언트에게 알림
			if (clientOutputStreams.containsKey(user)) {
				writer.writeObject(
						new ChatMessage(ChatMessage.MsgType.LOGIN_FAILURE, "", "", "사용자 이미 있음", null));
				return;
			}
		} catch (Exception ex) {
			System.out.println("S : 서버에서 송신 중 이상 발생");
			ex.printStackTrace();
		}
		// 해쉬테이블에 사용자-전송스트림 페어를 추가하고 새로운 로그인 리스트를 모두에게 알림
		clientOutputStreams.put(user, writer);

		positioning(user);
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "◈", "", user + "님이 로그인하였습니다.", null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.INIT_SCORE, "", "", String.valueOf(count), score));
		bcMSG();

		//listChecker();
	}  // close handleLogin

	// 주어진 사용자를 해쉬테이블에서 제거 (출력 스트림도 제거)
	// 그리고 업데이트된 접속자 리스트를 모든 접속자에게 보내줌
	private synchronized void handleLogout(String user) {
		clientOutputStreams.remove(user);
		if (!gaming){
			if (wait.size()>0){
				boolean flag=false;
				String id="";
				int wait_Size=wait.size();
				for (int c1=0; c1<wait_Size; c1++){
					if (wait.get(c1).equals(user)){
						//System.out.println("wait_loop");
						id=wait.remove(c1);
						flag=true;
						break;
					}
				}
				if (!flag){
					//System.out.println("wait_flag");
					id=wait.remove(0);
				}
				for (int c1=0; c1<pos.length; c1++){
					if (pos[c1]==""){
						pos[c1]=id;
						//System.out.println("handleLogout() id : "+id + ", pos : "+c1);
						broadcastMessage(new ChatMessage(ChatMessage.MsgType.POSITION, "", "", id+"/"+c1, null));
						break;
					}
				}
			}

			for (int c1=0; c1<ready.size(); c1++){
				if (ready.get(c1).equals(user)){
					ready.remove(c1);
					break;
				}
			}
			readyCheck();
			//listChecker();
		}

		broadcastMessage(new ChatMessage(ChatMessage.MsgType.WAIT, "", "", "", wait));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.USER, "", "", "", pos));
		// 새로운 로그인 리스트를 전체에게 보내 줌
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.LOGIN_LIST, "", "", makeClientList(), null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "◈", "", user + "님이 로그아웃하였습니다.", null));

	}  // close handleLogout

	// 클라이언트가 대화 상대방에게 보내는 메시지. 그 상대 혹은 "전체"에게 보내 주어야 함
	private synchronized void handleMessage(String sender, String receiver, String contents) {
		// 여기서 모두에게 보내는 경우를 처리해야 함
		if (receiver.equals(ChatMessage.ALL)) {			// "전체"에게 보내는 메시지이면
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, sender, "", contents, null));
			return;
		}
		// 특정 상대에게 보내는 경우라면
		ObjectOutputStream write = clientOutputStreams.get(receiver);
		try {
			write.writeObject(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, sender, "", contents, true));
		} catch (Exception ex) {
			System.out.println("S : 서버에서 송신 중 이상 발생");
			ex.printStackTrace();
		}
	}  // close handleIncomingMessage

	// 해쉬맵에 있는 모든 접속자들에게 주어진 메시지를 보내는 메소드.
	// 반드시 synchronized 된 메소드에서만 호출하기로 함
	private void broadcastMessage(ChatMessage message) {
		Set<String> s = clientOutputStreams.keySet();	// 먼저 등록된 사용자들을 추출하고 하나하나에 메시지 보냄
		// 그러기 위해서 먼저 사용자 리스트만 추출
		Iterator<String> it = s.iterator();
		String user;
		while(it.hasNext()) {
			user = it.next();
			try {
				ObjectOutputStream writer = clientOutputStreams.get(user);	// 대상 사용자와의 스트림 추출
				writer.writeObject(message);									// 그 스트림에 출력
				writer.flush();
				writer.reset();
			} catch(Exception ex) {
				System.out.println("S : 서버에서 송신 중 이상 발생");
				ex.printStackTrace();
			}
		} // end while	   
	}	// end broadcastMessage

	private String makeClientList() {
		Set<String> s = clientOutputStreams.keySet();	// 먼저 등록된 사용자들을 추출
		Iterator<String> it = s.iterator();
		String userList = "";
		while(it.hasNext()) {
			userList += it.next() + "/";					// 스트링 리스트에 추가하고 구분자 명시
		} // end while
		return userList;									 
	}	// makeClientList

	void listChecker(){
		int c1;
		for (c1=0; c1<pos.length; c1++){
			System.out.println("pos["+c1+"] : " + pos[c1]);
		}
		for (c1=0; c1<wait.size(); c1++){
			System.out.println("wait["+c1+"] : " + wait.get(c1));
		}
		System.out.println("");
	}
}
