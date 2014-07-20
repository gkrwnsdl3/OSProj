import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class LoginAndRegist {
	JFrame f;
	JTextField tfd_id, tfd_pw, tfd_psv_ip, tfd_psv_port;
	Connection conn;
	Socket sock;				// 서버 연결용 소켓
	Timer timer;
	DefaultTableModel model;
	Vector<String> data;
	JTable table;
	String ip;
	int port;

	LoginAndRegist(){
		f = new JFrame();
		f.setLayout(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//f.setSize(225, 190);
		f.setSize(225, 400);
		f.setResizable(false);
		f.setTitle("Login / Regist");

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension di1 = tk.getScreenSize(); // 스크린사이즈의 사이즈
		Dimension di2 = f.getSize(); // 프레임의 사이즈
		f.setLocation(di1.width / 2 - di2.width / 2, di1.height / 2 - di2.height / 2);

		JLabel lbl_id = new JLabel("ID : ");
		lbl_id.setBounds(10, 10, 25, 20);
		f.add(lbl_id);
		tfd_id = new JTextField();
		tfd_id.setBounds(60, 10, 150, 20);
		tfd_id.requestFocus();
		f.add(tfd_id);

		JLabel lbl_pw = new JLabel("PW : ");
		lbl_pw.setBounds(10, 40, 30, 20);
		f.add(lbl_pw);
		tfd_pw = new JPasswordField();
		tfd_pw.addKeyListener(new PWListener());
		tfd_pw.setBounds(60, 40, 150, 20);
		f.add(tfd_pw);
		
		JLabel exp = new JLabel("<html>우선 아이디와 비밀번호를 입력한 뒤,<br>밑의 리스트에서<br>접속하려는 서버를 선택하세요<br>회원가입도 위 방법으로 진행합니다.</html>");
		exp.setBounds(10, 70, 200, 70);
		f.add(exp);
		
		JLabel lbl_psv_ip = new JLabel("수동접속ip :");
		lbl_psv_ip.setBounds(10, 260, 70, 20);
		f.add(lbl_psv_ip);
		
		tfd_psv_ip = new JTextField();
		tfd_psv_ip.setBounds(85, 260, 125, 20);
		f.add(tfd_psv_ip);
		
		JLabel lbl_psv_port = new JLabel("수동접속port :");
		lbl_psv_port.setBounds(10, 290, 80, 20);
		f.add(lbl_psv_port);
		
		tfd_psv_port = new JTextField();
		tfd_psv_port.setBounds(95, 290, 115, 20);
		f.add(tfd_psv_port);
		
		JButton button_psv = new JButton("수동접속");
		button_psv.addActionListener(new ButtonListener());
		button_psv.setBounds(10, 320, 200, 40);
		f.add(button_psv);

		Vector <String> colName = new Vector <String>();
		model = new DefaultTableModel(colName, 0){
			public boolean isCellEditable(int rowIndex, int mColIndex){
				return false;
			}};
			colName.add("No");
			colName.add("Name");
			colName.add("Port");
			table = new JTable(model);
			table.addMouseListener(new TableMouseListener());
			JScrollPane scroll = new JScrollPane(table);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getColumnModel().getColumn(0).setPreferredWidth(30);
			table.getColumnModel().getColumn(1).setPreferredWidth(130);
			table.getColumnModel().getColumn(2).setPreferredWidth(40);
			scroll.setBounds(10, 140, 200, 100);
			scroll.setPreferredSize(new Dimension(400,200));
			f.add(scroll);

			timer = new Timer(1000, new TimerListener());
			timer.start();
			f.setVisible(true);
			tfd_id.requestFocus();
			
			dbConnectionInit("localhost", "ozg", "root", "a");
			//dbConnectionInit("os2.sch.ac.kr", "d20105199", "20105199", "rlagkrwns");
	}
	
	public class ButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if (tfd_psv_ip.getText().isEmpty() || tfd_psv_port.getText().isEmpty()){
				JOptionPane.showMessageDialog(null, "수동접속 ip와 port를 입력하세요!");
				return;
			}
			ip=tfd_psv_ip.getText();
			port=Integer.valueOf(tfd_psv_port.getText());
			login(tfd_id.getText());
		}
		
	}

	public class TableMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub

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
			if (tfd_id.getText().isEmpty()){
				JOptionPane.showMessageDialog(null, "아이디를 입력하세요!");
				return;
			}
			else if (tfd_pw.getText().isEmpty()){
				JOptionPane.showMessageDialog(null, "비밀번호를 입력하세요!");
				return;
			}
			timer.stop();
			int choice = JOptionPane.showConfirmDialog(null, "해당 서버에 접속할까요?");
			if (choice == JOptionPane.YES_OPTION) {
				String no = (String)table.getValueAt(table.getSelectedRow(), 0);
				try{
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("select no, ip, socket from osproj_server");
					while (rs.next()){
						if (no.equals(rs.getString("no"))){
							ip=rs.getString("ip");
							port=rs.getInt("socket");
							break;
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				enter();
			}
			else
				timer.start();
		}
	}

	public class TimerListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try{
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("select no, name, socket from osproj_server"); 

				model.setRowCount(0);
				while (rs.next()){
					data = new Vector<String>();
					data.add(rs.getString("no"));
					data.add(rs.getString("name"));
					data.add(rs.getString("socket"));
					model.addRow(data);
				}
				stmt.close();
			}catch (Exception ex) {
			}
		}

	}

	public void dbConnectionInit(String address, String dbName, String id, String pw) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + address + "/" + dbName, id, pw); // DB 연결하기
		} catch (ClassNotFoundException | SQLException e) {
			//e.printStackTrace();
			int choice = JOptionPane.showConfirmDialog(null, "사용자DB에 접속할 수 없습니다.\n계속하시겠습니까?");
			if (choice != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null, "프로그램을 종료합니다.");
				System.exit(0);
			}
		} // JDBC드라이버를 JVM영역으로 가져오기
	}

	public void userCheck(long pw){
		try {
			String id=tfd_id.getText();
			boolean exist=false;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select id, pw from osProj_user");
			while(rs.next()){
				if (rs.getString("id").equals(id)){
					exist=true;
					if (rs.getInt("pw")==pw){
						//System.out.println("접속!");
						//if (rs.getInt("logon")==1){
						//	int choice = JOptionPane.showConfirmDialog(null, "접속중인 ID입니다.\nexit");
						//	System.exit(0);
						//}
						login(id);
						break;
					}
					else{
						JOptionPane.showMessageDialog(null, "비밀번호가 틀렸습니다.");
						tfd_pw.setText("");
						tfd_pw.requestFocus();
						break;
					}
				}
			}
			if (!exist){
				int choice = JOptionPane.showConfirmDialog(null, "존재하지않는 ID입니다.\n새로운 ID를 등록할까요?");
				if (choice == JOptionPane.YES_OPTION) {
					stmt.executeUpdate("INSERT INTO osProj_user (id, pw) VALUES ('" + id + "'," + pw + ");");
					stmt.close();
					//System.out.println("등록!");
					login(id);
				}
				else{
					tfd_id.setText("");
					tfd_pw.setText("");
					tfd_id.requestFocus();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void login(String id){
		timer.stop();
		setUpNetworking();
		GameClient gc = new GameClient(id, sock, conn);
		gc.setTitle(gc.frameTitle + " (Logon : " + gc.user + ") IP : " + ip + ", Port : " + port);
		f.setVisible(false);
	}

	private void setUpNetworking() {  
		try {
			sock = new Socket(ip, port);
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "서버접속에 실패하였습니다. 접속을 종료합니다.");
			//ex.printStackTrace();
			System.exit(0);		// 네트워크가 초기 연결 안되면 클라이언트 강제 종료
		}
	} // close setUpNetworking 

	long hf(){
		long res=0;
		for (int c1=0; c1<tfd_pw.getText().length(); c1++){
			res=31*res+(int)(tfd_pw.getText().charAt(c1));
		}
		//System.out.println(res%10001);
		return res%10001;
	}

	void enter(){
		long res;
		res=hf();
		userCheck(res);
	}

	public class PWListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getKeyCode()==10){
				enter();
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

}
