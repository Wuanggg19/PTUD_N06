USE QuanLyKhachSan_Nhom06_v4;
GO

-- 10 Người dùng (Nhân viên) [cite: 1, 7]
INSERT INTO NguoiDung (maNguoiDung, tenDangNhap, matKhau, hoTen, soDienThoai, vaiTro, chucVu) VALUES
('NV001', 'admin', '123', N'Huỳnh Nhật Quang', '0901234567', N'Quản lý', N'Trưởng phòng'),
('NV002', 'letan1', '123', N'Phan Trần Phúc', '0902345678', N'Lễ tân', N'Nhân viên'),
('NV003', 'letan2', '123', N'Bùi Ngọc Thiện', '0903456789', N'Lễ tân', N'Nhân viên'),
('NV004', 'letan3', '123', N'Nguyễn Văn Sỹ', '0904567890', N'Lễ tân', N'Nhân viên'),
('NV005', 'letan4', '123', N'Huỳnh Công Thuận', '0905678901', N'Lễ tân', N'Nhân viên'),
('NV006', 'kt001', '123', N'Trần Thị A', '0906789012', N'Kế toán', N'Nhân viên'),
('NV007', 'baotri1', '123', N'Lê Văn B', '0907890123', N'Bảo trì', N'Kỹ thuật'),
('NV008', 'letan5', '123', N'Nguyễn Văn C', '0908901234', N'Lễ tân', N'Nhân viên'),
('NV009', 'letan6', '123', N'Phạm Thị D', '0909012345', N'Lễ tân', N'Nhân viên'),
('NV010', 'admin2', '123', N'Lý Văn E', '0900123456', N'Quản lý', N'Phó phòng');

-- 10 Khách hàng [cite: 15, 113]
INSERT INTO KhachHang (maKhachHang, tenKhachHang, diaChi, gioiTinh, soDienThoai) VALUES
('KH001', N'Nguyễn An', N'TP.HCM', 1, '0812345678'),
('KH002', N'Trần Bình', N'Hà Nội', 1, '0823456789'),
('KH003', N'Lê Chi', N'Đà Nẵng', 0, '0834567890'),
('KH004', N'Phạm Danh', N'Cần Thơ', 1, '0845678901'),
('KH005', N'Hoàng Yến', N'Hải Phòng', 0, '0856789012'),
('KH006', N'Vũ Nam', N'Bình Dương', 1, '0867890123'),
('KH007', N'Đặng Thu', N'Đồng Nai', 0, '0878901234'),
('KH008', N'Bùi Tiến', N'Long An', 1, '0889012345'),
('KH009', N'Ngô Mỹ', N'Tiền Giang', 0, '0890123456'),
('KH010', N'Đỗ Hùng', N'Vũng Tàu', 1, '0801234567');

-- 10 Phòng [cite: 12, 132]
INSERT INTO Phong (maPhong, loaiPhong, soGiuong, giaPhong, trangThai) VALUES
('P101', N'Đơn', 1, 500000, N'Trống'),
('P102', N'Đôi', 2, 800000, N'Trống'),
('P201', N'VIP', 1, 1500000, N'Đang ở'),
('P202', N'Gia đình', 3, 2000000, N'Trống'),
('P301', N'Đơn', 1, 550000, N'Đã đặt'),
('P302', N'Đôi', 2, 850000, N'Trống'),
('P401', N'VIP', 2, 2500000, N'Đang ở'),
('P402', N'Đơn', 1, 500000, N'Trống'),
('P501', N'Đôi', 2, 900000, N'Đã đặt'),
('P502', N'Gia đình', 4, 3000000, N'Trống');

-- 10 Dịch vụ [cite: 12, 113, 141]
INSERT INTO DichVu (maDichVu, tenDichVu, donGia, trangThai) VALUES
('DV01', N'Mì tôm', 20000, N'Sẵn sàng'),
('DV02', N'Nước suối', 15000, N'Sẵn sàng'),
('DV03', N'Giặt ủi', 50000, N'Sẵn sàng'),
('DV04', N'Spa', 500000, N'Sẵn sàng'),
('DV05', N'Ăn sáng', 100000, N'Sẵn sàng'),
('DV06', N'Coca Cola', 25000, N'Sẵn sàng'),
('DV07', N'Bia Tiger', 35000, N'Sẵn sàng'),
('DV08', N'Thuê xe máy', 150000, N'Sẵn sàng'),
('DV09', N'Massage', 400000, N'Sẵn sàng'),
('DV10', N'Đồ ăn vặt', 30000, N'Sẵn sàng');

-- 10 Phiếu đặt phòng [cite: 48, 139]
-- Lưu ý: giaBD ở đây lưu tổng tiền dự kiến ban đầu
INSERT INTO PhieuDatPhong (maDatPhong, maKhachHang, maNguoiDung, ngayDat, ngayNhan, ngayTra, giaBD, trangThai) VALUES
('DP001', 'KH001', 'NV002', '2026-03-20', '2026-03-21', '2026-03-23', 1000000, N'Đã nhận'),
('DP002', 'KH002', 'NV003', '2026-03-20', '2026-03-22', '2026-03-25', 2400000, N'Chờ nhận'),
('DP003', 'KH003', 'NV004', '2026-03-21', '2026-03-21', '2026-03-22', 1500000, N'Đã nhận'),
('DP004', 'KH004', 'NV002', '2026-03-21', '2026-03-25', '2026-03-27', 4000000, N'Chờ nhận'),
('DP005', 'KH005', 'NV005', '2026-03-22', '2026-03-22', '2026-03-24', 1100000, N'Đã nhận'),
('DP006', 'KH006', 'NV003', '2026-03-22', '2026-03-23', '2026-03-26', 7500000, N'Chờ nhận'),
('DP007', 'KH007', 'NV004', '2026-03-22', '2026-03-22', '2026-03-23', 500000, N'Đã nhận'),
('DP008', 'KH008', 'NV002', '2026-03-22', '2026-03-24', '2026-03-25', 900000, N'Chờ nhận'),
('DP009', 'KH009', 'NV005', '2026-03-22', '2026-03-22', '2026-03-25', 1500000, N'Đã nhận'),
('DP010', 'KH010', 'NV003', '2026-03-22', '2026-03-26', '2026-03-28', 6000000, N'Chờ nhận');

-- 10 Chi tiết phiếu đặt phòng [cite: 52]
INSERT INTO ChiTietPhieuDatPhong (maDatPhong, maPhong, giaThuePhong) VALUES
('DP001', 'P101', 500000),
('DP002', 'P102', 800000),
('DP003', 'P201', 1500000),
('DP004', 'P202', 2000000),
('DP005', 'P301', 550000),
('DP006', 'P401', 2500000),
('DP007', 'P402', 500000),
('DP008', 'P501', 900000),
('DP009', 'P101', 500000),
('DP010', 'P502', 3000000);

-- 10 Hóa đơn Header [cite: 56, 70]
INSERT INTO HoaDonHeader (maHD, maDatPhong, maKhachHang, maNguoiDung, ngayLap, tongTien, thanhToan, thueVAT) VALUES
('HD001', 'DP001', 'KH001', 'NV002', '2026-03-22', 1150000, 1, 0.1),
('HD002', 'DP003', 'KH003', 'NV004', '2026-03-22', 1700000, 1, 0.1),
('HD003', 'DP005', 'KH005', 'NV005', '2026-03-22', 1250000, 0, 0.1),
('HD004', 'DP007', 'KH007', 'NV002', '2026-03-22', 600000, 1, 0.1),
('HD005', 'DP009', 'KH009', 'NV005', '2026-03-22', 1800000, 0, 0.1),
('HD006', 'DP001', 'KH001', 'NV002', '2026-03-22', 50000, 1, 0.1),
('HD007', 'DP003', 'KH003', 'NV004', '2026-03-22', 80000, 1, 0.1),
('HD008', 'DP005', 'KH005', 'NV005', '2026-03-22', 30000, 0, 0.1),
('HD009', 'DP007', 'KH007', 'NV002', '2026-03-22', 150000, 1, 0.1),
('HD010', 'DP009', 'KH009', 'NV005', '2026-03-22', 400000, 1, 0.1);

-- 10 Chi tiết hóa đơn [cite: 53, 100]
INSERT INTO HoaDonDetail (maHD, maDichVu, soLuong, donGia) VALUES
('HD001', 'DV01', 2, 20000),
('HD001', 'DV02', 3, 15000),
('HD002', 'DV03', 1, 50000),
('HD002', 'DV06', 2, 25000),
('HD003', 'DV04', 1, 500000),
('HD004', 'DV05', 1, 100000),
('HD005', 'DV08', 1, 150000),
('HD006', 'DV07', 2, 35000),
('HD007', 'DV10', 3, 30000),
('HD008', 'DV02', 5, 15000),
('HD009', 'DV09', 1, 400000),
('HD010', 'DV03', 2, 50000);