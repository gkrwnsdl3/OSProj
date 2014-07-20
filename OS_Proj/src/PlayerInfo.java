import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;

import javax.swing.*;

public class PlayerInfo {
	Connection conn;
	String user;
	JFrame f;
	JLabel lbl_win, lbl_lose, lbl_wl, lbl_ms;
	PlayerInfo(Connection c, String u){
		conn=c;
		user=u;
		f = new JFrame();
		f.setTitle("PlayerInfo");
		f.setResizable(false);
		f.setLayout(null);
		f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(200, 300);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension di1 = tk.getScreenSize(); // 스크린사이즈의 사이즈
		Dimension di2 = f.getSize(); // 프레임의 사이즈
		f.setLocation(di1.width / 2 - di2.width / 2, di1.height / 2 - di2.height / 2);

		JLabel lbl_id = new JLabel("ID : " + user);
		lbl_id.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
		lbl_id.setBounds(10, 10, 180, 30);
		f.add(lbl_id);

		lbl_win = new JLabel();
		lbl_win.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		lbl_win.setBounds(10, 50, 180, 30);
		f.add(lbl_win);

		lbl_lose = new JLabel();
		lbl_lose.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		lbl_lose.setBounds(10, 90, 180, 30);
		f.add(lbl_lose);

		lbl_wl = new JLabel();
		lbl_wl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		lbl_wl.setBounds(10, 130, 180, 30);
		f.add(lbl_wl);

		lbl_ms = new JLabel();
		lbl_ms.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		lbl_ms.setBounds(10, 170, 180, 30);
		f.add(lbl_ms);

		JButton button_Back = new JButton("대기실로");
		button_Back.addActionListener(new BackListener());
		button_Back.setBounds(10, 210, 165, 50);
		button_Back.setBorderPainted(false);
		button_Back.setContentAreaFilled(false);
		//button_Back.setForeground(Color.red);
		button_Back.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
		f.add(button_Back);

		load();

		f.setVisible(true);
	}

	void load(){
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select id, maxscore, win, lose from osProj_user");
			while (rs.next()){
				if (rs.getString("id").equals(user)){
					DecimalFormat fmt = new DecimalFormat("##.##");
					lbl_win.setText("W : " + rs.getString("win"));
					lbl_lose.setText("L : " + rs.getString("lose"));
					lbl_ms.setText("Hi.score : " + rs.getString("maxscore"));
					double rate = rs.getInt("win");
					rate=rate+rs.getInt("lose");
					rate=rs.getInt("win")/rate*100;
					if (rate>50){
						lbl_wl.setForeground(Color.green);
					}
					else{
						lbl_wl.setForeground(Color.red);
					}
					lbl_wl.setText("W/L : " + String.valueOf(fmt.format(rate)) + " %");
					break;
				}
			}
			stmt.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			f.dispose();
		}

	}
}
