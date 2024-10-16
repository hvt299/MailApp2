package client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

import javax.swing.DefaultListModel;
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
import javax.swing.JList;
import java.awt.Component;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;

import utils.ServerUtils;

import javax.swing.event.ListSelectionEvent;

public class Client_Home extends JFrame {

	private static final long serialVersionUID = ServerUtils.getSerialversionuid();
	private static final int SERVER_PORT = ServerUtils.getServerPort();
	private static final String SERVER_ADDRESS = ServerUtils.getServerAddress();
	private JPanel contentPane;
	private JTextArea taInfo;
	private JLabel lblEmailTitle;
	private JLabel lblTime;
	private JLabel lblFrom;
	private JLabel lblTo;
	private DefaultListModel<String> listModel = new DefaultListModel<>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client_Home frame = new Client_Home("");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @param requestParts 
	 * @param email 
	 */
	public Client_Home(String email) {
//		String[] requestParts = mailList.split("\n");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Bạn đang đăng nhập với tài khoản email: " + email);
		setBounds(100, 100, 850, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnCompose = new JButton("Thư mới");
		btnCompose.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnCompose.setBounds(10, 10, 170, 40);
		contentPane.add(btnCompose);
		
		btnCompose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client_Compose main = new Client_Compose(email);
	            main.setLocationRelativeTo(null);
				main.setVisible(true);
			}
		});
		
		listModel = new DefaultListModel<>();
		startEmailCheckThread(email);
		
		JList<String> list = new JList<>(listModel);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
		            JList<?> source = (JList<?>) e.getSource();
		            Object selectedValue = source.getSelectedValue();
		            if (selectedValue != null) {
		            	String selected = source.getSelectedValue().toString();
		            	getMailContent(selected, email);		            	
		            }
		        }
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 60, 170, 393);
		contentPane.add(scrollPane);
		
		JLabel lblTitle = new JLabel("MAIL APPLICATION");
		lblTitle.setForeground(Color.RED);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(445, 10, 163, 13);
		contentPane.add(lblTitle);
		
		JLabel lblHeading = new JLabel("Home");
		lblHeading.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeading.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblHeading.setBounds(445, 30, 163, 15);
		contentPane.add(lblHeading);
		
		taInfo = new JTextArea();
		taInfo.setEditable(false);
		
		JScrollPane scrollPane2 = new JScrollPane(taInfo, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane2.setBounds(190, 162, 636, 291);
		contentPane.add(scrollPane2);
		
		lblEmailTitle = new JLabel("Title:");
		lblEmailTitle.setHorizontalAlignment(SwingConstants.LEFT);
		lblEmailTitle.setForeground(Color.BLACK);
		lblEmailTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblEmailTitle.setBounds(190, 62, 636, 13);
		contentPane.add(lblEmailTitle);
		
		lblTime = new JLabel("Time:");
		lblTime.setHorizontalAlignment(SwingConstants.LEFT);
		lblTime.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTime.setBounds(190, 85, 636, 15);
		contentPane.add(lblTime);
		
		lblFrom = new JLabel("From:");
		lblFrom.setHorizontalAlignment(SwingConstants.LEFT);
		lblFrom.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblFrom.setBounds(190, 110, 636, 15);
		contentPane.add(lblFrom);
		
		lblTo = new JLabel("To:");
		lblTo.setHorizontalAlignment(SwingConstants.LEFT);
		lblTo.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTo.setBounds(190, 137, 636, 15);
		contentPane.add(lblTo);
		
		this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
	            try {	
	            	DatagramSocket clientSocket = new DatagramSocket();
				    InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
	                sendRequest("LOGOUT " + email, serverIP, SERVER_PORT, clientSocket);
	                System.exit(0);
	            } catch (Exception e2) {
	                e2.printStackTrace();
	            }
            }
        });
	}
	
	private void getMailContent(String id, String email) {
		try {
			if (!id.equals("Hộp thư trống")) {
				DatagramSocket clientSocket = new DatagramSocket();
			    InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
	            sendRequest("GET_MAIL_CONTENT " + id, serverIP, SERVER_PORT, clientSocket);

	            // Nhận phản hồi từ server
	            byte[] receiveData = new byte[1024];
	            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	            clientSocket.receive(receivePacket);
	            String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
	            
	            String[] requestParts = serverResponse.split(" ", 2);
	            String command = requestParts[0];
	            
	            switch (command) {
	            	case "SUCCESS":
	            		String[] mailContent = requestParts[1].split("\n");
	            		lblEmailTitle.setText(mailContent[0]);
	            		lblTime.setText(mailContent[1]);
	            		lblFrom.setText(mailContent[2]);
	            		lblTo.setText(email);
	            		logMessage(mailContent[3]);
	            		break;
	            	case "ERROR":
	            		break;
	            }
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private void updateMailList(String mail) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
		    InetAddress serverIP = InetAddress.getByName(SERVER_ADDRESS);
            sendRequest("GET_MAILS " + mail, serverIP, SERVER_PORT, clientSocket);

            // Nhận phản hồi từ server
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
            
            String[] requestParts = serverResponse.split(" ", 2);
            String command = requestParts[0];
            
            switch (command) {
            	case "SUCCESS":
            		String[] mailList = requestParts[1].split("\n");
            		listModel.clear();
            		for (String item : mailList) {
            		    listModel.addElement(item);
            		}
            		break;
            	case "ERROR":
            		break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void startEmailCheckThread(String mail) {
        new Thread(() -> {
            while (true) {
                try {
                    updateMailList(mail); // Cập nhật danh sách email sau mỗi khoảng thời gian
                    Thread.sleep(5000); // Cập nhật mỗi 5 giây
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
	
	private static void sendRequest(String request, InetAddress serverIP, int serverPort, DatagramSocket clientSocket) throws Exception {
        byte[] sendData = request.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
        clientSocket.send(sendPacket);
    }
	
	// Hàm để log thông tin vào JTextArea
	private void logMessage(String message) {
		taInfo.setText(message + "\n");
		//taInfo.append(message + "\n");
	}
}
