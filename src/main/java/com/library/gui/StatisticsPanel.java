package com.library.gui;

import com.library.client.LibraryClient;
import com.library.common.Message;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

public class StatisticsPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final LibraryClient client;
    
    // Statistics cards
    private JLabel totalBooksLabel;
    private JLabel totalCopiesLabel;
    private JLabel availableCopiesLabel;
    private JLabel borrowedBooksLabel;
    private JLabel returnedBooksLabel;
    private JLabel overdueBooksLabel;
    
    // Progress bars
    private JProgressBar borrowedProgressBar;
    private JProgressBar availableProgressBar;
    
    public StatisticsPanel(LibraryClient client) {
        this.client = client;
        initializeUI();
        refreshData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create main panel with grid layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Create statistics cards
        createStatisticsCards(mainPanel, gbc);
        
        // Create charts/progress bars
        createChartsPanel(mainPanel, gbc);
        
        // Create refresh button
        createRefreshPanel(mainPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void createStatisticsCards(JPanel parent, GridBagConstraints gbc) {
        // First row - Book statistics
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        parent.add(createStatCard("📚 Tổng số đầu sách", "0", new Color(33, 150, 243), 
                                 label -> totalBooksLabel = label), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        parent.add(createStatCard("📖 Tổng số bản sách", "0", new Color(76, 175, 80),
                                 label -> totalCopiesLabel = label), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        parent.add(createStatCard("✅ Sách có sẵn", "0", new Color(139, 195, 74),
                                 label -> availableCopiesLabel = label), gbc);
        
        // Second row - Borrow statistics
        gbc.gridx = 0; gbc.gridy = 1;
        parent.add(createStatCard("📋 Đang mượn", "0", new Color(255, 152, 0),
                                 label -> borrowedBooksLabel = label), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        parent.add(createStatCard("↩️ Đã trả", "0", new Color(96, 125, 139),
                                 label -> returnedBooksLabel = label), gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        parent.add(createStatCard("⚠️ Quá hạn", "0", new Color(244, 67, 54),
                                 label -> overdueBooksLabel = label), gbc);
    }
    
    private JPanel createStatCard(String title, String value, Color color, 
                                 java.util.function.Consumer<JLabel> labelConsumer) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(200, 100));
        
        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        // Value
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(color);
        
        // Store reference to value label
        labelConsumer.accept(valueLabel);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void createChartsPanel(JPanel parent, GridBagConstraints gbc) {
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBorder(new TitledBorder("Biểu đồ thống kê"));
        chartsPanel.setPreferredSize(new Dimension(0, 200));
        
        // Available vs Borrowed chart
        JPanel availabilityPanel = new JPanel(new GridBagLayout());
        availabilityPanel.setBorder(new TitledBorder("Tình trạng sách"));
        
        GridBagConstraints chartGbc = new GridBagConstraints();
        chartGbc.insets = new Insets(5, 5, 5, 5);
        
        // Available books progress
        chartGbc.gridx = 0; chartGbc.gridy = 0; chartGbc.anchor = GridBagConstraints.WEST;
        availabilityPanel.add(new JLabel("Có sẵn:"), chartGbc);
        
        availableProgressBar = new JProgressBar(0, 100);
        availableProgressBar.setStringPainted(true);
        availableProgressBar.setForeground(new Color(76, 175, 80));
        chartGbc.gridx = 1; chartGbc.gridy = 0; chartGbc.fill = GridBagConstraints.HORIZONTAL; chartGbc.weightx = 1.0;
        availabilityPanel.add(availableProgressBar, chartGbc);
        
        // Borrowed books progress
        chartGbc.gridx = 0; chartGbc.gridy = 1; chartGbc.fill = GridBagConstraints.NONE; chartGbc.weightx = 0;
        availabilityPanel.add(new JLabel("Đang mượn:"), chartGbc);
        
        borrowedProgressBar = new JProgressBar(0, 100);
        borrowedProgressBar.setStringPainted(true);
        borrowedProgressBar.setForeground(new Color(255, 152, 0));
        chartGbc.gridx = 1; chartGbc.gridy = 1; chartGbc.fill = GridBagConstraints.HORIZONTAL; chartGbc.weightx = 1.0;
        availabilityPanel.add(borrowedProgressBar, chartGbc);
        
        chartsPanel.add(availabilityPanel);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(new TitledBorder("Tóm tắt hệ thống"));
        
        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setBackground(summaryPanel.getBackground());
        summaryArea.setFont(new Font("Arial", Font.PLAIN, 12));
        summaryArea.setText("""
            Hệ thống quản lý sách thư viện
            
            Chức năng:
            • Quản lý thông tin sách
            • Theo dõi mượn/trả sách
            • Thống kê và báo cáo
            • Cảnh báo sách quá hạn
            
            Trạng thái: Hoạt động bình thường
            """);
        
        chartGbc.gridx = 0; chartGbc.gridy = 0; chartGbc.fill = GridBagConstraints.BOTH;
        chartGbc.weightx = 1.0; chartGbc.weighty = 1.0;
        summaryPanel.add(summaryArea, chartGbc);
        
        chartsPanel.add(summaryPanel);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        parent.add(chartsPanel, gbc);
    }
    
    private void createRefreshPanel(JPanel parent, GridBagConstraints gbc) {
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton refreshButton = new JButton("🔄 Cập nhật thống kê");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setBackground(new Color(33, 150, 243));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshData());
        
        refreshPanel.add(refreshButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.weighty = 0;
        parent.add(refreshPanel, gbc);
    }
    
    @Override
    public void refreshData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.GET_STATISTICS, null);
                Message response = client.sendRequest(request);
                  if (response.isSuccess()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> statisticsMap = (Map<String, Object>) response.getData();
                    return statisticsMap;
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> statistics = get();
                    updateStatistics(statistics);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        StatisticsPanel.this,
                        "Lỗi tải thống kê: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateStatistics(Map<String, Object> statistics) {
        // Extract values
        int totalBooks = ((Number) statistics.get("totalBooks")).intValue();
        int totalCopies = ((Number) statistics.get("totalCopies")).intValue();
        int availableCopies = ((Number) statistics.get("availableCopies")).intValue();
        long borrowedBooks = ((Number) statistics.get("borrowedBooks")).longValue();
        long returnedBooks = ((Number) statistics.get("returnedBooks")).longValue();
        long overdueBooks = ((Number) statistics.get("overdueBooks")).longValue();
        
        // Update labels
        totalBooksLabel.setText(String.valueOf(totalBooks));
        totalCopiesLabel.setText(String.valueOf(totalCopies));
        availableCopiesLabel.setText(String.valueOf(availableCopies));
        borrowedBooksLabel.setText(String.valueOf(borrowedBooks));
        returnedBooksLabel.setText(String.valueOf(returnedBooks));
        overdueBooksLabel.setText(String.valueOf(overdueBooks));
        
        // Update progress bars
        if (totalCopies > 0) {
            int availablePercentage = (int) ((availableCopies * 100.0) / totalCopies);
            int borrowedPercentage = (int) ((borrowedBooks * 100.0) / totalCopies);
            
            availableProgressBar.setValue(availablePercentage);
            availableProgressBar.setString(availablePercentage + "% (" + availableCopies + "/" + totalCopies + ")");
            
            borrowedProgressBar.setValue(borrowedPercentage);
            borrowedProgressBar.setString(borrowedPercentage + "% (" + borrowedBooks + "/" + totalCopies + ")");
        } else {
            availableProgressBar.setValue(0);
            availableProgressBar.setString("0%");
            borrowedProgressBar.setValue(0);
            borrowedProgressBar.setString("0%");
        }
        
        // Change color for overdue books if any
        if (overdueBooks > 0) {
            overdueBooksLabel.setForeground(new Color(244, 67, 54));
        } else {
            overdueBooksLabel.setForeground(new Color(76, 175, 80));
        }
    }
}
