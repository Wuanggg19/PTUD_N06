-- 1. Tạo Database
CREATE DATABASE QuanLyKhachSan_Nhom06_v4;
GO
USE QuanLyKhachSan_Nhom06_v4;
GO

-- 2. NHÓM BẢNG DANH MỤC (Tạo trước vì không tham chiếu ai)
CREATE TABLE NguoiDung (
    maNguoiDung VARCHAR(10) PRIMARY KEY,
    tenDangNhap VARCHAR(30) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    hoTen NVARCHAR(50),
    soDienThoai VARCHAR(15),
    vaiTro NVARCHAR(20),
    chucVu NVARCHAR(20)
);

CREATE TABLE KhachHang (
    maKhachHang VARCHAR(10) PRIMARY KEY,
    tenKhachHang NVARCHAR(50) NOT NULL,
    diaChi NVARCHAR(100),
    gioiTinh BIT, -- True: Nam, False: Nữ
    soDienThoai VARCHAR(15)
);

CREATE TABLE Phong (
    maPhong VARCHAR(10) PRIMARY KEY,
    loaiPhong NVARCHAR(30),
    soGiuong INT,
    giaPhong FLOAT,
    trangThai NVARCHAR(20) -- Trống, Đang ở, Đã đặt [cite: 22, 27]
);

CREATE TABLE DichVu (
    maDichVu VARCHAR(10) PRIMARY KEY,
    tenDichVu NVARCHAR(50),
    donGia FLOAT,
    trangThai NVARCHAR(20)
);

-- 3. NHÓM BẢNG NGHIỆP VỤ (Tạo sau vì có khóa ngoại tham chiếu nhóm trên)

-- Phiếu đặt phòng (Header)
CREATE TABLE PhieuDatPhong (
    maDatPhong VARCHAR(10) PRIMARY KEY,
    maKhachHang VARCHAR(10) FOREIGN KEY REFERENCES KhachHang(maKhachHang),
    maNguoiDung VARCHAR(10) FOREIGN KEY REFERENCES NguoiDung(maNguoiDung),
    ngayDat DATETIME DEFAULT GETDATE(),
    ngayNhan DATETIME, 
    ngayTra DATETIME,   
    giaBD FLOAT, -- Giá ban đầu theo yêu cầu của bạn
    trangThai NVARCHAR(20)
);

-- Chi tiết phiếu đặt phòng (Nối PhieuDatPhong và Phong)
CREATE TABLE ChiTietPhieuDatPhong (
    maDatPhong VARCHAR(10) FOREIGN KEY REFERENCES PhieuDatPhong(maDatPhong),
    maPhong VARCHAR(10) FOREIGN KEY REFERENCES Phong(maPhong),
    giaThuePhong FLOAT,
    PRIMARY KEY (maDatPhong, maPhong)
);

-- Hóa đơn (Header)
CREATE TABLE HoaDonHeader (
    maHD VARCHAR(10) PRIMARY KEY,
    maDatPhong VARCHAR(10) FOREIGN KEY REFERENCES PhieuDatPhong(maDatPhong),
    maKhachHang VARCHAR(10) FOREIGN KEY REFERENCES KhachHang(maKhachHang),
    maNguoiDung VARCHAR(10) FOREIGN KEY REFERENCES NguoiDung(maNguoiDung),
    ngayLap DATETIME DEFAULT GETDATE(),
    tongTien FLOAT,
    thanhToan BIT DEFAULT 0, -- 0: Chưa thanh toán, 1: Đã thanh toán [cite: 56, 73]
    thueVAT FLOAT DEFAULT 0.1
);

-- Chi tiết hóa đơn (Detail)
CREATE TABLE HoaDonDetail (
    maHD VARCHAR(10) FOREIGN KEY REFERENCES HoaDonHeader(maHD),
    maDichVu VARCHAR(10) FOREIGN KEY REFERENCES DichVu(maDichVu),
    soLuong INT CHECK (soLuong > 0),
    donGia FLOAT,
    thanhTien AS (soLuong * donGia), -- Cột tự động tính toán để tránh sai sót 
    PRIMARY KEY (maHD, maDichVu)
);
GO