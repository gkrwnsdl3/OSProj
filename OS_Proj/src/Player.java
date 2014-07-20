import javax.swing.*;


public class Player extends JPanel{
	JTextField answer;
	JTextField id;
	JTextField score;
	
	public Player(){
		setLayout(null);
		answer = new JTextField();
		answer.setBounds(26, 169, 116, 21);
		add(answer);
		answer.setColumns(10);
		answer.setHorizontalAlignment(SwingConstants.CENTER);
		answer.setOpaque(false);
		answer.setText("Answer Here!");
		
		id = new JTextField();
		id.setBounds(26, 10, 116, 21);
		add(id);
		id.setColumns(10);
		id.setHorizontalAlignment(SwingConstants.CENTER);
		id.setOpaque(false);
		id.setText("id");
		
		score = new JTextField();
		score.setColumns(10);
		score.setBounds(26, 41, 116, 21);
		add(score);
		score.setHorizontalAlignment(SwingConstants.CENTER);
		score.setOpaque(false);
		score.setText("score");
	}
}
