# HỆ THỐNG QUẢN LÝ KHÁCH SẠN - NHÓM 06

Tài liệu này tóm tắt toàn bộ cấu trúc dự án và các tính năng đã hoàn thiện tính đến ngày 26/03/2026.

---

## 1. Công Nghệ & Kiến Trúc
*   **Ngôn ngữ**: Java (JDK 17+)
*   **Framework Giao diện**: 
    *   **JavaFX**: Toàn bộ nội dung hiển thị (Dashboard, Room, Booking).
    *   **Swing (JFXPanel)**: Dùng làm khung `MainFrame` để nhúng JavaFX.
*   **Cơ sở dữ liệu**: SQL Server.
*   **Mô hình**: DAO (Data Access Object) + Entity + GUI.

---

## 2. Các Tính Năng Đã Hoàn Thiện

### A. Hệ Thống Điều Hướng (MainFrame)
*   **Sidebar Thông Minh**: Phân quyền theo vai trò (`vaiTro`). 
    *   Tài khoản `Admin`: Hiển thị đầy đủ tất cả các mục.
    *   Tài khoản `Nhân viên`: Ẩn mục **Nhân viên** và **Thống kê**.
*   **Hiệu ứng UI**: Các nút menu bo tròn, căn lề trái, có hiệu ứng Hover và Highlight khi được chọn. Font chữ cố định 15px không bị co giãn.
*   **Đồng hồ**: Hiển thị thời gian thực ở góc Sidebar.

### B. Tổng Quan (DashboardView)
*   **Thống kê nhanh**: Tổng số phòng, số phòng đang ở, phòng trống và doanh thu trong ngày.
*   **Danh sách nhanh**: Hiển thị 10 phiếu đặt gần nhất và 10 phòng đang trống.
*   **Làm mới**: Tự động tải lại dữ liệu mỗi khi chuyển tab hoặc nhấn nút "Làm mới".

### C. Sơ Đồ Phòng (RoomView)
*   **Trạng thái màu sắc**: 
    *   🟢 **Xanh lá**: Phòng trống.
    *   🔴 **Đỏ**: Phòng đang ở.
    *   🟡 **Vàng**: Phòng đang sửa chữa.
*   **Tìm kiếm & Lọc**: Ô nhập mã phòng (Real-time), lọc theo trạng thái và loại phòng (Standard, VIP...).
*   **Tương tác**: 
    *   **Chuột phải**: Menu nhanh (Nhận phòng, Dịch vụ, Trả phòng).
    *   **Double-click**: Mở cửa sổ chi tiết phòng và khách hàng.

### D. Lập Phiếu Đặt Phòng (BookingView)
*   **Tra cứu khách hàng**: Nhập SĐT để tự động điền thông tin khách cũ.
*   **Chọn nhiều phòng**: Cơ chế **Click-to-Toggle** (Click chọn, Click lại bỏ chọn) không cần giữ phím Ctrl.
*   **Validation (Báo lỗi)**: 
    *   Ô nhập thiếu/sai sẽ bị **Highlight viền đỏ**.
    *   Thông báo lỗi tổng hợp hiện trong 1 Pop-up.
    *   Tự động **Focus** (trỏ chuột) vào ô lỗi đầu tiên sau khi đóng thông báo.
*   **Phím tắt**: Sử dụng phím `Enter` để di chuyển nhanh qua các ô nhập liệu.

### E. Quy Trình Thanh Toán (Check-out)
*   **Logic tính tiền**: 
    *   Tự động tính số ngày ở (Nếu < 1 ngày tính là 1 ngày).
    *   `Tổng tiền = (Tiền phòng + Tiền dịch vụ) * 1.08 (VAT)`.
*   **Kiểm tra nợ**: Hệ thống kiểm tra xem phiếu đặt đã có hóa đơn chưa. Nếu có, sẽ chỉ thu thêm phần chênh lệch.
*   **Đồng bộ**: Sau khi thanh toán, phòng tự động về trạng thái "Trống" và Phiếu đặt chuyển sang "DaThanhToan".

---

## 3. Các Logic Kỹ Thuật Quan Trọng (Cần Lưu Ý)

1.  **Auto-Reconnect (ConnectDB.java)**:
    *   Mỗi khi gọi `getConnection()`, hệ thống sẽ kiểm tra nếu kết nối bị đóng (`isClosed`) thì sẽ tự động khởi tạo lại. Tránh lỗi mất kết nối khi để máy treo lâu.
2.  **Thread Safety (Task JavaFX)**:
    *   Toàn bộ thao tác truy vấn Database nặng được bọc trong `Task` và chạy trên luồng phụ (`new Thread(task).start()`).
    *   Các lệnh cập nhật giao diện được bọc trong `Platform.runLater()` để tránh treo ứng dụng (Freeze UI).
3.  **Transaction (PhieuDatPhongDAO.java)**:
    *   Sử dụng `setAutoCommit(false)` khi đặt phòng. Đảm bảo nếu tạo chi tiết phiếu lỗi thì sẽ không lưu phiếu đặt và không đổi màu phòng.

---

## 4. Hướng Phát Triển Tiếp Theo
*   [ ] Hoàn thiện màn hình **Quản lý Dịch vụ**: Cho phép thêm/sửa/xóa dịch vụ.
*   [ ] Hoàn thiện màn hình **Thống kê**: Vẽ biểu đồ doanh thu theo tháng/năm bằng JavaFX Chart.
*   [ ] Chức năng **Đổi phòng**: Chuyển khách từ phòng này sang phòng khác mà vẫn giữ nguyên phiếu đặt.
*   [ ] Chức năng **Thêm dịch vụ**: Cho phép lễ tân nhập thêm dịch vụ khách dùng (nước uống, đồ ăn) vào phòng đang ở.

---
*Tài liệu được khởi tạo bởi Gemini CLI Assistant.*
