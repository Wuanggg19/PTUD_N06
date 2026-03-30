# 🏨 PHẦN MỀM QUẢN LÝ KHÁCH SẠN — NHÓM 06

## 📦 Yêu cầu cài đặt (Ai cũng phải làm bước này)

### 1. Phần mềm cần có
| Phần mềm | Phiên bản | Link tải |
|----------|-----------|----------|
| JDK | 21 trở lên | https://adoptium.net |
| VS Code | Mới nhất | https://code.visualstudio.com |
| Extension Pack for Java | — | Cài trong VS Code |
| SQL Server | 2019/2022 | Có sẵn, hoặc dùng bản Express |
| SQL Server Management Studio (SSMS) | — | Để import database |

---

### 2. Import Database

1. Mở **SSMS**, kết nối vào SQL Server local của bạn
2. Mở file `libary/SQLQueryKhachSan_v2.sql` → **Execute** (tạo cấu trúc bảng)
3. Mở file `libary/SQLQueryKhachSan_Data_v2.sql` → **Execute** (thêm dữ liệu mẫu)
4. Kiểm tra: Database `QuanLyKhachSan_N06_v5` phải xuất hiện trong danh sách

---

### 3. Cấu hình kết nối Database (QUAN TRỌNG ⚠)

Mở file **`db.properties`** ở thư mục gốc project và sửa mật khẩu:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan_N06_v5;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=MẬT_KHẨU_SQL_SERVER_CỦA_BẠN
```

> ⚠ **Mỗi người sửa mật khẩu theo máy của mình. KHÔNG sửa code Java.**

---

### 4. Chạy chương trình

Trong VS Code:
1. Mở file `src/gui/StartUpGui.java`
2. Nhấn **Run** (hoặc dùng cấu hình `StartUpGui` trong `.vscode/launch.json`)
3. Đăng nhập với tài khoản mẫu (xem trong file SQL data)

---

## 📋 Cấu trúc Menu — Phân bổ chức năng

```
🏠 Tổng quan            ← DashboardView.java          ✅ Hoàn thiện
├─ 📅 QUẢN LÝ ĐẶT PHÒNG
│   ├─ Đặt phòng        ← BookingView.java             ✅ Hoàn thiện
│   ├─ Danh sách đặt    ← BookingListView.java         🔧 CẦN LÀM
│   ├─ Check-in         ← CheckInView.java             🔧 CẦN LÀM
│   └─ Check-out        ← CheckoutView.java            ✅ Hoàn thiện
├─ 🛏 QUẢN LÝ KHÁCH SẠN
│   ├─ Phòng (sơ đồ)   ← RoomView.java                ✅ Hoàn thiện
│   ├─ Loại phòng       ← RoomManagementView.java      ✅ Hoàn thiện
│   └─ Dịch vụ          ← ServiceView.java             🔧 CẦN LÀM
├─ 👥 KHÁCH HÀNG
│   └─ Khách hàng       ← CustomerView.java            ✅ Hoàn thiện
├─ 💰 THANH TOÁN
│   ├─ Thanh toán       ← PaymentView.java             🔧 CẦN LÀM
│   └─ Hóa đơn          ← InvoiceView.java             🔧 CẦN LÀM
├─ 📊 BÁO CÁO
│   ├─ TK Doanh thu     ← RevenueStatsView.java        🔧 CẦN LÀM
│   ├─ TK Phòng         ← RoomStatsView.java           🔧 CẦN LÀM
│   └─ TK Khách hàng   ← CustomerStatsView.java        🔧 CẦN LÀM
└─ ⚙ HỆ THỐNG
    ├─ Tài khoản         ← AccountView.java             🔧 CẦN LÀM
    └─ Đổi mật khẩu     ← ChangePasswordView.java      🔧 CẦN LÀM
```

---

## 🔧 Hướng dẫn implement một chức năng (TODO)

Mỗi file skeleton đã có:
- **Giao diện đầy đủ** (bảng, nút, form...)
- **Comment TODO rõ ràng** chỉ đúng chỗ cần code
- **Phương thức đã khai báo** sẵn, chỉ cần điền logic vào

### Ví dụ — Implement `loadData()` trong `ServiceView.java`:

```java
public void loadData() {
    try {
        if (ConnectDB.getConnection() == null) return;
        DichVuDAO dao = new DichVuDAO();
        List<DichVu> list = dao.getAllDichVu();  // Gọi DAO có sẵn
        dataList.setAll(list != null ? list : new ArrayList<>());
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### Quy tắc code chung
- **Package**: Luôn khai báo `package gui;` ở đầu file view
- **Không xóa phương thức** đã có, chỉ viết code vào bên trong
- **Tên class = tên file** (Java phân biệt chữ hoa/thường)
- **Không sửa** các file đang hoạt động tốt (xem danh sách ✅ ở trên)

---

## 📁 Cấu trúc dự án

```
PhanMemQuanLyKhachSan/
├── src/
│   ├── connectDB/
│   │   └── ConnectDB.java          ← Kết nối DB (đọc từ db.properties)
│   ├── dao/                        ← Thao tác CSDL (đã có, dùng thẳng)
│   ├── entity/                     ← Model dữ liệu (đã có, dùng thẳng)
│   └── gui/                        ← Toàn bộ giao diện
├── libary/
│   ├── javafx-sdk-26/              ← JavaFX SDK (không sửa)
│   ├── mssql-jdbc-12.4.2.jre11.jar← Driver SQL Server (không sửa)
│   └── *.sql                       ← Script tạo và import dữ liệu
├── db.properties                   ← ⚠ TỰ SỬA MẬT KHẨU CHO MÁY BẠN
└── .vscode/launch.json             ← Cấu hình chạy VS Code (không sửa)
```

---

## ❓ Lỗi thường gặp & Cách xử lý

| Lỗi | Nguyên nhân | Cách sửa |
|-----|-------------|----------|
| `Login failed for user 'sa'` | Sai mật khẩu DB | Sửa `db.password` trong `db.properties` |
| `Cannot connect to database` | SQL Server chưa chạy | Mở Services, bật `SQL Server (MSSQLSERVER)` |
| `package javafx does not exist` | Thiếu cấu hình JavaFX | Dùng cấu hình `StartUpGui` trong launch.json |
| Màn hình trắng khi click menu | View bị lỗi khi khởi tạo | Xem console, tìm dòng `ERROR: View [...]` |

---

*Cập nhật lần cuối: Nhóm 06 — Quản Lý Khách Sạn*
