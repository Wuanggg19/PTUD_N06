-- 1. Tạo Database mới
CREATE DATABASE QuanLyKhachSan_N06_v5;
GO
USE QuanLyKhachSan_N06_v5;
GO

-- 2. Tạo các bảng danh mục (Independent Tables)
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(30) PRIMARY KEY,
    tenNhanVien NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50),
    luongNhanVien DECIMAL(18, 2) CHECK (luongNhanVien >= 0),
    phaiNhanVien BIT, -- 1: Nam, 0: Nữ
    soDienThoai VARCHAR(15),
    tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    vaiTro NVARCHAR(20) -- Admin, LeTan
);

CREATE TABLE KhachHang (
    maKhachHang VARCHAR(30) PRIMARY KEY,
    tenKhachHang NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255),
    gioiTinh BIT,
    soDienThoai VARCHAR(15) NOT NULL
);

CREATE TABLE Phong (
    maPhong VARCHAR(10) PRIMARY KEY,
    loaiPhong NVARCHAR(50),
    soGiuong INT CHECK (soGiuong > 0),
    trangThai NVARCHAR(30) DEFAULT N'Trống', -- Trống, Đang ở, Đang sửa chữa
    giaPhong DECIMAL(18, 2) CHECK (giaPhong >= 0)
);

CREATE TABLE DichVu (
    maDichVu VARCHAR(30) PRIMARY KEY,
    tenDichVu NVARCHAR(100) NOT NULL,
    donGia DECIMAL(18, 2) CHECK (donGia >= 0),
    trangThai NVARCHAR(30) DEFAULT N'Đang kinh doanh'
);

-- 3. Nhóm bảng quản lý giá biến động
CREATE TABLE BangGiaHeader (
    maBangGia VARCHAR(30) PRIMARY KEY,
    tenBangGia NVARCHAR(100),
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    loaiNgay NVARCHAR(30), -- NgayThuong, CuoiTuan, Le
    CONSTRAINT CHK_ThoiGianBangGia CHECK (ngayKetThuc >= ngayBatDau)
);

CREATE TABLE BangGiaDetail (
    maBangGia VARCHAR(30),
    maPhong VARCHAR(10),
    giaPhongMoi DECIMAL(18, 2) CHECK (giaPhongMoi >= 0),
    PRIMARY KEY (maBangGia, maPhong),
    FOREIGN KEY (maBangGia) REFERENCES BangGiaHeader(maBangGia),
    FOREIGN KEY (maPhong) REFERENCES Phong(maPhong)
);

-- 4. Nhóm nghiệp vụ Đặt phòng và Hóa đơn
CREATE TABLE PhieuDatPhong (
    maDatPhong VARCHAR(30) PRIMARY KEY,
    ngayDat DATETIME2 DEFAULT GETDATE(),
    trangThai NVARCHAR(30), -- ChoXacNhan, DaNhanPhong, DaHuy
    maNhanVien VARCHAR(30) FOREIGN KEY REFERENCES NhanVien(maNhanVien),
    maKhachHang VARCHAR(30) FOREIGN KEY REFERENCES KhachHang(maKhachHang)
);

CREATE TABLE ChiTietPhieuDat (
    maDatPhong VARCHAR(30),
    maPhong VARCHAR(10),
    giaThuePhong DECIMAL(18, 2) CHECK (giaThuePhong >= 0),
    ngayNhan DATETIME2 NOT NULL,
    ngayTra DATETIME2 NOT NULL,
    PRIMARY KEY (maDatPhong, maPhong),
    FOREIGN KEY (maDatPhong) REFERENCES PhieuDatPhong(maDatPhong),
    FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT CHK_NgayThue CHECK (ngayTra >= ngayNhan)
);

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(30) PRIMARY KEY,
    ngayLap DATETIME2 DEFAULT GETDATE(),
    thue DECIMAL(5, 2) DEFAULT 0.08, -- 8% VAT
    tongTienPhong DECIMAL(18, 2) DEFAULT 0,
    tongTienDichVu DECIMAL(18, 2) DEFAULT 0,
    maDatPhong VARCHAR(30) UNIQUE, -- Một phiếu đặt chỉ xuất 1 hóa đơn
    maNhanVien VARCHAR(30) FOREIGN KEY REFERENCES NhanVien(maNhanVien),
    FOREIGN KEY (maDatPhong) REFERENCES PhieuDatPhong(maDatPhong)
);

CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(30),
    maDichVu VARCHAR(30),
    soLuong INT CHECK (soLuong > 0),
    donGiaLuuTru DECIMAL(18, 2) CHECK (donGiaLuuTru >= 0),
    PRIMARY KEY (maHoaDon, maDichVu),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
);