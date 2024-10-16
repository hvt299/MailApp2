# XÂY DỰNG ỨNG DỤNG MAILAPP BẰNG JAVA SWING + MYSQL
## Tổng quan
* Ứng dụng sẽ giúp bạn gửi và nhận thư với nhau thông qua mạng LAN.
## Các công nghệ được sử dụng
* Ngôn ngữ: Java.
* Cơ sở dữ liệu: MySQL.
## Chức năng
* Đăng nhập, đăng ký tài khoản.
* Đọc thư.
* Gửi và nhận thư.
## Cách cài đặt
* Tải và cài đặt MySQL Web Community: https://dev.mysql.com/downloads/installer.
* Tạo database bằng file sql: https://github.com/hvt299/MailApp2/blob/main/src/server/mailserver.sql.
* Tải MySQL driver (MySQL Connector/J) và chèn thư viện vào project: https://mvnrepository.com/artifact/com.mysql/mysql-connector-j/8.3.0.
* Cấu hình lại URL, USER, PASSWORD khớp với cơ sở dữ liệu của bạn: DatabaseConnection.java.
* Cấu hình lại địa chỉ IP khớp với địa chỉ IP mạng của bạn: ServerUtils.java.
* Thực thi 2 file Server_Run.java để chạy server và Client_Login.java để chạy client.
## Tác giả
* Hứa Viết Thái ([hvt299](https://github.com/hvt299)).
* Phạm Thế Anh ([giodong34567](https://github.com/giodong34567)).
