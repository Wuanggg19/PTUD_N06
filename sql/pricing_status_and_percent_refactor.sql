/*
Migration: pricing_status_and_percent_refactor.sql
Target: SQL Server
*/

BEGIN TRY
    BEGIN TRAN;

    IF COL_LENGTH('BangGiaHeader', 'trangThai') IS NULL
    BEGIN
        ALTER TABLE BangGiaHeader
        ADD trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_BangGiaHeader_trangThai DEFAULT N'Đang hoạt động';
    END

    IF COL_LENGTH('BangGiaHeader', 'phanTramTang') IS NULL
    BEGIN
        ALTER TABLE BangGiaHeader
        ADD phanTramTang DECIMAL(5,2) NOT NULL CONSTRAINT DF_BangGiaHeader_phanTramTang DEFAULT (0);
    END

    UPDATE BangGiaHeader
    SET loaiNgay = CASE
        WHEN UPPER(LTRIM(RTRIM(loaiNgay))) IN (N'NGÀY THƯỜNG', N'NGAY THUONG', N'NGAY_THUONG', N'THUONG') THEN N'NGAY_THUONG'
        WHEN UPPER(LTRIM(RTRIM(loaiNgay))) IN (N'CUỐI TUẦN', N'CUOI TUAN', N'CUOI_TUAN', N'WEEKEND') THEN N'CUOI_TUAN'
        WHEN UPPER(LTRIM(RTRIM(loaiNgay))) IN (N'LỄ', N'LE', N'NGÀY LỄ', N'NGAY LE', N'NGAY_LE', N'TẾT', N'TET') THEN N'NGAY_LE'
        ELSE N'NGAY_THUONG'
    END;

    UPDATE BangGiaHeader
    SET trangThai = CASE
        WHEN trangThai IS NULL OR LTRIM(RTRIM(trangThai)) = N'' THEN N'Đang hoạt động'
        WHEN trangThai = N'Không hoạt động' THEN N'Không hoạt động'
        ELSE N'Đang hoạt động'
    END;

    IF OBJECT_ID('CK_BangGiaHeader_LoaiNgay', 'C') IS NULL
    BEGIN
        ALTER TABLE BangGiaHeader
        ADD CONSTRAINT CK_BangGiaHeader_LoaiNgay CHECK (loaiNgay IN (N'NGAY_THUONG', N'CUOI_TUAN', N'NGAY_LE'));
    END

    IF OBJECT_ID('CK_BangGiaHeader_TrangThai', 'C') IS NULL
    BEGIN
        ALTER TABLE BangGiaHeader
        ADD CONSTRAINT CK_BangGiaHeader_TrangThai CHECK (trangThai IN (N'Đang hoạt động', N'Không hoạt động'));
    END

    IF OBJECT_ID('CK_BangGiaHeader_PhanTramTang', 'C') IS NULL
    BEGIN
        ALTER TABLE BangGiaHeader
        ADD CONSTRAINT CK_BangGiaHeader_PhanTramTang CHECK (phanTramTang >= 0);
    END

    IF NOT EXISTS (SELECT 1 FROM BangGiaHeader WHERE maBangGia = 'BG_NGAY_THUONG_DEFAULT')
    BEGIN
        INSERT INTO BangGiaHeader(maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, loaiNgay, trangThai, phanTramTang)
        VALUES ('BG_NGAY_THUONG_DEFAULT', N'Bảng giá ngày thường mặc định', '2000-01-01', '2099-12-31', N'NGAY_THUONG', N'Đang hoạt động', 0);
    END

    IF NOT EXISTS (SELECT 1 FROM BangGiaHeader WHERE maBangGia = 'BG_CUOI_TUAN_DEFAULT')
    BEGIN
        INSERT INTO BangGiaHeader(maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, loaiNgay, trangThai, phanTramTang)
        VALUES ('BG_CUOI_TUAN_DEFAULT', N'Bảng giá cuối tuần mặc định', '2000-01-01', '2099-12-31', N'CUOI_TUAN', N'Đang hoạt động', 0);
    END

    IF OBJECT_ID('tempdb..#BasePriceByType') IS NOT NULL DROP TABLE #BasePriceByType;
    SELECT loaiPhong, CAST(AVG(giaPhong) AS DECIMAL(18,2)) AS giaCoSo
    INTO #BasePriceByType
    FROM Phong
    GROUP BY loaiPhong;

    INSERT INTO ChiTietBangGia(maBangGia, loaiPhong, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
    SELECT 'BG_NGAY_THUONG_DEFAULT', b.loaiPhong, b.giaCoSo, b.giaCoSo, b.giaCoSo, b.giaCoSo, b.giaCoSo, 0
    FROM #BasePriceByType b
    WHERE NOT EXISTS (
        SELECT 1 FROM ChiTietBangGia c
        WHERE c.maBangGia = 'BG_NGAY_THUONG_DEFAULT' AND c.loaiPhong = b.loaiPhong
    );

    INSERT INTO ChiTietBangGia(maBangGia, loaiPhong, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
    SELECT 'BG_CUOI_TUAN_DEFAULT', b.loaiPhong, b.giaCoSo, b.giaCoSo, b.giaCoSo, b.giaCoSo, b.giaCoSo, 0
    FROM #BasePriceByType b
    WHERE NOT EXISTS (
        SELECT 1 FROM ChiTietBangGia c
        WHERE c.maBangGia = 'BG_CUOI_TUAN_DEFAULT' AND c.loaiPhong = b.loaiPhong
    );

    COMMIT TRAN;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRAN;
    THROW;
END CATCH;
