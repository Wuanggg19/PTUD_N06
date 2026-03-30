package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import dao.PhongDAO;
import dao.HoaDonDAO;
import dao.PhieuDatPhongDAO;
import entity.Phong;
import entity.HoaDon;
import entity.PhieuDatPhong;

public class DashboardPanel extends JPanel {

    private final Color COLOR_TOTAL = new Color(52, 152, 219);  // Blue
    private final Color COLOR_OCCUPIED = new Color(231, 76, 60); // Red
    private final Color COLOR_VACANT = new Color(46, 204, 113);   // Green
    private final Color COLOR_REVENUE = new Color(155, 89, 182); // Purple

    private PhongDAO phongDAO = new PhongDAO();
    private HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private PhieuDatPhongDAO phieuDatPhongDAO = new PhieuDatPhongDAO();
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 30));
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. Tiêu đề
        JLabel lblTitle = new JLabel("TỔNG QUAN HỆ THỐNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // 2. Panel chính chứa 2 phần: Thẻ thống kê (Trên) và Danh sách (Dưới)
        JPanel pnlMain = new JPanel(new BorderLayout(0, 30));
        pnlMain.setOpaque(false);

        // --- Phần trên: Thẻ thống kê ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 25, 0));
        pnlCards.setOpaque(false);
        loadStatistics(pnlCards);
        pnlMain.add(pnlCards, BorderLayout.NORTH);

        // --- Phần dưới: 2 Bảng danh sách ---
        JPanel pnlLists = new JPanel(new GridLayout(1, 2, 25, 0));
        pnlLists.setOpaque(false);

        pnlLists.add(createTablePanel("PHÒNG VỪA ĐẶT (GẦN ĐÂY)", createRecentBookingTable()));
        pnlLists.add(createTablePanel("DANH SÁCH PHÒNG ĐANG TRỐNG", createVacantRoomTable()));

        pnlMain.add(pnlLists, BorderLayout.CENTER);

        add(pnlMain, BorderLayout.CENTER);
    }

    private void loadStatistics(JPanel container) {
        try {
            List<Phong> dsPhong = phongDAO.getAllPhong();
            int total = dsPhong.size();
            long occupied = dsPhong.stream().filter(p -> p.getTrangThai() != null && p.getTrangThai().equals("Đang ở")).count();
            long vacant = dsPhong.stream().filter(p -> p.getTrangThai() != null && p.getTrangThai().equals("Trống")).count();

            List<HoaDon> dsHoaDon = hoaDonDAO.getAllHoaDon();
            double todayRevenue = dsHoaDon.stream()
                    .filter(hd -> hd.getNgayLap() != null && hd.getNgayLap().toLocalDate().equals(LocalDate.now()))
                    .mapToDouble(hd -> hd.getTongTienPhong() + hd.getTongTienDichVu())
                    .sum();

            container.add(new StatCard("TỔNG SỐ PHÒNG", String.valueOf(total), "Phòng khách sạn", COLOR_TOTAL));
            container.add(new StatCard("ĐANG CÓ KHÁCH", String.valueOf(occupied), "Phòng đang ở", COLOR_OCCUPIED));
            container.add(new StatCard("PHÒNG TRỐNG", String.valueOf(vacant), "Sẵn sàng đón khách", COLOR_VACANT));
            container.add(new StatCard("DOANH THU HÔM NAY", df.format(todayRevenue), "Tổng tiền thu về", COLOR_REVENUE));
        } catch (Exception e) {
            container.add(new StatCard("LỖI DỮ LIỆU", "0", "Vui lòng kiểm tra kết nối", Color.GRAY));
        }
    }

    private JPanel createTablePanel(String title, JTable table) {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);
        
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(52, 73, 94));
        pnl.add(lbl, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JTable createRecentBookingTable() {
        String[] columns = {"Mã Phiếu", "Khách Hàng", "Ngày Đặt", "Trạng Thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        try {
            List<PhieuDatPhong> ds = phieuDatPhongDAO.getAllPhieuDatPhong();
            // Lấy 10 phiếu mới nhất
            ds.stream()
              .sorted((p1, p2) -> p2.getNgayDat().compareTo(p1.getNgayDat()))
              .limit(10)
              .forEach(p -> {
                  model.addRow(new Object[]{
                      p.getMaDatPhong(),
                      p.getKhachHang().getMaKhachHang(),
                      p.getNgayDat().toLocalDate(),
                      p.getTrangThai()
                  });
              });
        } catch (Exception e) {}

        return setupModernTable(model);
    }

    private JTable createVacantRoomTable() {
        String[] columns = {"Mã Phòng", "Loại Phòng", "Số Giường", "Giá Phòng"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try {
            List<Phong> ds = phongDAO.filterPhong("Trống", null);
            ds.forEach(p -> {
                model.addRow(new Object[]{
                    p.getMaPhong(),
                    p.getLoaiPhong(),
                    p.getSoGiuong(),
                    df.format(p.getGiaPhong())
                });
            });
        } catch (Exception e) {}

        return setupModernTable(model);
    }

    private JTable setupModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setGridColor(new Color(245, 245, 245));
        table.setSelectionBackground(new Color(52, 152, 219, 50));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        return table;
    }

    private class StatCard extends JPanel {
        private String title, value, subtitle;
        private Color color;

        public StatCard(String title, String value, String subtitle, Color color) {
            this.title = title;
            this.value = value;
            this.subtitle = subtitle;
            this.color = color;
            setPreferredSize(new Dimension(250, 160));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Double(0, 0, 8, getHeight(), 0, 0));
            g2.setColor(new Color(127, 140, 141));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(title, 25, 40);
            g2.setColor(new Color(44, 62, 80));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString(value, 25, 80);
            g2.setColor(new Color(189, 195, 199));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            g2.drawString(subtitle, 25, 110);
        }
    }
}
