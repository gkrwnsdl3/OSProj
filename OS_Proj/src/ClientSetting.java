import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

public class ClientSetting{
	JFrame f, main;
	JComboBox<String> cbx;
	JButton button_BGM, button_SFX;
	ArrayList<GameClient.ClipList> cliplist;
	boolean sfx=true;

	ClientSetting(){
		f = new JFrame();
		f.setLayout(null);
		f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		f.setSize(500,340);
		f.setResizable(false);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension di1 = tk.getScreenSize(); // 스크린사이즈의 사이즈
		Dimension di2 = f.getSize(); // 프레임의 사이즈
		f.setLocation(di1.width / 2 - di2.width / 2, di1.height / 2 - di2.height / 2);
		
		final ImageIcon image = new ImageIcon("ext/setting.jpg");
		JPanel panel = new JPanel(){
			public void paintComponent(Graphics g){
				g.drawImage(image.getImage(), 0, 0, null);
			}
		};
		panel.setLayout(null);
		panel.setBounds(0, 0, 500, 340);
		f.add(panel);

		cbx = new JComboBox<String>();
		cbx.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 50));
		cbx.addItem("Set LookAndFeel");
		cbx.addItem("Windows (Initial)");
		cbx.addItem("WindowsClassic");
		cbx.addItem("Motif");
		//cbx.addItem("Nimbus");
		cbx.addPopupMenuListener(new CBActionListener());
		cbx.setBounds(10, 10, 465, 100);
		panel.add(cbx);
		
		button_BGM = new JButton("배경음 : On");
		button_BGM.addActionListener(new BGMListener());
		button_BGM.setBounds(10, 120, 465, 50);
		button_BGM.setBorderPainted(false);
		button_BGM.setContentAreaFilled(false);
		button_BGM.setForeground(Color.cyan);
		button_BGM.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
		panel.add(button_BGM);

		button_SFX = new JButton("효과음 : On");
		button_SFX.addActionListener(new SFXListener());
		button_SFX.setBounds(10, 180, 465, 50);
		button_SFX.setBorderPainted(false);
		button_SFX.setContentAreaFilled(false);
		button_SFX.setForeground(Color.orange);
		button_SFX.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
		panel.add(button_SFX);

		JButton button_Back = new JButton("대기실로");
		button_Back.addActionListener(new BackListener());
		button_Back.setBounds(10, 240, 465, 50);
		button_Back.setBorderPainted(false);
		button_Back.setContentAreaFilled(false);
		button_Back.setForeground(Color.red);
		button_Back.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
		panel.add(button_Back);

		f.setTitle("Setting");
		f.setVisible(false);
	}
	
	void load(JFrame m, ArrayList<GameClient.ClipList> c){
		cliplist=c;
		main=m;
	}

	class BGMListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if (button_BGM.getText().equals("배경음 : On")){
				for (int c1=0; c1<cliplist.size(); c1++){
					if (cliplist.get(c1).file_url.equals("ext/ChmpSlct_DraftMode(8bitS).wav")){
						cliplist.get(c1).clip.stop();
						cliplist.get(c1).clip.close();
						cliplist.remove(c1);
						break;
					}
				}
				button_BGM.setText("배경음 : off");
			}
			else{
				GameClient.playSound("ext/ChmpSlct_DraftMode(8bitS).wav");
				button_BGM.setText("배경음 : On");
			}
		}

	}
	
	class SFXListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (button_SFX.getText().equals("효과음 : On")){
				sfx=false;
				button_SFX.setText("효과음 : off");
			}
			else{
				sfx=true;
				button_SFX.setText("효과음 : On");
			}
		}
		
	}

	class BackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			f.setVisible(false);
		}

	}

	class CBActionListener implements PopupMenuListener{

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			// TODO Auto-generated method stub
			if (cbx.getSelectedIndex()==0)
				return;
			try{
				if (cbx.getSelectedItem().equals("Windows (Initial)")){
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				}
				else if (cbx.getSelectedItem().equals("WindowsClassic")){
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
				}
				else if (cbx.getSelectedItem().equals("Motif")){
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				}
				//else if (cbx.getSelectedItem().equals("Nimbus")){
				//	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				//}
				SwingUtilities.updateComponentTreeUI(f);
				SwingUtilities.updateComponentTreeUI(main);
			} catch (Exception ex){}
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			// TODO Auto-generated method stub

		}
	}
}