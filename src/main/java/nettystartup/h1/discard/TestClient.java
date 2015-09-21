/**
 * 2015. 9. 14.
 * Copyright by yyh / Hubigo AIAL
 * TestClient.java
 */
package nettystartup.h1.discard;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestClient {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("127.0.0.1", 20203);
			
			InputStream input = socket.getInputStream();
			DataInputStream dataInput = new DataInputStream(input);
			char in;
			int inputInt;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String string = dataInput.readUTF();
			
			System.out.println(string);
//			while((inputInt = input.read()) != -1)	{
//				in = (char) inputInt;
//				System.out.print(in);
//			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
