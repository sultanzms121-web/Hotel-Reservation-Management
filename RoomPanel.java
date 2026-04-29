import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RoomPanel extends JPanel {
    private DataStore dataStore;
    private JComponent txtId, txtType, txtPrice; // txtType is now a JComboBox
    private DefaultTableModel tableModel;
    private JTable table;
    private int hoveredRow = -1;

    private boolean editMode = false;
    private String editingRoomId = null;
    private int roomCounter = 0;

    private static final String CUST_FILE = "customers.dat";
    private static final String ROOM_FILE = "rooms.dat";
    private static final String RES_FILE = "reservations.dat";
    
    private final Color GOLD = new Color(212, 175, 55);

    private JTextField txtSearch;

    public RoomPanel(DataStore dataStore) {
        this.dataStore = dataStore;
        setLayout(new BorderLayout());
        setOpaque(false);

        updateCounter();

        // -------------------- FORM PANEL --------------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addLabel(form, gbc, row, "Room ID:");
        txtId = new JTextField(generateNextRoomId());
        ((JTextField) txtId).setEditable(false);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(txtId, gbc);

        row++;
        addLabel(form, gbc, row, "Type:");
        txtType = new JComboBox<>(RoomType.values()); // Dropdown for Room Type
        ((JComboBox<?>) txtType).setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = row;
        form.add(txtType, gbc);

        row++;
        addLabel(form, gbc, row, "Price per night:");
        txtPrice = new JTextField();
        gbc.gridx = 1; gbc.gridy = row;
        form.add(txtPrice, gbc);

        row++;
        // -------------------- BUTTONS --------------------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        
        btnPanel.add(createAnimatedButton("Add / Save Room", GOLD, this::addOrSaveRoom));
        btnPanel.add(createAnimatedButton("Edit Room", GOLD, this::editRoom));
        btnPanel.add(createAnimatedButton("Remove Room", GOLD, this::removeRoom));

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        // -------------------- LIVE SEARCH FIELD --------------------
        row++;
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(18);
        txtSearch.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(GOLD),
                "Search Rooms", 0, 0, null, GOLD));
        searchPanel.add(txtSearch);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(searchPanel, gbc);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        add(form, BorderLayout.NORTH);

        // -------------------- TABLE PANEL --------------------
        tableModel = new DefaultTableModel(new Object[]{"ID","Type","Price"},0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        table.setShowGrid(true);
        table.setGridColor(new Color(212, 175, 55, 80));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBackground(GOLD);
        table.getTableHeader().setOpaque(true);

        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(GOLD, 1));
        add(scrollPane, BorderLayout.CENTER);

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
            public void mousePressed(MouseEvent e) { btn.setBackground(baseColor.darker()); }
            @Override
            public void mouseReleased(MouseEvent e) { btn.setBackground(baseColor.brighter()); }
        });

        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ==================== LOGIC METHODS ====================
    private void updateCounter() {
        int max = 0;
        for (Room r : dataStore.getRooms()) {
            try {
                int num = Integer.parseInt(r.getRoomId());
                if (num > max) max = num;
            } catch (Exception ignored) {}
        }
        this.roomCounter = max;
    }

    private String generateNextRoomId(){
        return String.format("%03d", roomCounter + 1);
    }

    private void addOrSaveRoom(){
        if(editMode){ saveEditedRoom(); return; }
        try{
            RoomType type = (RoomType) ((JComboBox<?>) txtType).getSelectedItem();
            double price = Double.parseDouble(((JTextField) txtPrice).getText().trim());
            dataStore.addRoom(new Room(generateNextRoomId(), type, price));
            updateCounter();
            saveDataToFile(); 
            refreshTable();
            clearForm();
        } catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this,"Invalid price format.");
        }
    }

    private void editRoom(){
        int row = table.getSelectedRow();
        if(row == -1){
            JOptionPane.showMessageDialog(this,"Select a room to edit.");
            return;
        }
        editingRoomId = tableModel.getValueAt(row,0).toString();
        Room r = findRoom(editingRoomId);
        if(r != null) {
            ((JTextField) txtId).setText(r.getRoomId());
            ((JComboBox<?>) txtType).setSelectedItem(r.getType());
            ((JTextField) txtPrice).setText(String.valueOf(r.getPricePerNight()));
            editMode = true;
        }
    }

    private void saveEditedRoom(){
        try{
            RoomType type = (RoomType) ((JComboBox<?>) txtType).getSelectedItem();
            double price = Double.parseDouble(((JTextField) txtPrice).getText().trim());
            dataStore.getRooms().removeIf(r -> r.getRoomId().equals(editingRoomId));
            dataStore.addRoom(new Room(editingRoomId, type, price));

            saveDataToFile();
            editMode = false;
            editingRoomId = null;
            clearForm();
            refreshTable();
            JOptionPane.showMessageDialog(this,"Room updated.");
        } catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Error in price.");
        }
    }

    private void removeRoom(){
        int selected = table.getSelectedRow();
        if(selected == -1) return;
        String id = tableModel.getValueAt(selected,0).toString();
        dataStore.getRooms().removeIf(r -> r.getRoomId().equals(id));
        dataStore.getReservations().removeIf(res -> res.getRoom().getRoomId().equals(id));
        
        saveDataToFile();
        refreshTable();
        updateCounter();
        ((JTextField) txtId).setText(generateNextRoomId());
    }

    private void saveDataToFile() {
        try { dataStore.saveAll(CUST_FILE, ROOM_FILE, RES_FILE); }
        catch (IOException e) { JOptionPane.showMessageDialog(this, "Save Failed: " + e.getMessage()); }
    }

    private void refreshTable(){
        tableModel.setRowCount(0);
        for(Room r : dataStore.getRooms()){
            tableModel.addRow(new Object[]{r.getRoomId(), r.getType(), r.getPricePerNight()});
        }
    }

    private void clearForm() {
        ((JComboBox<?>) txtType).setSelectedIndex(0);
        ((JTextField) txtPrice).setText("");
        ((JTextField) txtId).setText(generateNextRoomId());
        editMode = false;
    }

    private Room findRoom(String id){
        return dataStore.getRooms().stream()
                .filter(r->r.getRoomId().equals(id))
                .findFirst().orElse(null);
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, String text){
        JLabel lbl = new JLabel(text);
        lbl.setForeground(GOLD);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(lbl, gbc);
    }

    private void filterTable() {
        String q = txtSearch.getText().toLowerCase();
        tableModel.setRowCount(0);
        for(Room r : dataStore.getRooms()){
            if(r.getRoomId().toLowerCase().contains(q) ||
               r.getType().toString().toLowerCase().contains(q) ||
               String.valueOf(r.getPricePerNight()).toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{r.getRoomId(), r.getType(), r.getPricePerNight()});
            }
        }
    }
}
