import java.util.ArrayList;


public class QuestionMaker {
	ArrayList <String> list = new ArrayList <String>();
	ArrayList <String> prefix_List = new ArrayList <String>();
	String answer;
	
	public QuestionMaker(int cycle){
		maker(cycle);
	}

	public void maker(int cycle){
		makeList(cycle);
		//printer(list);
		infix_Prefix();
		//printer(prefix_List);
		answer=prefix_Calculate();
		//System.out.println(question_I);
	}
	
	public String prefix_Calculate(){
		ArrayList <String> s = new ArrayList <String>();
		for (int c1=0; c1<prefix_List.size(); c1++){
			String str=prefix_List.get(c1);
			if (priority(str)!=1 && priority(str)!=2){
				s.add(str);
			}
			else{
				int op2=Integer.valueOf(s.remove(s.size()-1)), op1=Integer.valueOf(s.remove(s.size()-1));
				switch (str){
				case "+":
					s.add(String.valueOf(op1+op2));
					break;
				case "-":
					s.add(String.valueOf(op1-op2));
					break;
				case "*":
					s.add(String.valueOf(op1*op2));
					break;
				case "/":
					s.add(String.valueOf(op1/op2));
					break;
				}
			}
		}
		return s.remove(s.size()-1);
	}

	public void infix_Prefix(){
		ArrayList <String> s = new ArrayList <String>();
		for (int c1=0; c1<list.size(); c1++){
			String str=list.get(c1);
			if (str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/")){
				while (!s.isEmpty()){
					if (priority(str)<=priority(s.get(s.size()-1))){
						prefix_List.add(s.remove(s.size()-1));
					}
					else{
						break;
					}
				}
				s.add(str);
			}
			else if (str.equals("(")){
				s.add(str);
			}
			else if (str.equals(")")){
				String top=s.remove(s.size()-1);
				while (!top.equals("(")){
					prefix_List.add(top);
					top=s.remove(s.size()-1);
				}
			}
			else{
				prefix_List.add(str);
			}
		}
		while (!s.isEmpty()){
			prefix_List.add(s.remove(s.size()-1));
		}
	}

	int priority(String str){
		if (str.equals("(") || str.equals(")")){
			return 0;
		}
		else if (str.equals("+") || str.equals("-")){
			return 1;
		}
		else if (str.equals("*") || str.equals("/")){
			return 2;
		}
		return -1;
	}

	public void printer(ArrayList <String> l){
		for (int c1=0; c1<l.size(); c1++){
			System.out.print(l.get(c1));
		}
		System.out.println();
	}

	public void makeSymbol(){
		switch ((int) (Math.random()*4)){
		case 0:
			list.add("+");
			break;
		case 1:
			list.add("-");
			break;
		case 2:
			list.add("*");
			break;
		case 3:
			list.add("/");
			break;
		}
	}
	
	public void makeList(int cycle){
		boolean flag=false;
		for (int c1=0; c1<cycle; c1++){
			switch ((int) (Math.random()*2)){
			case 0:
				list.add("(");
				flag=true;
			case 1:
				list.add(String.valueOf((int) (Math.random()*10)+1));
				break;
			}
			makeSymbol();
			list.add(String.valueOf((int) (Math.random()*10)+1));
			int rands=(int) (Math.random()*2);
			for (int c2=0; c2<rands; c2++){
				makeSymbol();
				list.add(String.valueOf((int) (Math.random()*10)+1));
			}
			if (flag){
				list.add(")");
				flag=false;
			}
			makeSymbol();
		}
		list.add(String.valueOf((int) (Math.random()*10)+1));
	}
}
