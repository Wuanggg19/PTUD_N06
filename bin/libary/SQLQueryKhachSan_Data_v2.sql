USE QuanLyKhachSan_N06_v5;
GO

-- 1. THÊM DỮ LIỆU BẢNG NHÂN VIÊN (Mật khẩu mẫu: '123456')
INSERT INTO NhanVien (maNhanVien, tenNhanVien, chucVu, luongNhanVien, phaiNhanVien, soDienThoai, tenDangNhap, matKhau, vaiTro) VALUES
('NV001', N'Phan Trần Phúc', N'Quản lý', 20000000, 1, '0901234567', 'admin', '123456', 'Admin'),
('NV002', N'Lê Văn Sỹ', N'Lễ tân', 8000000, 1, '0902234567', 'sy_le', '123456', 'User'),
('NV003', N'Huỳnh Nhật Quang', N'Lễ tân', 8500000, 1, '0903234567', 'quang_h', '123456', 'User'),
('NV004', N'Nguyễn Thị Lan', N'Kế toán', 12000000, 0, '0904234567', 'lan_kt', '123456', 'User'),
('NV005', N'Trần Minh Tâm', N'Lễ tân', 8000000, 1, '0905234567', 'tam_tm', '123456', 'User'),
('NV006', N'Phạm Hồng Nhung', N'Lễ tân', 8000000, 0, '0906234567', 'nhung_ph', '123456', 'User'),
('NV007', N'Bùi Ngọc Thiện', N'Kỹ thuật', 10000000, 1, '0907234567', 'thien_bn', '123456', 'User'),
('NV008', N'Vũ Hoàng Nam', N'Bảo vệ', 7000000, 1, '0908234567', 'nam_vh', '123456', 'User'),
('NV009', N'Đỗ Kim Chi', N'Tạp vụ', 6500000, 0, '0909234567', 'chi_dk', '123456', 'User'),
('NV010', N'Lý Hải Đăng', N'Lễ tân', 8000000, 1, '0910234567', 'dang_lh', '123456', 'User');

-- 2. THÊM DỮ LIỆU BẢNG KHÁCH HÀNG
INSERT INTO KhachHang (maKhachHang, tenKhachHang, diaChi, gioiTinh, soDienThoai) VALUES
('KH001', N'Nguyễn Văn An', N'TP. Hồ Chí Minh', 1, '0981111222'),
('KH002', N'Trần Thị Bình', N'Hà Nội', 0, '0982222333'),
('KH003', N'Lê Hoàng Cường', N'Đà Nẵng', 1, '0983333444'),
('KH004', N'Phạm Minh Đức', N'Cần Thơ', 1, '0984444555'),
('KH005', N'Võ Thị Em', N'Huế', 0, '0985555666'),
('KH006', N'Đặng Văn Phúc', N'Bình Dương', 1, '0986666777'),
('KH007', N'Hoàng Kim Ngân', N'Đồng Nai', 0, '0987777888'),
('KH008', N'Ngô Gia Huy', N'Long An', 1, '0988888999'),
('KH009', N'Trịnh Hoài Nam', N'Vũng Tàu', 1, '0989999000'),
('KH010', N'Sơn Tùng MTP', N'Thái Bình', 1, '0919191919');

-- 3. THÊM DỮ LIỆU BẢNG PHÒNG
INSERT INTO Phong (maPhong, loaiPhong, soGiuong, trangThai, giaPhong) VALUES
('P101', N'Standard', 1, N'Trống', 500000),
('P102', N'Standard', 1, N'Đang ở', 500000),
('P201', N'Deluxe', 1, N'Trống', 800000),
('P202', N'Deluxe', 2, N'Trống', 1200000),
('P301', N'Suite', 1, N'Đang ở', 2000000),
('P302', N'Suite', 2, N'Sửa chữa', 2500000),
('P401', N'VIP', 1, N'Trống', 5000000),
('P103', N'Standard', 2, N'Trống', 700000),
('P203', N'Deluxe', 2, N'Trống', 1200000),
('P303', N'Suite', 1, N'Trống', 2000000);

-- 4. THÊM DỮ LIỆU BẢNG DỊCH VỤ
INSERT INTO DichVu (maDichVu, tenDichVu, donGia, trangThai) VALUES
('DV001', N'Nước suối', 15000, N'Đang kinh doanh'),
('DV002', N'Mì ly', 20000, N'Đang kinh doanh'),
('DV003', N'Coca Cola', 25000, N'Đang kinh doanh'),
('DV004', N'Giặt ủi (kg)', 50000, N'Đang kinh doanh'),
('DV005', N'Thuê xe máy', 150000, N'Đang kinh doanh'),
('DV006', N'Ăn sáng tại phòng', 100000, N'Đang kinh doanh'),
('DV007', N'Massage', 300000, N'Đang kinh doanh'),
('DV008', N'Đưa đón sân bay', 200000, N'Đang kinh doanh'),
('DV009', N'Bia Tiger', 35000, N'Đang kinh doanh'),
('DV010', N'Rượu vang', 1200000, N'Đang kinh doanh');

-- 5. BẢNG GIÁ HEADER
INSERT INTO BangGiaHeader (maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, loaiNgay) VALUES
('BG2026_01', N'Giá thường năm 2026', '2026-01-01', '2026-12-31', N'Ngày thường'),
('BG2026_LE', N'Giá lễ 30/4', '2026-04-28', '2026-05-02', N'Ngày lễ');

-- 6. PHIẾU ĐẶT PHÒNG
INSERT INTO PhieuDatPhong (maDatPhong, ngayDat, trangThai, maNhanVien, maKhachHang) VALUES
('DP0001', '2026-03-20', N'DaNhanPhong', 'NV002', 'KH001'),
('DP0002', '2026-03-21', N'DaNhanPhong', 'NV003', 'KH002'),
('DP0003', '2026-03-22', N'ChoXacNhan', 'NV005', 'KH003'),
('DP0004', '2026-03-22', N'DaHuy', 'NV002', 'KH004'),
('DP0005', '2026-03-23', N'ChoXacNhan', 'NV003', 'KH005'),
('DP0006', '2026-03-23', N'ChoXacNhan', 'NV010', 'KH006'),
('DP0007', '2026-03-24', N'ChoXacNhan', 'NV002', 'KH007'),
('DP0008', '2026-03-24', N'ChoXacNhan', 'NV003', 'KH008'),
('DP0009', '2026-03-25', N'ChoXacNhan', 'NV005', 'KH009'),
('DP0010', '2026-03-25', N'ChoXacNhan', 'NV002', 'KH010');

-- 7. CHI TIẾT PHIẾU ĐẶT
INSERT INTO ChiTietPhieuDat (maDatPhong, maPhong, giaThuePhong, ngayNhan, ngayTra) VALUES
('DP0001', 'P102', 500000, '2026-03-20 14:00:00', '2026-03-22 12:00:00'),
('DP0002', 'P301', 2000000, '2026-03-21 14:00:00', '2026-03-23 12:00:00'),
('DP0003', 'P201', 800000, '2026-04-01 14:00:00', '2026-04-03 12:00:00'),
('DP0004', 'P101', 500000, '2026-03-25 14:00:00', '2026-03-26 12:00:00'),
('DP0005', 'P401', 5000000, '2026-03-28 14:00:00', '2026-03-30 12:00:00'),
('DP0006', 'P202', 1200000, '2026-03-29 14:00:00', '2026-03-31 12:00:00'),
('DP0007', 'P103', 700000, '2026-04-05 14:00:00', '2026-04-07 12:00:00'),
('DP0008', 'P303', 2000000, '2026-04-10 14:00:00', '2026-04-12 12:00:00'),
('DP0009', 'P203', 1200000, '2026-04-15 14:00:00', '2026-04-16 12:00:00'),
('DP0010', 'P101', 500000, '2026-04-20 14:00:00', '2026-04-22 12:00:00');

-- 8. HÓA ĐƠN (Thanh toán cho 2 phiếu đầu)
INSERT INTO HoaDon (maHoaDon, ngayLap, thue, tongTienPhong, tongTienDichVu, maDatPhong, maNhanVien) VALUES
('HD00001', '2026-03-22 12:30:00', 0.08, 1000000, 45000, 'DP0001', 'NV002'),
('HD00002', '2026-03-23 11:45:00', 0.08, 4000000, 150000, 'DP0002', 'NV003');

-- 9. CHI TIẾT HÓA ĐƠN
INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, soLuong, donGiaLuuTru) VALUES
('HD00001', 'DV001', 3, 15000),
('HD00002', 'DV005', 1, 150000);