package com.smartcampus;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MainFrame extends JFrame {
    private DatabaseManager dbManager;

    public MainFrame() {
        dbManager = new DatabaseManager();

        setTitle("Smart Campus Resource Allocation System");
        setSize(1024, 768);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("View Bookings", createViewBookingsPanel());
        tabbedPane.addTab("Book Resource", createBookResourcePanel());
        tabbedPane.addTab("Add User", createUserPanel());
        tabbedPane.addTab("Add Resource", createResourcePanel());
        tabbedPane.addTab("Add Department", createDepartmentPanel());
        tabbedPane.addTab("Approve Bookings", createApproveBookingsPanel());
        tabbedPane.addTab("Add Usage Log", createUsageLogPanel());

        final int[] previousTab = {0};
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String title = tabbedPane.getTitleAt(selectedIndex);
            if ("Approve Bookings".equals(title)) {
                JPasswordField pwd = new JPasswordField(10);
                int action = JOptionPane.showConfirmDialog(this, pwd, "Enter Admin Password", JOptionPane.OK_CANCEL_OPTION);
                if (action == JOptionPane.OK_OPTION) {
                    String password = new String(pwd.getPassword());
                    if ("root".equals(password)) {
                        previousTab[0] = selectedIndex;
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect Password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                        tabbedPane.setSelectedIndex(previousTab[0]);
                    }
                } else {
                    tabbedPane.setSelectedIndex(previousTab[0]);
                }
            } else {
                previousTab[0] = selectedIndex;
            }
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton themeBtn = new JButton("🌓 Toggle Theme");
        themeBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        themeBtn.setFocusPainted(false);
        themeBtn.addActionListener(e -> toggleTheme());
        topBar.add(themeBtn);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private boolean isDarkTheme = false;

    private void toggleTheme() {
        try {
            if (isDarkTheme) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                isDarkTheme = false;
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                isDarkTheme = true;
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel createViewBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    int statusColumnIndex = -1;
                    for (int i = 0; i < getModel().getColumnCount(); i++) {
                        if ("Status".equals(getModel().getColumnName(i))) {
                            statusColumnIndex = i;
                            break;
                        }
                    }
                    if (statusColumnIndex != -1) {
                        Object statusValue = getModel().getValueAt(convertRowIndexToModel(row), statusColumnIndex);
                        String status = statusValue != null ? statusValue.toString() : "";
                        if ("Approved".equalsIgnoreCase(status)) {
                            c.setBackground(isDarkTheme ? new Color(40, 100, 40) : new Color(200, 255, 200));
                            c.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            c.setBackground(isDarkTheme ? new Color(100, 40, 40) : new Color(255, 200, 200));
                            c.setForeground(isDarkTheme ? Color.WHITE : Color.BLACK);
                        } else {
                            c.setBackground(getBackground());
                            c.setForeground(getForeground());
                        }
                    } else {
                        c.setBackground(getBackground());
                        c.setForeground(getForeground());
                    }
                } else {
                    c.setBackground(getSelectionBackground());
                    c.setForeground(getSelectionForeground());
                }
                return c;
            }
        };
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(15, 15));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        JButton refreshButton = new JButton("Refresh Bookings");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        bottomPanel.add(refreshButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        JLabel titleLabel = new JLabel("All Bookings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        refreshButton.addActionListener(e -> loadBookings(model));


        loadBookings(model);

        return panel;
    }

    private void loadBookings(DefaultTableModel model) {
        String sql = "SELECT b.Booking_ID, b.User_ID, b.Booking_Date, b.Start_Time, b.End_Time, b.Status, u.Name as User_Name, r.Resource_Name "
                +
                "FROM Booking b " +
                "JOIN User u ON b.User_ID = u.User_ID " +
                "JOIN Resource r ON b.Resource_ID = r.Resource_ID";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Booking ID");
            columnNames.add("User ID");
            columnNames.add("Date");
            columnNames.add("Start Time");
            columnNames.add("End Time");
            columnNames.add("Status");
            columnNames.add("User");
            columnNames.add("Resource");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getObject("Booking_ID") == null ? "NULL" : rs.getObject("Booking_ID"));
                row.add(rs.getObject("User_ID") == null ? "NULL" : rs.getObject("User_ID"));
                row.add(rs.getObject("Booking_Date") == null ? "NULL" : rs.getObject("Booking_Date"));
                row.add(rs.getObject("Start_Time") == null ? "NULL" : rs.getObject("Start_Time"));
                row.add(rs.getObject("End_Time") == null ? "NULL" : rs.getObject("End_Time"));
                row.add(rs.getObject("Status") == null ? "NULL" : rs.getObject("Status"));
                row.add(rs.getObject("User_Name") == null ? "NULL" : rs.getObject("User_Name"));
                row.add(rs.getObject("Resource_Name") == null ? "NULL" : rs.getObject("Resource_Name"));
                data.add(row);
            }

            model.setDataVector(data, columnNames);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createBookResourcePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField dateField = new JTextField(15);
        JTextField startField = new JTextField(15);
        JTextField endField = new JTextField(15);
        JTextField purposeField = new JTextField(15);
        JTextField userIdField = new JTextField(15);
        JComboBox<String> resourceCombo = new JComboBox<>();
        JButton findBtn = new JButton("Find Available Resources");

        int row = 0;
        addFormField(panel, "Date (YYYY-MM-DD):", dateField, gbc, row++);
        addFormField(panel, "Start Time (HH:MM:SS):", startField, gbc, row++);
        addFormField(panel, "End Time (HH:MM:SS):", endField, gbc, row++);

        gbc.gridx = 1;
        gbc.gridy = row++;
        panel.add(findBtn, gbc);

        addFormField(panel, "Available Resource:", resourceCombo, gbc, row++);
        addFormField(panel, "Purpose:", purposeField, gbc, row++);
        addFormField(panel, "User ID:", userIdField, gbc, row++);

        findBtn.addActionListener(e -> {
            resourceCombo.removeAllItems();
            String sql = "SELECT Resource_ID, Resource_Name FROM Resource WHERE Status = 'Available' AND Resource_ID NOT IN "
                    +
                    "(SELECT Resource_ID FROM Booking WHERE Booking_Date = ? AND Status != 'Rejected' AND " +
                    "(Start_Time < ? AND End_Time > ?))";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(dateField.getText().trim()));
                pstmt.setTime(2, Time.valueOf(endField.getText().trim()));
                pstmt.setTime(3, Time.valueOf(startField.getText().trim()));
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    resourceCombo.addItem(rs.getInt("Resource_ID") + " - " + rs.getString("Resource_Name"));
                }
                if (resourceCombo.getItemCount() == 0) {
                    JOptionPane.showMessageDialog(panel, "No resources available for the selected time.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error finding resources: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton submitButton = new JButton("Book Resource");
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            int randomId = 100 + new java.util.Random().nextInt(900);
            String sql = "INSERT INTO Booking (Booking_ID, Booking_Date, Start_Time, End_Time, Purpose, Status, User_ID, Resource_ID) VALUES (?, ?, ?, ?, ?, 'Pending', ?, ?)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, randomId);
                pstmt.setDate(2, Date.valueOf(dateField.getText().trim()));
                pstmt.setTime(3, Time.valueOf(startField.getText().trim()));
                pstmt.setTime(4, Time.valueOf(endField.getText().trim()));
                pstmt.setString(5, purposeField.getText().trim());
                pstmt.setInt(6, Integer.parseInt(userIdField.getText().trim()));

                String selectedRes = (String) resourceCombo.getSelectedItem();
                if (selectedRes == null) {
                    JOptionPane.showMessageDialog(panel, "Please select an available resource first!");
                    return;
                }
                int resId = Integer.parseInt(selectedRes.split(" - ")[0]);
                pstmt.setInt(7, resId);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Booking created successfully! Your Booking ID is: " + randomId);


                dateField.setText("");
                startField.setText("");
                endField.setText("");
                purposeField.setText("");
                userIdField.setText("");
                resourceCombo.removeAllItems();
            } catch (Exception ex) {
                String errorMsg = ex.getMessage();
                if (errorMsg != null && errorMsg.contains("foreign key constraint fails") && errorMsg.contains("`User_ID`")) {
                    JOptionPane.showMessageDialog(this, "Error: User ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        return wrapWithTitle(panel, "Book a Resource");
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[] { "Student", "Faculty", "Admin" });
        JTextField deptIdField = new JTextField(15);

        int row = 0;
        addFormField(panel, "User ID:", idField, gbc, row++);
        addFormField(panel, "Name:", nameField, gbc, row++);
        addFormField(panel, "Email:", emailField, gbc, row++);

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);
        row++;

        addFormField(panel, "Department ID:", deptIdField, gbc, row++);

        JButton submitButton = new JButton("Add User");
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String sql = "INSERT INTO User (User_ID, Name, Email, Role, Department_ID) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(idField.getText().trim()));
                pstmt.setString(2, nameField.getText().trim());
                pstmt.setString(3, emailField.getText().trim());
                pstmt.setString(4, roleCombo.getSelectedItem().toString());
                pstmt.setInt(5, Integer.parseInt(deptIdField.getText().trim()));

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "User added successfully!");
                idField.setText("");
                nameField.setText("");
                emailField.setText("");
                deptIdField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return wrapWithTitle(panel, "Add New User");
    }

    private JPanel createResourcePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField typeField = new JTextField(15);
        JTextField locationField = new JTextField(15);
        JTextField capacityField = new JTextField(15);
        JTextField deptIdField = new JTextField(15);

        int row = 0;
        addFormField(panel, "Resource ID:", idField, gbc, row++);
        addFormField(panel, "Resource Name:", nameField, gbc, row++);
        addFormField(panel, "Type:", typeField, gbc, row++);
        addFormField(panel, "Location:", locationField, gbc, row++);
        addFormField(panel, "Capacity:", capacityField, gbc, row++);
        addFormField(panel, "Department ID:", deptIdField, gbc, row++);

        JButton submitButton = new JButton("Add Resource");
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String sql = "INSERT INTO Resource (Resource_ID, Resource_Name, Resource_Type, Location, Capacity, Department_ID) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(idField.getText().trim()));
                pstmt.setString(2, nameField.getText().trim());
                pstmt.setString(3, typeField.getText().trim());
                pstmt.setString(4, locationField.getText().trim());
                pstmt.setInt(5, Integer.parseInt(capacityField.getText().trim()));
                pstmt.setInt(6, Integer.parseInt(deptIdField.getText().trim()));

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Resource added successfully!");
                idField.setText("");
                nameField.setText("");
                typeField.setText("");
                locationField.setText("");
                capacityField.setText("");
                deptIdField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return wrapWithTitle(panel, "Add New Resource");
    }

    private JPanel createDepartmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField blockField = new JTextField(15);

        int row = 0;
        addFormField(panel, "Department ID:", idField, gbc, row++);
        addFormField(panel, "Department Name:", nameField, gbc, row++);
        addFormField(panel, "Block:", blockField, gbc, row++);

        JButton submitButton = new JButton("Add Department");
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String sql = "INSERT INTO Department (Department_ID, Department_Name, Block) VALUES (?, ?, ?)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(idField.getText().trim()));
                pstmt.setString(2, nameField.getText().trim());
                pstmt.setString(3, blockField.getText().trim());

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Department added successfully!");
                idField.setText("");
                nameField.setText("");
                blockField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return wrapWithTitle(panel, "Add New Department");
    }

    private JPanel createApproveBookingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField bookingIdField = new JTextField(15);
        JComboBox<String> statusCombo = new JComboBox<>(new String[] { "Approved", "Rejected" });

        int row = 0;
        addFormField(panel, "Booking ID:", bookingIdField, gbc, row++);

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("New Status:"), gbc);
        gbc.gridx = 1;
        panel.add(statusCombo, gbc);
        row++;

        JButton submitButton = new JButton("Update Status");
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String sql = "UPDATE Booking SET Status = ? WHERE Booking_ID = ?";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, statusCombo.getSelectedItem().toString());
                pstmt.setInt(2, Integer.parseInt(bookingIdField.getText().trim()));

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Booking status updated successfully!");
                    bookingIdField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Booking ID not found.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return wrapWithTitle(panel, "Approve Bookings");
    }

    private JPanel createUsageLogPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField logIdField = new JTextField(15);
        JTextField bookingIdField = new JTextField(15);
        JTextField startField = new JTextField(15);
        JTextField endField = new JTextField(15);
        JTextField durationField = new JTextField(15);

        int row = 0;
        addFormField(panel, "Log ID:", logIdField, gbc, row++);
        addFormField(panel, "Booking ID:", bookingIdField, gbc, row++);
        addFormField(panel, "Actual Start Time (HH:MM:SS):", startField, gbc, row++);
        addFormField(panel, "Actual End Time (HH:MM:SS):", endField, gbc, row++);
        addFormField(panel, "Duration (minutes):", durationField, gbc, row++);

        JButton submitButton = new JButton("Add Usage Log");
        gbc.gridx = 1;
        gbc.gridy = row;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            String sql = "INSERT INTO Usage_Log (Log_ID, Booking_ID, Actual_Start_Time, Actual_End_Time, Usage_Duration) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(logIdField.getText().trim()));
                pstmt.setInt(2, Integer.parseInt(bookingIdField.getText().trim()));
                pstmt.setTime(3, Time.valueOf(startField.getText().trim()));
                pstmt.setTime(4, Time.valueOf(endField.getText().trim()));
                pstmt.setInt(5, Integer.parseInt(durationField.getText().trim()));

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Usage Log added successfully!");
                logIdField.setText("");
                bookingIdField.setText("");
                startField.setText("");
                endField.setText("");
                durationField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return wrapWithTitle(panel, "Add Usage Log");
    }

    private JPanel wrapWithTitle(JPanel panel, String title) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 30, 20);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrapper.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 20, 20);
        
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        wrapper.add(panel, gbc);
        return wrapper;
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}
