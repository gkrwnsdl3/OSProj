import javax.swing.UIManager;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception ex) {}
		//GameClient frame=new GameClient();
		new LoginAndRegist();
		//frame.setVisible(true);
	}
}
