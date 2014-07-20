import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.sound.sampled.*;

public class GameClient extends JFrame{
	final int PARTY_MAX=6;
	boolean positioning=false, start=false;
	ArrayList<Player> player= new ArrayList<Player>();
	ArrayList<String> answer= new ArrayList<String>();
	static ArrayList<ClipList> cliplist = new ArrayList<ClipList>(); 
	JLabel label_Score, label_Wait, label_Timer;
	JButton button_Quest_Ready, settingButton, logButton;
	int position=99, times=3, wait, timer_s, timer_ms, cycle=0, wrongcycle;
	Timer timer, timeleft, wrong;
	String frameTitle="GameClient";
	JTextArea incoming;			// 수신된 메시지를 출력하는 곳
	JTextArea outgoing;			// 송신할 메시지를 작성하는 곳
	JList counterParts;			// 현재 로그인한 채팅 상대목록을 나타내는 리스트.
	JPanel panel;
	String user;				// 이 클라이언트로 로그인 한 유저의 이름
	ObjectInputStream reader;	// 수신용 스트림
	ObjectOutputStream writer;	// 송신용 스트림
	Socket sock;				// 서버 연결용 소켓
	String question="";
	long time_Start, time_End, re_Ready;
	JFrame frame;
	static ClientSetting cs;
	ImageIcon bgImg;
	Connection conn;

	public GameClient(String id, Socket s, Connection c){
		sock=s;
		user=id;
		conn=c;
		frame=this;
		//setTitle(frameTitle);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		setSize(950, 600);
		setResizable(false);
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension di1 = tk.getScreenSize(); // 스크린사이즈의 사이즈
		Dimension di2 = getSize(); // 프레임의 사이즈
		setLocation(di1.width / 2 - di2.width / 2, di1.height / 2 - di2.height / 2);

		bgImg=new ImageIcon("ext/bg.jpg");

		panel=new JPanel(){
			public void paintComponent(Graphics g){
				//Graphics2D g2d = (Graphics2D)g;
				//g.drawImage(new ImageIcon("ext/bg2.jpg").getImage(), 0, 0, null);
				//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)0.5));
				//g2d.drawImage(bgImg.getImage(), 0, 0, null);
				g.drawImage(bgImg.getImage(), 0, 0, null);
			}
		};
		panel.setLayout(null);
		add(panel);

		settingButton = new JButton("Setting");
		settingButton.addActionListener(new SettingButtonListener());
		settingButton.setBounds(772, 480, 150, 71);
		settingButton.setContentAreaFilled(false);
		settingButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
		settingButton.setForeground(new Color(64, 226, 247));
		settingButton.setBorderPainted(false);
		panel.add(settingButton);

		logButton = new JButton("Login");
		logButton.addActionListener(new LogButtonListener());
		logButton.setBounds(612, 480, 148, 71);
		logButton.setContentAreaFilled(false);
		logButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
		logButton.setForeground(new Color(15, 247, 197));
		logButton.setBorderPainted(false);
		panel.add(logButton);

		// 메시지 디스플레이 창
		incoming = new JTextArea(15,20);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		incoming.setOpaque(false);
		incoming.setForeground(Color.white);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setBounds(612, 10, 310, 340);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		qScroller.setOpaque(false);
		qScroller.getViewport().setOpaque(false);
		outgoing = new JTextArea(5,20);
		outgoing.setLineWrap(true);
		outgoing.addKeyListener(new outgoingKeyListener());
		outgoing.setWrapStyleWord(true);
		outgoing.setEditable(true);
		outgoing.setOpaque(false);
		outgoing.setCaretColor(Color.magenta);
		outgoing.setForeground(Color.white);
		JScrollPane oScroller = new JScrollPane(outgoing);
		oScroller.setBounds(772, 360, 150, 110);
		oScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		oScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		oScroller.setOpaque(false);
		oScroller.getViewport().setOpaque(false);

		// 대화 상대 목록. 초기에는 "전체" - ChatMessage.ALL 만 있음
		String[] list = {ChatMessage.ALL};//{};
		counterParts = new JList(list);
		JScrollPane cScroller = new JScrollPane(counterParts);
		cScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		cScroller.setOpaque(false);
		cScroller.getViewport().setOpaque(false);
		counterParts.setOpaque(false);
		counterParts.setCellRenderer(new OpaqueCellRenderer());
		counterParts.setVisibleRowCount(5);
		counterParts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		counterParts.setFixedCellWidth(100);
		cScroller.setBounds(612,360,148,110);
		panel.add(qScroller);
		panel.add(oScroller);
		panel.add(cScroller);

		for (int c1=0; c1<PARTY_MAX; c1++){
			player.add(new Player());
			player.get(c1).setOpaque(false);
			player.get(c1).id.addMouseListener(new InfoListener(c1));
			panel.add(player.get(c1));
		}
		alldisable();
		player.get(0).setBounds(12, 93, 170, 200);
		player.get(1).setBounds(221, 93, 170, 200);
		player.get(2).setBounds(430, 93, 170, 200);
		player.get(3).setBounds(12, 351, 170, 200);
		player.get(4).setBounds(221, 351, 170, 200);
		player.get(5).setBounds(430, 351, 170, 200);

		JLabel label_1 = new JLabel("Score");
		label_1.setFont(new Font("굴림", Font.PLAIN, 30));
		label_1.setBounds(12, 15, 81, 68);
		panel.add(label_1);

		label_Score=new JLabel("0");
		label_Score.setFont(new Font("굴림", Font.PLAIN, 30));
		label_Score.setForeground(Color.red);
		//label_Score.setBounds(105, 15, 286, 68);
		label_Score.setBounds(105, 15, 200, 68);
		panel.add(label_Score);

		JLabel label_2 = new JLabel("WaitCount");
		label_2.setBounds(486, 15, 57, 15);
		panel.add(label_2);

		label_Wait = new JLabel("0");
		label_Wait.setHorizontalAlignment(SwingConstants.CENTER);
		label_Wait.setFont(new Font("굴림", Font.PLAIN, 30));
		label_Wait.setBounds(430, 48, 170, 35);
		panel.add(label_Wait);

		label_Timer = new JLabel(timer_s+":"+timer_ms);
		label_Timer.setHorizontalAlignment(SwingConstants.CENTER);
		label_Timer.setFont(new Font("굴림", Font.PLAIN, 30));
		label_Timer.setBounds(220, 32, 170, 35);
		panel.add(label_Timer);

		button_Quest_Ready = new JButton("로그인하세요");
		button_Quest_Ready.addActionListener(new ReadyButtonListener());
		button_Quest_Ready.setHorizontalAlignment(SwingConstants.CENTER);
		button_Quest_Ready.setFont(new Font("굴림", Font.PLAIN, 30));
		button_Quest_Ready.setBounds(36, 300, 538, 41);
		button_Quest_Ready.setEnabled(false);
		button_Quest_Ready.setContentAreaFilled(false);
		button_Quest_Ready.setBorderPainted(false);
		panel.add(button_Quest_Ready);

		cs = new ClientSetting();
		playSound("ext/ChmpSlct_DraftMode(8bitS).wav");

		// 네트워킹을 시동하고, 서버에서 메시지를 읽을 스레드 구동
		setUpNetworking();
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		loginButton();
	}

	public class InfoListener implements MouseListener{
		int index;
		InfoListener(int i){
			index=i;
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			new PlayerInfo(conn, player.get(index).id.getText());
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	public class OpaqueCellRenderer extends DefaultListCellRenderer{
		OpaqueCellRenderer(){
			setOpaque(false);
		}
	}

	private void setUpNetworking() {  
		try {
			//sock = new Socket("220.69.203.11", 5000);
			//sock = new Socket("127.0.0.1", 5000);			// 소켓 통신을 위한 포트는 5000번 사용키로 함
			//sock = new Socket("192.168.1.119", 5000);
			reader = new ObjectInputStream(sock.getInputStream());
			writer = new ObjectOutputStream(sock.getOutputStream());
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "서버접속에 실패하였습니다. 접속을 종료합니다.");
			ex.printStackTrace();
			dispose();		// 네트워크가 초기 연결 안되면 클라이언트 강제 종료
		}
	} // close setUpNetworking 
	// 서버에서 보내는 메시지를 받는 스레드 작업을 정의하는 클래스
	public class IncomingReader implements Runnable {
		public void run() {
			ChatMessage message;
			ChatMessage.MsgType type;
			try {
				while (true) {
					message = (ChatMessage) reader.readObject();     	 // 서버로기 부터의 메시지 대기                  
					type = message.getType();
					//System.out.println("User : " + user + ", " + message);
					if (type==ChatMessage.MsgType.USER){
						type_User(message);
					} else if (type == ChatMessage.MsgType.LOGIN_FAILURE) {	 // 로그인이 실패한 경우라면
						type_LogFail();
					} else if (type == ChatMessage.MsgType.READY && !start){
						type_Ready(message);
					} else if (type == ChatMessage.MsgType.QUESTION){
						type_Question(message);
					} else if (type == ChatMessage.MsgType.SERVER_MSG) { // 메시지를 받았다면 보여줌
						if (message.getSender().equals(user)) continue;  // 내가 보낸 편지면 보일 필요 없음
						type_SerMsg(message);
					} else if (type == ChatMessage.MsgType.LOGIN_LIST) {
						type_LoginList(message);
					} else if (type==ChatMessage.MsgType.POSITION && !positioning){
						type_Position(message);
					} else if (type==ChatMessage.MsgType.WAIT && !positioning){
						type_Wait(message);
					} else if (type == ChatMessage.MsgType.ANSWER){ 
						type_Answer(message);
					} else if (type == ChatMessage.MsgType.END){
						type_End(message);
					} else if (type == ChatMessage.MsgType.INIT_SCORE){
						type_Init_Score(message);
					} else if (type == ChatMessage.MsgType.NO_ACT || positioning){
						// 아무 액션이 필요없는 메시지. 그냥 스킵
					} else {
						// 정체가 확인되지 않는 이상한 메시지
						throw new Exception("서버에서 알 수 없는 메시지 도착했음");
					}
				} // close while
			} catch(Exception ex) {
				System.out.println("클라이언트 스레드 종료");		// 프레임이 종료될 경우 이를 통해 스레드 종료
				//ex.printStackTrace();
			}
		} // close run

		void type_User(ChatMessage message){
			String pos[] = new String[PARTY_MAX];
			pos=(String[]) message.getObj();
			for (int c1=0; c1<PARTY_MAX; c1++){
				if (pos[c1].isEmpty()){
					player.get(c1).id.setText("");
					player.get(c1).answer.setText("");
					player.get(c1).score.setText("");
				}
				else{
					player.get(c1).id.setText(pos[c1]);
				}
			}
		}

		void type_LogFail(){
			frame.dispose();
			JOptionPane.showMessageDialog(null, "Login이 실패하였습니다. 다시 로그인하세요.\n중복접속했거나 네트워크 연결이 불안정합니다.");
			System.exit(0);
			//setTitle(frameTitle + " : 로그인 하세요");
			//logButton.setText("Login");
		}

		void type_Ready(ChatMessage message){
			if (message.getContents().equals("혼자레디") && button_Quest_Ready.getText().equals("Ready!")){
				button_Quest_Ready.setText("Ready?");
				player.get(position).score.setText("");
				button_Quest_Ready.setForeground(Color.white);
				JOptionPane.showMessageDialog(null, "혼자 준비할 수 없습니다.");
			}
			else if (!message.getContents().isEmpty()){
				wait=Integer.valueOf(message.getContents());
				for (int c1=0; c1<wait; c1++){
					player.get(c1).score.setText("0");
				}
				button_Quest_Ready.setEnabled(false);
				logButton.setEnabled(false);
				player.get(position).answer.requestFocus();
				start=true;
				logButton.setForeground(Color.black);
				button_Quest_Ready.setText("게임시작!");
			}
			else{
				ArrayList <String> list = new ArrayList<String>();
				list=(ArrayList<String>) message.getObj();
				for (int c1=0; c1<PARTY_MAX; c1++){
					player.get(c1).score.setText("");
					for (int c2=0; c2<list.size(); c2++){
						if (list.get(c2).equals(player.get(c1).id.getText())){
							player.get(c1).score.setText("Ready!");
							break;
						}
					}
				}
			}
		}

		void type_Question(ChatMessage message){
			question="";
			answer.add((String)message.getContents());
			ArrayList <String> list = new ArrayList<String>();
			list=(ArrayList<String>) message.getObj();
			for (int c1=0; c1<list.size(); c1++){
				question+=list.get(c1);
			}
			if (position!=99){
				player.get(position).answer.setEnabled(true);
				player.get(position).answer.requestFocus();
				for (int c1=0; c1<wait; c1++){
					player.get(c1).answer.setText("");
				}
			}
			else{
				for (int c1=0; c1<PARTY_MAX; c1++){
					if (!player.get(c1).id.getText().isEmpty()){
						player.get(c1).answer.setText("");
					}
				}
			}
			label_Timer.setForeground(Color.black);

			times=3;
			timer = new Timer(1000, new Times());
			timer.start();
			cycle++;
		}

		class Times implements ActionListener{
			public void actionPerformed (ActionEvent event){
				playSound("ext/countdown_1.wav");
				if (times>0){
					button_Quest_Ready.setText("여기서 문제가 출제됩니다 " + times + " sec");
					times-=1;
				}
				else{
					button_Quest_Ready.setText(question);
					time_Start=System.currentTimeMillis();
					timer.stop();
					timer_s=15;
					timeleft = new Timer(100, new TimeLeft());
					timeleft.start();
				}
			}
		}

		class TimeLeft implements ActionListener{
			boolean red=false;
			public void actionPerformed (ActionEvent event){
				if (timer_ms==0){
					timer_s--;
					timer_ms=10;
				}
				timer_ms--;
				if (timer_s<3){
					if (!red){
						label_Timer.setForeground(Color.red);
						red=true;
					}
					else{
						label_Timer.setForeground(Color.black);
						red=false;
					}
				}
				label_Timer.setText(timer_s+":"+timer_ms);
				if (timer_s==0 && timer_ms==0){
					button_Quest_Ready.setText(answer.get(cycle-1));
					timeleft.stop();
				}
			}
		}

		void type_SerMsg(ChatMessage message){
			if (message.getObj()!=null){
				if ((boolean) message.getObj()==true){
					playSound("ext/GUI_Equip_Item_01.wav");
					incoming.append("귓-" + message.getSender() + " : " + message.getContents() + "\n");
				}
			}
			else{
				playSound("ext/pm_receive.wav");
				incoming.append(message.getSender() + " : " + message.getContents() + "\n");
			}
			incoming.setCaretPosition(incoming.getDocument().getLength());
		}

		void type_LoginList(ChatMessage message){
			// 유저 리스트를 추출 해서 counterParts 리스트에 넣어 줌.
			// 나는  빼고 (""로 만들어 정렬 후 리스트 맨 앞에 오게 함)
			String[] users = message.getContents().split("/");
			for (int i=0; i<users.length; i++) {
				if (user.equals(users[i]))users[i] = "";
			}
			users = sortUsers(users);		// 유저 목록을 쉽게 볼 수 있도록 정렬해서 제공
			users[0] =  ChatMessage.ALL;	// 리스트 맨 앞에 "전체"가 들어가도록 함
			counterParts.setListData(users);
			repaint();
		}

		void type_Position(ChatMessage message){
			alldisable();
			String str=message.getContents();
			String id="";
			for (int c1=0; c1<str.length(); c1++){
				String temp="";
				temp=str.substring(c1, c1+1);
				if (temp.equals("/")){
					break;
				}
				id+=temp;
			}
			if (id.equals(user)){
				str=str.substring(user.length()+1, str.length());
				label_Wait.setText("0");
				positioning=true;
				position=Integer.valueOf(str);
				player.get(position).answer.setEnabled(true);
				player.get(position).answer.setForeground(Color.yellow);
				player.get(position).answer.addKeyListener(new AnswerKeyListener());
				player.get(position).id.setEnabled(true);
				player.get(position).id.setEditable(false);
				player.get(position).id.setForeground(Color.yellow);
			}
		}

		void type_Wait(ChatMessage message){
			alldisable();
			button_Quest_Ready.setEnabled(false);
			ArrayList <String> list = (ArrayList<String>) message.getObj();
			for (int c1=0; c1<list.size(); c1++){
				if (list.get(c1).equals(user)){
					label_Wait.setText(String.valueOf(c1+1));
				}
			}
		}

		void type_Answer(ChatMessage message){
			if (message.getContents().equals("T")){
				for (int c1=0; c1<PARTY_MAX; c1++){
					if (player.get(c1).id.getText().equals(message.getReceiver())){
						player.get(c1).answer.setText("정답! +" + message.getObj());
						if (player.get(c1).score.getText().isEmpty())
							player.get(c1).score.setText("0");
						player.get(c1).score.setText(String.valueOf(Long.valueOf(player.get(c1).score.getText())+(long)message.getObj()));
						player.get(c1).answer.setEnabled(false);
						playSound("ext/traderequested.wav");
						break;
					}
				}
			}
			else if (message.getContents().equals("AT")){
				timer_s=0;
				timer_ms=0;
				if (answer.size()!=0){
					button_Quest_Ready.setText(answer.get(cycle-1));
				}
				if (timeleft!=null){
					timeleft.stop();
				}
			}
			else{
				for (int c1=0; c1<PARTY_MAX; c1++){
					if (player.get(c1).id.getText().equals(message.getReceiver())){
						if (player.get(c1).score.getText().isEmpty())
							player.get(c1).score.setText("0");
						if ((long)message.getObj()>0){
							player.get(c1).score.setText(String.valueOf((Long.valueOf(player.get(c1).score.getText()))-(long)message.getObj()/5));
							if (c1==position){
								wrongcycle=6;
								wrong = new Timer(200, new WrongActionListener());
								wrong.start();
								playSound("ext/hit_shield_02.wav");
							}
						}
						break;
					}
				}
			}
			if (position!=99){
				int score=Integer.valueOf(player.get(position).score.getText());
				if (score>0){
					label_Score.setForeground(Color.green);
				}
				else{
					label_Score.setForeground(Color.red);
				}
				label_Score.setText(player.get(position).score.getText());
			}
		}

		public class WrongActionListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				wrongcycle--;
				if (wrongcycle%2==1){
					label_Score.setForeground(Color.red);
				}
				else{
					label_Score.setForeground(Color.green);
				}

				if (wrongcycle==0){
					if (Integer.valueOf(player.get(position).score.getText())>0){
						label_Score.setForeground(Color.green);
					}
					else{
						label_Score.setForeground(Color.red);
					}
					wrong.stop();
				}
			}

		}

		void type_End(ChatMessage message){
			ArrayList <Integer> s = new ArrayList<Integer>();
			for (int c1=0; c1<wait; c1++){
				int max=-1;
				for (int c2=0; c2<wait; c2++){
					if (max==-1){
						if (!s.contains(c2) && Integer.MIN_VALUE<Integer.valueOf(player.get(c2).score.getText())){
							max=c2;
						}
					}
					else{
						if (!s.contains(c2) && Integer.valueOf(player.get(max).score.getText())<Integer.valueOf(player.get(c2).score.getText())){
							max=c2;
						}
					}
				}
				s.add(max);
			}

			String res="", newrecord="";
			for (int c1=0; c1<s.size(); c1++){
				res+=(c1+1)+"등 : "+player.get(s.get(c1)).id.getText()+"\n";
			}

			try {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("select id, maxscore from osProj_user");
				if (position!=99){
					while (rs.next()){
						if (player.get(position).id.getText().equals(rs.getString("id"))){
							if (Integer.valueOf(player.get(position).score.getText())>rs.getInt("maxscore")){
								newrecord="최고기록!\n";
								stmt.executeUpdate("update osProj_user set maxscore=" + Integer.valueOf(player.get(position).score.getText()) + " where id='" + rs.getString("id") + "'");
							}
							break;
						}
					}
				}

				if (position==99);
				else if (position==(s.get(0))){
					playSound("ext/uiMessage0.wav");
					rs = stmt.executeQuery("select id, win from osProj_user");
					while(rs.next()){
						if (player.get(position).id.getText().equals(rs.getString("id"))){
							int score=rs.getInt("win")+1;
							stmt.executeUpdate("update osProj_user set win=" + score + " where id='" + rs.getString("id") + "'");
							break;
						}
					}
					JOptionPane.showMessageDialog(null, "WIN!\n" + newrecord + res);
				}
				else{
					playSound("ext/Impact_1.wav");
					rs = stmt.executeQuery("select id, lose from osProj_user");
					while(rs.next()){
						if (player.get(position).id.getText().equals(rs.getString("id"))){
							int score=rs.getInt("lose")+1;
							stmt.executeUpdate("update osProj_user set lose=" + score + " where id='" + rs.getString("id") + "'");
							break;
						}
					}
					JOptionPane.showMessageDialog(null, "lose...\n" + newrecord + res);
				}
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			timer_s=0;
			timer_ms=0;
			repaint();
			button_Quest_Ready.setEnabled(true);
			logButton.setText("Logout");
			logButton.setEnabled(true);
			button_Quest_Ready.setText("Ready?");
			button_Quest_Ready.setForeground(Color.white);
			label_Score.setText("");
			label_Score.setForeground(Color.red);
			logButton.setForeground(new Color(15, 247, 197));
			label_Timer.setText(timer_s+":"+timer_ms);
			label_Timer.setForeground(Color.black);

			if (position!=99)
				player.get(position).answer.setEnabled(true);

			for (int c1=0; c1<PARTY_MAX; c1++){
				player.get(c1).answer.setText("");
				player.get(c1).score.setText("");
			}

			start=false;
		}

		void type_Init_Score(ChatMessage message){
			wait=Integer.valueOf(message.getContents());
			long score[]=new long[PARTY_MAX];
			score=(long[])message.getObj();
			for (int c1=0; c1<PARTY_MAX; c1++){
				if (score[c1]!=0){
					player.get(c1).score.setText(String.valueOf(score[c1]));
				}
				else{
					player.get(c1).score.setText("0");
				}
			}
		}

		// 주어진 String 배열을 정렬한 새로운 배열 리턴
		private String [] sortUsers(String [] users) {
			String [] outList = new String[users.length];
			ArrayList<String> list = new ArrayList<String>();
			for (String s : users) {
				list.add(s);
			}
			Collections.sort(list);				// Collections.sort를 사용해 한방에 정렬
			for (int i=0; i<users.length; i++) {
				outList[i] = list.get(i);
			}
			return outList;
		}
	}

	public static class ClipList{
		Clip clip;
		AudioInputStream ais;
		String file_url;

		ClipList(String str){
			go(str);
		}

		void go(String str){
			try {
				file_url=str;
				clip=AudioSystem.getClip();
				ais=AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(str)));
			} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized static void playSound(String file_url){
		if (!cs.sfx && !file_url.equals("ext/ChmpSlct_DraftMode(8bitS).wav"))
			return;
		try {
			for (int c1=0; c1<cliplist.size(); c1++){
				if (cliplist.get(c1).file_url.equals(file_url)){
					if (!cliplist.get(c1).clip.isRunning()){
						cliplist.get(c1).clip.stop();
						cliplist.get(c1).clip.close();
						cliplist.remove(c1);
					}
				}
			}
			cliplist.add(new ClipList(file_url));
			cliplist.get(cliplist.size()-1).clip.open(cliplist.get(cliplist.size()-1).ais);
			cliplist.get(cliplist.size()-1).clip.start();
			if (file_url.equals("ext/ChmpSlct_DraftMode(8bitS).wav")){
				cliplist.get(cliplist.size()-1).clip.loop(-1);
			}

		} catch(Exception e){
			System.out.println("sound exception");
			e.printStackTrace();
		}
	}

	public class AnswerKeyListener implements KeyListener{
		public void keyPressed(KeyEvent e) {
			if (start && player.get(position).answer.getText().isEmpty());
			else if (start && e.getKeyCode()==10){
				time_End=System.currentTimeMillis();
				try {
					writer.writeObject(new ChatMessage(ChatMessage.MsgType.ANSWER, user, "", player.get(position).answer.getText(), time_End-time_Start));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if (e.getKeyCode()==17){
				outgoing.requestFocus();
			}
		}
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode()==10){
				player.get(position).answer.setText("");
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}
	}

	void alldisable(){
		for (int c1=0; c1<PARTY_MAX; c1++){
			player.get(c1).answer.setEnabled(false);
			player.get(c1).id.setEnabled(false);
			player.get(c1).score.setEnabled(false);
		}
	}

	public class outgoingKeyListener implements KeyListener{
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==17){
				player.get(position).answer.requestFocus();
			}
			if (e.getKeyCode()==10){
				send();
			}
		}
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode()==10){
				outgoing.setText("");
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}
	}

	void send(){
		String to = (String) counterParts.getSelectedValue();
		if (to == null) {
			JOptionPane.showMessageDialog(null, "송신할 대상을 선택하지않으면 전체 전송합니다");
			counterParts.setSelectedIndex(0);
			to="전체";
		}
		try {
			if (to=="전체"){
				incoming.append(user + " : " + outgoing.getText() + "\n"); // 나의 메시지 창에 보이기
				writer.writeObject(new ChatMessage(ChatMessage.MsgType.CLIENT_MSG, user, to, outgoing.getText(), null));
			}
			else{
				incoming.append("귓-" + to + " : " + outgoing.getText() + "\n"); // 나의 메시지 창에 보이기
				writer.writeObject(new ChatMessage(ChatMessage.MsgType.CLIENT_MSG, user, to, outgoing.getText(), true));
			}
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus();
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
			ex.printStackTrace();
		}
	}

	private class ReadyButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent ev) {
			if (System.currentTimeMillis()-re_Ready<3000){
				JOptionPane.showMessageDialog(null, "다시 준비하는데는 3초 기다려야합니다.");
				return;
			}
			playSound("ext/hit.wav");
			if (button_Quest_Ready.getText().equals("Ready?")){
				try {
					writer.writeObject(new ChatMessage(ChatMessage.MsgType.READY, user, "", "T", null));
					writer.flush();
					button_Quest_Ready.setText("Ready!");
					button_Quest_Ready.setForeground(Color.red);
				} catch(Exception ex) {
					System.out.println("레디오류!");
					ex.printStackTrace();
				}
			}
			else{
				try {
					re_Ready=System.currentTimeMillis();
					writer.writeObject(new ChatMessage(ChatMessage.MsgType.READY, user, "", "F", null));
					writer.flush();
					button_Quest_Ready.setText("Ready?");
					button_Quest_Ready.setForeground(Color.white);
				} catch(Exception ex) {
					System.out.println("레디오류!");
					ex.printStackTrace();
				}
			}
		}
	}

	private class SettingButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent ev) {
			cs.load(frame, cliplist);
			cs.f.setVisible(true);
		}
	}

	void loginButton(){
		if (logButton.getText().equals("Login")) {
			processLogin();
		}
		else{
			processLogout();
		}
	}

	// 로그인 처리
	private void processLogin() {
		try {
			playSound("ext/yourturn.wav");
			logButton.setText("Logout");
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			button_Quest_Ready.setEnabled(true);
			button_Quest_Ready.setForeground(Color.white);
			button_Quest_Ready.setText("Ready?");
			writer.writeObject(new ChatMessage(ChatMessage.MsgType.LOGIN, user, "", "", null));
			writer.flush();
			//setTitle(frameTitle + " (Logon : " + user + ")");
			System.out.println(user);
			for (int c1=0; c1<PARTY_MAX; c1++){
				player.get(c1).answer.setText("");
				player.get(c1).id.setText("");
				player.get(c1).score.setText("");
			}
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "로그인 중 서버접속에 문제가 발생하였습니다.");
			ex.printStackTrace();
		}
	}
	// 로그아웃 처리
	private void processLogout() {
		int choice = JOptionPane.showConfirmDialog(null, "Logout합니다");
		if (choice == JOptionPane.YES_OPTION) {
			try {
				playSound("ext/Windows Shutdown.wav");
				Thread.sleep(960);
				writer.writeObject(new ChatMessage(ChatMessage.MsgType.LOGOUT, user, "", String.valueOf(position), null));
				writer.flush();
				// 연결된 모든 스트림과 소켓을 닫고 프로그램을 종료 함
				writer.close(); reader.close(); sock.close();
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "로그아웃 중 서버접속에 문제가 발생하였습니다. 강제종료합니다");
				ex.printStackTrace();
			} finally {
				System.exit(100);			// 클라이언트 완전 종료 
			}
		}
	}

	public class LogButtonListener implements ActionListener{ 
		public void actionPerformed(ActionEvent arg0) {
			loginButton();
		}
	}
}
