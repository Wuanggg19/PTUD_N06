# HƯỚNG DẪN DỰ ÁN QUẢN LÝ KHÁCH SẠN (NHÓM 06)

Tài liệu này tóm tắt toàn bộ các bước thiết lập và kiểm thử dự án Java + SQL Server đã thực hiện.

## 1. Cấu trúc Dự án (Src)
- **connectDB**: Chứa `ConnectDB.java` (Cấu hình kết nối SQL Server).
- **entity**: Chứa các lớp thực thể (NhanVien, Phong, KhachHang, DichVu, HoaDon...).
- **dao**: Chứa các lớp truy cập dữ liệu (NhanVienDAO, PhongDAO...).
- **test**: Chứa các file chạy thử (MainTest, BusinessTest).

## 2. Thiết lập Database (SQL Server)
### Bước 1: Chạy Script SQL
- Chạy file `libary/SQLQueryKhachSan.sql` để tạo Database và bảng.
- Chạy file `libary/SQLQueryKhachSanData.sql` để nạp dữ liệu mẫu.

### Bước 2: Cấu hình tài khoản 'sa' (Sửa lỗi Login Failed)
Nếu gặp lỗi đăng nhập user 'sa', hãy chạy lệnh này trong SQL Server Management Studio (SSMS):
```sql
ALTER LOGIN sa WITH PASSWORD = '123456', -- Thay bằng mật khẩu bạn chọn
CHECK_POLICY = OFF, 
CHECK_EXPIRATION = OFF;
GO
ALTER LOGIN sa ENABLE;
GO
```
**Lưu ý:** Sau khi chạy lệnh, phải chuột phải vào Server chọn **Restart**.

### Bước 3: Thêm thư viện JDBC
- Phải thêm file `mssql-jdbc-xxx.jar` vào mục **Referenced Libraries** của dự án để Java có thể kết nối với SQL.

## 3. Cấu hình Java (ConnectDB.java)
Mở file `src/connectDB/ConnectDB.java` và đảm bảo các thông tin sau chính xác:
- **databaseName**: `QuanLyKhachSan_Nhom06_v4`
- **user**: `sa`
- **password**: `123456` (Mật khẩu bạn đã đặt ở Bước 2)

## 4. Cách Kiểm thử
- **MainTest.java**: Chạy file này để kiểm tra kết nối Database, danh sách nhân viên và chức năng Đăng nhập.
- **BusinessTest.java**: Chạy file này để kiểm tra các ràng buộc nghiệp vụ (Mã NV phải là NVxxx, Mật khẩu >= 6 ký tự).

## 5. Các lỗi thường gặp
- **Lỗi Login failed for user 'sa'**: Do mật khẩu sai hoặc chưa bật "Mixed Mode Authentication" trong Properties của Server.
- **Lỗi Violation of UNIQUE KEY**: Do bạn cố gắng thêm một dữ liệu (như tên đăng nhập) đã tồn tại trong Database.
- **Lỗi TCP/IP connection**: Do chưa bật TCP/IP Port 1433 trong *SQL Server Configuration Manager*.

---
*Chúc bạn hoàn thành tốt bài tập môn Phát triển ứng dụng!*
