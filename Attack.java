package Programming_project_2_Speck_Attack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.swing.text.PlainDocument;

import edu.rit.util.Hex;

class Attack{

	String [] plaintext ;
	String [] ciphertext;
	Key_values[] k =  new Key_values[(int) Math.pow(2, 16)];
	Key_values subkeys;
	int hits=0;

	public Attack(String [] plaintext , String[] ciphertext){
		this.plaintext = plaintext;
		this.ciphertext = ciphertext;
	}


	public void Find_subkey(){
		for( int i=0; i < plaintext.length; i++){


			int p_text = Hex.toInt(plaintext[i]);
			short p_x = (short)((p_text >> 16) & 0x0000FFFF);
			short p_y = (short)(p_text & 0x0000FFFF);
			short B_0 = value_of_B_0(i);
			int j;

			int counter=0;



			for( j =0 ; j <(1<<16); j++){
				short key_1= (short) (j & 0x0000FFFF);
				short x= (short) ((( l_right_rotate(p_x) + p_y)^ key_1 ));
				short y = (short) (k_left_rotate(p_y)^x);

				short B_1 = (short) (B_0 ^ k_left_rotate(y));

				short key_2 = (short)(((l_right_rotate(x) + y) ^ B_1));

				short key_3 = (short)((l_right_rotate(B_1)+B_0) ^ ((Hex.toInt(ciphertext[i]))>>16) & 0x0000FFFF); 
				if(k[j]==null){
					k[j]= new Key_values(key_1, key_2, key_3);	
				}

				else if(k[j].key_2==key_2 && k[j].key_3==key_3){
					k[j].counter++;
					if(k[j].counter>hits){
						hits=k[j].counter;
						subkeys=k[j];
					}
				}
			}
		}
		System.out.println(Hex.toString(subkeys.key_1) +"\n"+ Hex.toString(subkeys.key_2) + "\n" + Hex.toString(subkeys.key_3));
	}


	private short value_of_B_0(int i) {
		int  cipher_text = Hex.toInt(ciphertext[i]);
		short c_x = (short)((cipher_text >> 16) & 0x0000FFFF);
		short c_y = (short)(cipher_text & 0x0000FFFF);
		short temp = (short) right_rotate((short)(c_x ^ c_y));

		return temp;
	}
	private short l_right_rotate(short s) {
		short x= (short) ((s& 0x0000FFFF)>>7);
		short y= (short) ((s& 0x0000FFFF)<<9);
		short temp = (short) (y|x);
		return temp;
	}

	private short k_left_rotate(short s){
		short y= (short)( (s& 0x0000FFFF)<<2);
		short x= (short)((s& 0x0000FFFF)>>14);
		short temp = (short) (y|x);
		return temp;
	}
	private short right_rotate (short s){
		short y = (short)((s & 0x0000FFFF)>>2);
		short x = (short)((s & 0x0000FFFF)<<14);
		short temp = (short) (y|x);
		return temp;
	}

	public static void main(String[] args) throws IOException {

		if(args.length!=1){
			usage();
		}
		FileInputStream in = new FileInputStream(args[0]);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String temp;
		ArrayList<String>  l=new ArrayList<String>();
		try{
			while((temp=br.readLine())!=null){
				String[] o = temp.split("\\s+");

				l.add((String)o[0]);
				l.add((String)o[1]);
			}
			
		}
		catch(Exception e){
			if(l.size()%2!=0){
				System.err.println("Usage :  Attack ");
				System.err.println("<Filename> = Insufficient data");
				System.exit(1);
			}
		}
		
			
		int counter1=0;
		int counter2=0;
		String [] plaintext = new String[l.size()/2];
		String [] ciphertext = new String[l.size()/2];
		for( int i =0 ; i<l.size(); i++){
			if( i % 2 ==0){
				plaintext[counter1]=l.get(i);
				counter1++;

			}
			else if(i%2!=0){
				ciphertext[counter2]= l.get(i);
				counter2++;

			}
		}

		Attack a = new Attack(plaintext,ciphertext);
		a.Find_subkey();
	}


	 private static void usage()  {
     System.err.println ("Usage: java Attack <Filename>");
     System.err.println ("<Filename> = File containing pairs of plaintext and ciphertext");
     System.exit (1);
     }
}