package client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;

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

public class Client_SignUp extends JFrame {

	private static final long serialVersionUID = ServerUtils.getSerialversionuid();
	private static final int SERVER_PORT = ServerUtils.getServerPort();
	private static final String SERVER_ADDRESS = ServerUtils.getServerAddress();
	private JPanel contentPane;
	private JTextField tfEmail;
	private JTextField tfPassword;
	private JTextField tfRePassword;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client_SignUp frame = new Client_SignUp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Client_SignUp() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Đăng ký (Client)");
		setBounds(100, 100, 600, 275);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel("MAIL APPLICATION");
		lblTitle.setForeground(Color.RED);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(201, 24, 163, 13);
		contentPane.add(lblTitle);
		
		JLabel lblHeading = new JLabel("Tạo tài khoản mới");
		lblHeading.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeading.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblHeading.setBounds(201, 47, 163, 15);
		contentPane.add(lblHeading);
		
		tfEmail = new JTextField();
		tfEmail.setColumns(10);
		tfEmail.setBounds(244, 93, 160, 19);
		contentPane.add(tfEmail);
		
		JLabel lblEmail = new JLabel("Email");
		lblEmail.setBounds(132, 96, 102, 13);
		contentPane.add(lblEmail);
		
		JButton btnSignUp = new JButton("ĐĂNG KÝ");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createAccount();
			}
		});
		btnSignUp.setBounds(201, 201, 143, 21);
		contentPane.add(btnSignUp);
		
		JLabel lblPassword = new JLabel("Mật khẩu");
		lblPassword.setBounds(132, 125, 102, 13);
		contentPane.add(lblPassword);
		
		tfPassword = new JPasswordField();
		tfPassword.setColumns(10);
		tfPassword.setBounds(244, 122, 160, 19);
		contentPane.add(tfPassword);
		
		JLabel lblRePassword = new JLabel("Nhập lại mật khẩu");
		lblRePassword.setBounds(132, 153, 102, 13);
		contentPane.add(lblRePassword);
		
		tfRePassword = new JPasswordField();
		tfRePassword.setColumns(10);
		tfRePassword.setBounds(244, 150, 160, 19);
		contentPane.add(tfRePassword);
		
		JButton btnBackLogin = new JButton("VỀ ĐĂNG NHẬP");
		btnBackLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				Client_Login main = new Client_Login();
//	            main.setLocationRelativeTo(null);
//				main.setVisible(true);
				setVisible(false);
			}
		});
		btnBackLogin.setBounds(10, 10, 143, 21);
		contentPane.add(btnBackLogin);
	}
	
	private void createAccount() {
		try {
			if (tfEmail.getText().length() <= 0) {
				JOptionPane.showMessageDialog(this, "Email không thể bỏ trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (tfPassword.getText().length() <= 0) {
				JOptionPane.showMessageDialog(this, "Mật khẩu không thể bỏ trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (!tfRePassword.getText().equals(tfPassword.getText())) {
				JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			DatagramSocket clientSocket = new DatagramSocket();
		    InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
		    String email = tfEmail.getText();
		    String password = tfPassword.getText();
		    
		    // Mã hóa password (MD5) => kết quả thu được hexString
//		    MessageDigest md = MessageDigest.getInstance("MD5");  
//		    byte[] result = md.digest(password.getBytes());
//		    StringBuilder hexString = new StringBuilder();
//            for (byte b : result) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
		    sendRequest("CREATE_ACCOUNT " + email + " " + password, serverIP, SERVER_PORT, clientSocket);
		    
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
            		Client_Login main = new Client_Login();
		            main.setLocationRelativeTo(null);
					main.setVisible(true);
					setVisible(false);
            		break;
            	case "ERROR":
            		JOptionPane.showMessageDialog(this, requestParts[1], "Thông báo", JOptionPane.WARNING_MESSAGE);
            		break;
            }
		} catch(Exception e) {}
	}
	
	private static void sendRequest(String request, InetAddress serverIP, int serverPort, DatagramSocket clientSocket) throws Exception {
        byte[] sendData = request.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
        clientSocket.send(sendPacket);
    }
}