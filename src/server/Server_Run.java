package server;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import utils.ServerUtils;

public class Server_Run extends JFrame {

	private static final long serialVersionUID = ServerUtils.getSerialversionuid();
	private static final int SERVER_PORT = ServerUtils.getServerPort();
	private static final String SERVER_ADDRESS = ServerUtils.getServerAddress();
	private JPanel contentPane;
	private JTextField tfPort;
	private JTextArea taInfo;
	private DatagramSocket serverSocket;
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server_Run frame = new Server_Run();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// Hàm để khởi động server
	private void startServer() {
		Thread serverThread = new Thread(() -> {
			try {
				// Khởi tạo socket server UDP
				InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
				serverSocket = new DatagramSocket(SERVER_PORT, serverIP);
		        
		        logMessage("Mail Server UDP đang khởi chạy trên port " + SERVER_PORT + "...");
		        
		        while (!serverSocket.isClosed()) {
		        	// Nhận yêu cầu từ client
		            byte[] receiveData = new byte[1024];
		            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		            serverSocket.receive(receivePacket);

		            String clientRequest = new String(receivePacket.getData(), 0, receivePacket.getLength());
		            InetAddress clientIP = receivePacket.getAddress();
		            int clientPort = receivePacket.getPort();

		            logMessage("Received: " + clientRequest);

		            String[] requestParts = clientRequest.split(" ", 3);
		            String command = requestParts[0];
		            
		            switch (command) {
			            case "CREATE_ACCOUNT":
			                createAccount(requestParts[1], requestParts[2], clientIP, clientPort, serverSocket);
			                break;
			            case "SEND_EMAIL":
			            	requestParts = clientRequest.split(" ", 2);
			            	requestParts = requestParts[1].split("\n", 4);
			            	sendMessage(requestParts[0], requestParts[1], requestParts[2], requestParts[3], clientIP, clientPort, serverSocket);
			                break;
			            case "LOGIN":
			                login(requestParts[1], requestParts[2], clientIP, clientPort, serverSocket);
			                break;
			            case "GET_MAILS":
			            	getMailList(requestParts[1], clientIP, clientPort, serverSocket);
			            	break;
			            case "GET_MAIL_CONTENT":
			            	getMailContent(requestParts[1], clientIP, clientPort, serverSocket);
			            	break;
			            case "LOGOUT":
			            	requestParts = clientRequest.split(" ", 2);
			            	logMessage(requestParts[1] + " đã thoát");
			            	break;
			            default:
			                logMessage("Không tìm thấy lệnh: " + command);
		            }
		        }
			} catch (IOException e) {
				logMessage("Lỗi khi khởi chạy server: " + e.getMessage());
			}
		});
		
		serverThread.start(); // Bắt đầu server trong luồng riêng
	}
	
	private static void createAccount(String accountName, String password, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
		try {
			String response;
			Connection conn = DatabaseConnection.getConnection();
			String query = "SELECT COUNT(*) FROM account WHERE username = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, accountName);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next() && rs.getInt(1) > 0) {
				response = "ERROR Tài khoản đã tồn tại.";
				sendResponse(response, clientIP, clientPort, serverSocket);
				rs.close();
				ps.close();
				conn.close();
				return;
			}
			
			query = "INSERT INTO account (username, pwd) VALUES (?, ?)";
			ps = conn.prepareStatement(query);
			ps.setString(1, accountName);
			ps.setString(2, password);
			ps.executeUpdate();
			response = "SUCCESS Tài khoản " + accountName + " đã tạo thành công.";
			sendResponse(response, clientIP, clientPort, serverSocket);
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
	
	private void sendMessage(String emailTitle, String email, String emailReceived, String content, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
		try {
			String response;
			Connection conn = DatabaseConnection.getConnection();
			String query = "SELECT id FROM account WHERE username = ?";
			PreparedStatement ps1 = conn.prepareStatement(query);
			PreparedStatement ps2 = conn.prepareStatement(query);
			ps1.setString(1, email);
			ps2.setString(1, emailReceived);
			ResultSet rs1 = ps1.executeQuery();
			ResultSet rs2 = ps2.executeQuery();
			
			if (rs1.next() && rs1.getInt(1) > 0) {
				int id_sent = rs1.getInt("id");
				if (rs2.next() && rs2.getInt(1) > 0) {
					int id_receive = rs2.getInt("id");
					query = "INSERT INTO mail (id_sent, id_receive, title, content, date) VALUES (?, ?, ?, ?, ?)";
					ps1 = conn.prepareStatement(query);
					ps1.setInt(1, id_sent);
					ps1.setInt(2, id_receive);
					ps1.setString(3, emailTitle);
					ps1.setString(4, content);
					ps1.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
					ps1.executeUpdate();
					response = "SUCCESS Gửi thư thành công.";
					sendResponse(response, clientIP, clientPort, serverSocket);
				} else {
					response = "ERROR Tài khoản người nhận không tồn tại.";
					sendResponse(response, clientIP, clientPort, serverSocket);
				}
			} else {
				response = "ERROR Tài khoản người gửi không tồn tại.";
				sendResponse(response, clientIP, clientPort, serverSocket);
			}
			
			rs1.close();
			rs2.close();
			ps1.close();
			ps2.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
//            String response = "SUCCESS Thư đã được gửi thành công từ " + email + " đến " + emailReceived + ".";
//            sendResponse(response, clientIP, clientPort, serverSocket);
//        } else {
//            sendResponse("ERROR Tài khoản không tồn tại.", clientIP, clientPort, serverSocket);
//        }
	}
    
    private void login(String accountName, String password, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
    	try {
			String response;
			Connection conn = DatabaseConnection.getConnection();
			String query = "SELECT id, pwd FROM account WHERE username = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, accountName);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next() && rs.getInt(1) > 0) {
				if (password.equals(rs.getString("pwd"))) {
					int accountID = rs.getInt("id");
					query = "SELECT id, title FROM mail WHERE id_receive = ?";
					ps = conn.prepareStatement(query);
					ps.setInt(1, accountID);
					rs = ps.executeQuery();
					
//					StringBuilder mailList = new StringBuilder();
//					while (rs.next()) {
//						mailList.append(rs.getInt("id")).append(":").append(rs.getString("title")).append("\n");
//					}
					
					logMessage(accountName + " đã kết nối");
					sendResponse("SUCCESS ", clientIP, clientPort, serverSocket);
				} else {
					response = "ERROR Sai mật khẩu.";
					sendResponse(response, clientIP, clientPort, serverSocket);
				}
			} else {
				response = "ERROR Tài khoản không tồn tại.";
				sendResponse(response, clientIP, clientPort, serverSocket);
			}
			
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    private void getMailList(String accountName, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
    	try {
			String response;
			Connection conn = DatabaseConnection.getConnection();
			String query = "SELECT id FROM account WHERE username = ?";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, accountName);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next() && rs.getInt(1) > 0) {
				int accountID = rs.getInt("id");
	            query = "SELECT id FROM mail WHERE id_receive = ? ORDER BY date DESC";
				ps = conn.prepareStatement(query);
				ps.setInt(1, accountID);
				rs = ps.executeQuery();
				
				StringBuilder mailList = new StringBuilder();
				while (rs.next()) {
	                String id = rs.getString("id");
	                mailList.append(id).append("\n");
				}
				
				if (mailList.length() == 0) {
					sendResponse("SUCCESS Hộp thư trống", clientIP, clientPort, serverSocket);	
	            } else {
					sendResponse("SUCCESS " + mailList.toString(), clientIP, clientPort, serverSocket);	
	            }
			} else {
				response = "ERROR Tài khoản không tồn tại.";
				sendResponse(response, clientIP, clientPort, serverSocket);
			}
			
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    private void getMailContent(String id, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
    	try {
			String response;
			Connection conn = DatabaseConnection.getConnection();
			String query = "SELECT id_sent, title, content, date FROM mail WHERE id = ? ORDER BY date DESC";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, Integer.valueOf(id));
			ResultSet rs = ps.executeQuery();
			
			if (rs.next() && rs.getInt(1) > 0) {
				int senderId = rs.getInt("id_sent");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String date = rs.getTimestamp("date").toString();
                
                String senderQuery = "SELECT username FROM account WHERE id = ?";
                PreparedStatement senderStmt = conn.prepareStatement(senderQuery);
                senderStmt.setInt(1, senderId);
                ResultSet senderRs = senderStmt.executeQuery();
                
                String senderName = "Unknown";
                if (senderRs.next()) {
                    senderName = senderRs.getString("username");
                }
				
				StringBuilder mailContent = new StringBuilder();
	            mailContent.append(title).append("\n");
	            mailContent.append(date).append("\n");
	            mailContent.append(senderName).append("\n");
	            mailContent.append(content);
				sendResponse("SUCCESS " + mailContent.toString(), clientIP, clientPort, serverSocket);	
			} else {
				response = "ERROR Thư không tồn tại.";
				sendResponse(response, clientIP, clientPort, serverSocket);
			}
			
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    private static void sendResponse(String response, InetAddress clientIP, int clientPort, DatagramSocket serverSocket) throws IOException {
        byte[] sendData = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
        serverSocket.send(sendPacket);
    }

	/**
	 * Create the frame.
	 */
	public Server_Run() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Máy chủ (Server)");
		setBounds(100, 100, 700, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(80, 426, 25, 13);
		contentPane.add(lblPort);
		
		tfPort = new JTextField();
		tfPort.setText("1234");
		tfPort.setBounds(115, 423, 96, 19);
		contentPane.add(tfPort);
		tfPort.setColumns(10);
		
		JButton btnStart = new JButton("Khởi động máy chủ");
		btnStart.setBounds(234, 422, 143, 21);
		contentPane.add(btnStart);
		
		JButton btnStop = new JButton("Dừng máy chủ");
		btnStop.setEnabled(false);
		btnStop.setBounds(387, 422, 143, 21);
		contentPane.add(btnStop);
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tfPort.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Port không thể bỏ trống", "Thông báo", JOptionPane.WARNING_MESSAGE);
					return;
				}
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
				startServer();  // Khởi chạy server trong luồng riêng
			}
		});
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();  // Dừng server
					logMessage("Server đã dừng.");
					btnStart.setEnabled(true);
					btnStop.setEnabled(false);
				}
			}
		});
		
		taInfo = new JTextArea();
		taInfo.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(taInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(48, 81, 587, 304);
		contentPane.add(scrollPane);
		
		JLabel lblTitle = new JLabel("MAIL APPLICATION");
		lblTitle.setForeground(Color.RED);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(251, 24, 163, 13);
		contentPane.add(lblTitle);
		
		JLabel lblRole = new JLabel("(Server)");
		lblRole.setHorizontalAlignment(SwingConstants.CENTER);
		lblRole.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblRole.setBounds(251, 47, 163, 15);
		contentPane.add(lblRole);
	}
	
	// Hàm để log thông tin vào JTextArea
	private void logMessage(String message) {
		java.util.Date date = new java.util.Date();
		taInfo.append(sdf.format(date) + ": "+ message + "\n");
	}
}
