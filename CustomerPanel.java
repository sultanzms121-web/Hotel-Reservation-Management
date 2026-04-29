import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CustomerPanel extends JPanel {
    private DataStore dataStore;
    private JTextField txtId, txtName, txtPhone, txtEmail, txtSearch;
    private DefaultTableModel tableModel;
    private JTable table;
    private int hoveredRow = -1;

    private boolean editMode = false;
    private String editingCustomerId = null;
    
    private final String ID_PREFIX = "242-";

    private static final String CUST_FILE = "customers.dat";
    private static final String ROOM_FILE = "rooms.dat";
    private static final String RES_FILE = "reservations.dat";

    public CustomerPanel(DataStore dataStore) {
        this.dataStore = dataStore;
        setLayout(new BorderLayout());
        setOpaque(false);

        // -------------------- FORM PANEL --------------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addLabel(form, gbc, row, "Customer ID:");
        txtId = new JTextField(ID_PREFIX);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(txtId, gbc);

        txtId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtId.getText().startsWith(ID_PREFIX)) {
                    txtId.setText(ID_PREFIX);
                }
            }
        });

        row++;
        addLabel(form, gbc, row, "Full Name:");
        txtName = new JTextField();
        gbc.gridx = 1; gbc.gridy = row;
        form.add(txtName, gbc);

        row++;
        addLabel(form, gbc, row, "Phone:");
        txtPhone = new JTextField();
        gbc.gridx = 1; gbc.gridy = row;
        form.add(txtPhone, gbc);

        row++;
        addLabel(form, gbc, row, "Email:");
        txtEmail = new JTextField();
        gbc.gridx = 1; gbc.gridy = row;
        form.add(txtEmail, gbc);

        row++;
        // Use a FlowLayout with larger horizontal gap to accommodate longer buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        
        Color gold = new Color(212, 175, 55);
        btnPanel.add(createAnimatedButton("Add / Save Customer", gold, this::addOrSaveCustomer));
        btnPanel.add(createAnimatedButton("Edit Selected", gold, this::editCustomer));
        btnPanel.add(createAnimatedButton("Remove", gold, this::removeCustomer));

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        // -------------------- SEARCH FIELD --------------------
        row++;
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(18);
        txtSearch.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(gold),
                "Search Customers", 0, 0, null, gold));
        searchPanel.add(txtSearch);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(searchPanel, gbc);

        add(form, BorderLayout.NORTH);

        // -------------------- TABLE PANEL --------------------
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Phone", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(212, 175, 55, 180));
                    c.setForeground(Color.WHITE);
                } else if (row == hoveredRow) {
                    c.setBackground(new Color(255, 250, 205, 150));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(0, 0, 0, 0));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        table.setRowHeight(30);
        table.setOpaque(false);
        table.setGridColor(new Color(212, 175, 55, 80));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(gold);
        table.getTableHeader().setForeground(Color.WHITE);

        table.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(gold, 1));
        add(scrollPane, BorderLayout.CENTER);

        // -------------------- LIVE SEARCH LISTENER --------------------
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        refreshTable();
    }

    // ==================== ANIMATED BUTTON FACTORY ====================
    private JButton createAnimatedButton(String text, Color baseColor, Runnable action) {
        JButton btn = new JButton(text);
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(220, 30));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(baseColor.brighter());
                btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
            }
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(baseColor.darker());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(baseColor.brighter());
            }
        });

        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ==================== LOGIC METHODS ====================
    private void addOrSaveCustomer() {
        if (editMode) { saveEditedCustomer(); return; }
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) email = "NULL"; // <-- set NULL if blank
        if (id.equals(ID_PREFIX) || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID and Name are required.");
            return;
        }
        dataStore.addCustomer(new Customer(id, name, txtPhone.getText(), email));
        saveAndRefresh();
        clearFields();
    }

    private void editCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        editingCustomerId = tableModel.getValueAt(row, 0).toString();
        Customer c = dataStore.getCustomers().stream().filter(cust -> cust.getCustomerId().equals(editingCustomerId)).findFirst().orElse(null);
        if (c != null) {
            txtId.setText(c.getCustomerId()); txtName.setText(c.getName());
            txtPhone.setText(c.getPhone()); txtEmail.setText(c.getEmail());
            txtId.setEditable(false); editMode = true;
        }
    }

    private void saveEditedCustomer() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) email = "NULL"; // <-- set NULL if blank
        dataStore.getCustomers().removeIf(c -> c.getCustomerId().equals(editingCustomerId));
        dataStore.addCustomer(new Customer(txtId.getText(), txtName.getText(), txtPhone.getText(), email));
        editMode = false; editingCustomerId = null; txtId.setEditable(true);
        saveAndRefresh(); clearFields();
    }

    private void removeCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        dataStore.getCustomers().removeIf(c -> c.getCustomerId().equals(tableModel.getValueAt(row, 0).toString()));
        saveAndRefresh();
    }

    private void saveAndRefresh() {
        try { dataStore.saveAll(CUST_FILE, ROOM_FILE, RES_FILE); refreshTable(); }
        catch (IOException e) { JOptionPane.showMessageDialog(this, "Save Error: " + e.getMessage()); }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Customer c : dataStore.getCustomers()) 
            tableModel.addRow(new Object[]{c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail()});
    }

    private void clearFields() {
        txtId.setText(ID_PREFIX); txtName.setText(""); txtPhone.setText(""); txtEmail.setText("");
        txtId.setEditable(true); editMode = false;
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(212, 175, 55));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(lbl, gbc);
    }

    // ==================== LIVE SEARCH ====================
    private void filterTable() {
        String query = txtSearch.getText().toLowerCase();
        tableModel.setRowCount(0);
        for (Customer c : dataStore.getCustomers()) {
            if (c.getCustomerId().toLowerCase().contains(query) ||
                c.getName().toLowerCase().contains(query) ||
                c.getPhone().toLowerCase().contains(query)) {
                tableModel.addRow(new Object[]{c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail()});
            }
        }
    }
}
