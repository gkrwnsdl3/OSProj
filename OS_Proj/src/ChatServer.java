import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

public class ChatServer {
	// ������ Ŭ���̾�Ʈ�� ����� �̸��� ��� ��Ʈ���� �ؽ� ���̺� ����
	// ���߿� Ư�� ����ڿ��� �޽����� ������ ���. ���� ������ �ִ� ������� ��ü ����Ʈ�� ���Ҷ��� ���
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
		Dimension di1 = tk.getScreenSize(); // ��ũ���������� ������
		Dimension di2 = f.getSize(); // �������� ������
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

		JLabel port = new JLabel("�� set Port :");
		port.setBounds(10, 90, 70, 30);
		f.add(port);

		tfd_port = new JTextField("5000");
		tfd_port.setBackground(Color.yellow);
		tfd_port.setBounds(75, 90, 105, 30);
		f.add(tfd_port);

		button = new JButton("���� ����");
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
				JOptionPane.showMessageDialog(null, "port�� 5000~5999 ������ ���� �Է��ؾ��մϴ�.");
			}
			else if (button.getText().equals("���� ����")){
				dbConnectionInit("localhost", "ozg", "root", "a");
				//dbConnectionInit("os2.sch.ac.kr", "d20105199", "20105199", "rlagkrwns");
				Thread th = new Thread(new thTh());
				th.start();
			}
			else{
				int choice = JOptionPane.showConfirmDialog(null, "������ �����ðڽ��ϱ�?");
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
			conn = DriverManager.getConnection("jdbc:mysql://" + address + "/" + dbName, id, pw); // DB �����ϱ�
		} catch (ClassNotFoundException | SQLException e) {
			//e.printStackTrace();
			int choice = JOptionPane.showConfirmDialog(null, "����DB�� ������ �� �����ϴ�.\n����Ͻðڽ��ϱ�?");
			if (choice == JOptionPane.YES_OPTION) {
				db=false;
			}
			else{
				JOptionPane.showMessageDialog(null, "���α׷��� �����մϴ�.");
				System.exit(0);
			}
		} // JDBC����̹��� JVM�������� ��������
	}

	public class thTh implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				String hostname=InetAddress.getLocalHost().getHostName(), ip=InetAddress.getLocalHost().getHostAddress();
				int port=Integer.valueOf(tfd_port.getText());
				ServerSocket serverSock = new ServerSocket(Integer.valueOf(tfd_port.getText()));	// ä���� ���� ���� ��Ʈ 5000 ���
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

				button.setText("������ �����Ϸ��� Ŭ��");
				tfd_port.setEditable(false);

				while(true) {
					Socket clientSocket = serverSock.accept();		// ���ο� Ŭ���̾�Ʈ ���� ���

					// Ŭ���̾�Ʈ�� ���� ����� ��Ʈ�� �� ������ ����
					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();									
					System.out.println("S : Ŭ���̾�Ʈ ���� ��");		// ���¸� �������� ��� �޽���
				}
			}
			catch(Exception ex) {
				System.out.println("S : Ŭ���̾�Ʈ  ���� �� �̻�߻�");	// ���¸� �������� ���  �޽���
				ex.printStackTrace();
				if (ex.getMessage().equals(("Address already in use: JVM_Bind"))){
					JOptionPane.showMessageDialog(null, "���� ��Ʈ�� ����ϴ� ������ �����մϴ�.");	
				}
			}
		}

	}

	// Client �� 1:1 �����ϴ� �޽��� ���� ������
	private class ClientHandler implements Runnable {
		Socket sock;					// Ŭ���̾�Ʈ ����� ����
		ObjectInputStream reader;		// Ŭ���̾�Ʈ�� ���� �����ϱ� ���� ��Ʈ��
		ObjectOutputStream writer;		// Ŭ���̾�Ʈ�� �۽��ϱ� ���� ��Ʈ��

		// ������. Ŭ���̾�Ʈ���� ���Ͽ��� �б�� ���� ��Ʈ�� ����� ��
		// ��Ʈ���� ���鶧 InputStream�� ���� ����� Hang��. �׷��� OutputStream���� �������.
		// ������ Ŭ���̾�Ʈ�� InputStream�� ���� ����� ������.
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				writer = new ObjectOutputStream(clientSocket.getOutputStream());
				reader = new ObjectInputStream(clientSocket.getInputStream());
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		// Ŭ���̾�Ʈ���� ���� �޽����� ���� �����ϴ� �۾��� ����
		public void run() {
			ChatMessage message;
			ChatMessage.MsgType type;
			try {
				while (true) {
					// ���� �޽����� ������ ���� ���� ������ ������ ����
					message = (ChatMessage) reader.readObject();	  // Ŭ���̾�Ʈ�� ���� �޽��� ����
					type = message.getType();
					if (type == ChatMessage.MsgType.LOGIN) {		  // Ŭ���̾�Ʈ �α��� ��û
						handleLogin(message.getSender(),writer);	  // Ŭ���̾�Ʈ �̸��� �׿��� �޽�����
						// ���� ��Ʈ���� ���
					}
					else if (type == ChatMessage.MsgType.LOGOUT) {	  // Ŭ���̾�Ʈ �α׾ƿ� ��û
						if (message.getContents()!=null){
							pos[Integer.valueOf(message.getContents())]="";
						}
						handleLogout(message.getSender());			  // ��ϵ� �̸� �� �̿� ����� ��Ʈ�� ����
						writer.close(); reader.close(); sock.close(); // �� Ŭ���̾�Ʈ�� ���õ� ��Ʈ���� �ݱ�
						System.out.println("S : '" + message.getSender() + "' Logout");
						return;										  // ������ ����
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
						//  �����ص� �Ǵ� �޽���
						continue;
					}
					else {
						// ��ü�� Ȯ�ε��� �ʴ� �̻��� �޽���?
						throw new Exception("S : Ŭ���̾�Ʈ���� �˼� ���� �޽��� ��������");
					}
				}
			} catch(Exception ex) {
				System.out.println("S : Ŭ���̾�Ʈ ���� ����");			// ����� Ŭ���̾�Ʈ ����Ǹ� ���ܹ߻�
				// �̸� �̿��� ������ �����Ŵ
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
						//System.out.println("��θ���!");
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
				broadcastMessage(new ChatMessage(ChatMessage.MsgType.READY, "", "", "ȥ�ڷ���", null));
			}
			else{
				//System.out.println("All Ready!");
				broadcastMessage(new ChatMessage(ChatMessage.MsgType.READY, "", "", String.valueOf(count), null));
				broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "��", "", "������ ���۵Ǿ����ϴ�.", null));
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
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "��", "", "������ ����Ǿ����ϴ�.", null));
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
					//System.out.println(user + "���� �ڸ����� : " + pos[c1]);
					broadcastMessage(new ChatMessage(ChatMessage.MsgType.POSITION, "", "", user+"/"+String.valueOf(c1), null));
					break;
				}
			}
		}
		if (!flag){
			//System.out.println("�����ʰ������� ��⿭�߰�");
			wait.add(user);
		}
	}

	void bcMSG(){
		// ���ο� �α��� ����Ʈ�� ��ü���� ���� ��
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.LOGIN_LIST, "", "", makeClientList(), null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.WAIT, "", "", "", wait));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.USER, "", "", "", pos));

		if (!gaming){
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "", "", "��� ������ �غ��ϸ� ������ ���۵˴ϴ�.", null));				
		}
		else{
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "", "", "������ ���� ���Դϴ�.", null));
		}
	}

	// ����� �̸��� Ŭ���̾�Ʈ���� ��� ��Ʈ���� �������� �ؽ� ���̺� �־���.
	// �̹� ������ �̸��� ����ڰ� �ִٸ�, ������ �α����� ���� �Ѱ����� Ŭ���̾�Ʈ���� �˸�
	// �׸��� ���ο� ������ ����Ʈ�� ��� �����ڿ��� ������
	// �ؽ� ���̺��� ���ٿ����� �������� ����� ��� (not Thread-Safe. Synchronized�� ��ȣ���� ��.
	private synchronized void handleLogin(String user, ObjectOutputStream writer) {

		//System.out.println("handleLogin() Login : " + user);
		try {
			// �̹� ������ �̸��� ����ڰ� �ִٸ�, ������ �α����� ���� �Ѱ����� Ŭ���̾�Ʈ���� �˸�
			if (clientOutputStreams.containsKey(user)) {
				writer.writeObject(
						new ChatMessage(ChatMessage.MsgType.LOGIN_FAILURE, "", "", "����� �̹� ����", null));
				return;
			}
		} catch (Exception ex) {
			System.out.println("S : �������� �۽� �� �̻� �߻�");
			ex.printStackTrace();
		}
		// �ؽ����̺� �����-���۽�Ʈ�� �� �߰��ϰ� ���ο� �α��� ����Ʈ�� ��ο��� �˸�
		clientOutputStreams.put(user, writer);

		positioning(user);
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "��", "", user + "���� �α����Ͽ����ϴ�.", null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.INIT_SCORE, "", "", String.valueOf(count), score));
		bcMSG();

		//listChecker();
	}  // close handleLogin

	// �־��� ����ڸ� �ؽ����̺��� ���� (��� ��Ʈ���� ����)
	// �׸��� ������Ʈ�� ������ ����Ʈ�� ��� �����ڿ��� ������
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
		// ���ο� �α��� ����Ʈ�� ��ü���� ���� ��
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.LOGIN_LIST, "", "", makeClientList(), null));
		broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, "��", "", user + "���� �α׾ƿ��Ͽ����ϴ�.", null));

	}  // close handleLogout

	// Ŭ���̾�Ʈ�� ��ȭ ���濡�� ������ �޽���. �� ��� Ȥ�� "��ü"���� ���� �־�� ��
	private synchronized void handleMessage(String sender, String receiver, String contents) {
		// ���⼭ ��ο��� ������ ��츦 ó���ؾ� ��
		if (receiver.equals(ChatMessage.ALL)) {			// "��ü"���� ������ �޽����̸�
			broadcastMessage(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, sender, "", contents, null));
			return;
		}
		// Ư�� ��뿡�� ������ �����
		ObjectOutputStream write = clientOutputStreams.get(receiver);
		try {
			write.writeObject(new ChatMessage(ChatMessage.MsgType.SERVER_MSG, sender, "", contents, true));
		} catch (Exception ex) {
			System.out.println("S : �������� �۽� �� �̻� �߻�");
			ex.printStackTrace();
		}
	}  // close handleIncomingMessage

	// �ؽ��ʿ� �ִ� ��� �����ڵ鿡�� �־��� �޽����� ������ �޼ҵ�.
	// �ݵ�� synchronized �� �޼ҵ忡���� ȣ���ϱ�� ��
	private void broadcastMessage(ChatMessage message) {
		Set<String> s = clientOutputStreams.keySet();	// ���� ��ϵ� ����ڵ��� �����ϰ� �ϳ��ϳ��� �޽��� ����
		// �׷��� ���ؼ� ���� ����� ����Ʈ�� ����
		Iterator<String> it = s.iterator();
		String user;
		while(it.hasNext()) {
			user = it.next();
			try {
				ObjectOutputStream writer = clientOutputStreams.get(user);	// ��� ����ڿ��� ��Ʈ�� ����
				writer.writeObject(message);									// �� ��Ʈ���� ���
				writer.flush();
				writer.reset();
			} catch(Exception ex) {
				System.out.println("S : �������� �۽� �� �̻� �߻�");
				ex.printStackTrace();
			}
		} // end while	   
	}	// end broadcastMessage

	private String makeClientList() {
		Set<String> s = clientOutputStreams.keySet();	// ���� ��ϵ� ����ڵ��� ����
		Iterator<String> it = s.iterator();
		String userList = "";
		while(it.hasNext()) {
			userList += it.next() + "/";					// ��Ʈ�� ����Ʈ�� �߰��ϰ� ������ ���
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
