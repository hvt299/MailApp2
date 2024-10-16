package client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import utils.ServerUtils;

import javax.swing.JScrollPane;
import java.awt.Component;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;

public class Client_Compose extends JFrame {

	private static final long serialVersionUID = ServerUtils.getSerialversionuid();
	private static final int SERVER_PORT = ServerUtils.getServerPort();
	private static final String SERVER_ADDRESS = ServerUtils.getServerAddress();
	private JPanel contentPane;
	private JTextField tfEmailReceived;
	private JTextField tfEmailTitle;
	private JTextArea taContent;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client_Compose frame = new Client_Compose("");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @param email 
	 */
	public Client_Compose(String email) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Soạn thư");
		setBounds(100, 100, 380, 580);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		tfEmailReceived = new JTextField();
		tfEmailReceived.setColumns(10);
		tfEmailReceived.setBounds(10, 33, 346, 25);
		contentPane.add(tfEmailReceived);
		
		JLabel lblEmailReceived = new JLabel("Email người nhận");
		lblEmailReceived.setBounds(10, 10, 102, 13);
		contentPane.add(lblEmailReceived);
		
		JButton btnSend = new JButton("GỬI");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage(email);
			}
		});
		btnSend.setBounds(213, 512, 143, 21);
		contentPane.add(btnSend);
		
		JLabel lblEmailTitle = new JLabel("Tiêu đề");
		lblEmailTitle.setBounds(10, 68, 102, 13);
		contentPane.add(lblEmailTitle);
		
		tfEmailTitle = new JTextField();
		tfEmailTitle.setColumns(10);
		tfEmailTitle.setBounds(10, 91, 346, 25);
		contentPane.add(tfEmailTitle);
		
		JLabel lblContent = new JLabel("Nội dung");
		lblContent.setBounds(10, 126, 102, 13);
		contentPane.add(lblContent);
		
		taContent = new JTextArea();
		
		JScrollPane scrollPane = new JScrollPane(taContent, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 149, 346, 353);
		contentPane.add(scrollPane);
	}
	
	private void sendMessage(String email) {
		try {
			if (tfEmailReceived.getText().length() <= 0) {
				JOptionPane.showMessageDialog(this, "Email người nhận không thể bỏ trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (tfEmailTitle.getText().length() <= 0) {
				JOptionPane.showMessageDialog(this, "Tiêu đề không thể bỏ trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (taContent.getText().length() <= 0) {
				JOptionPane.showMessageDialog(this, "Nội dung không thể bỏ trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			DatagramSocket clientSocket = new DatagramSocket();
		    InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
		    
		    String emailReceived = tfEmailReceived.getText();
		    String emailTitle = tfEmailTitle.getText();
		    String content = taContent.getText();
		    
//		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//            LocalDateTime now = LocalDateTime.now();
//            String formattedDate = now.format(formatter);
		    
		    sendRequest("SEND_EMAIL " + emailTitle + "\n" + email + "\n" + emailReceived + "\n" + content, serverIP, SERVER_PORT, clientSocket);
		    
		    // Nhận phản hồi từ server
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
            
            String[] requestParts = serverResponse.split(" ", 2);
            String command = requestParts[0];
            
            switch (command) {
            	case "SUCCESS":
            		JOptionPane.showMessageDialog(this, requestParts[1], "Thông báo", JOptionPane.INFORMATION_MESSAGE);
					setVisible(false);
            		break;
            	case "ERROR":
            		JOptionPane.showMessageDialog(this, requestParts[1], "Thông báo", JOptionPane.WARNING_MESSAGE);
            		break;
            }
		} catch (Exception e) {}
	}
	
	private static void sendRequest(String request, InetAddress serverIP, int serverPort, DatagramSocket clientSocket) throws Exception {
        byte[] sendData = request.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
        clientSocket.send(sendPacket);
    }

}