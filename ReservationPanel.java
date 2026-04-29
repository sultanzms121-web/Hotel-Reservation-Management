import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;

public class ReservationPanel extends JPanel {

    private DataStore dataStore;
    private JTextField txtId, txtSearch;
    private JComboBox<Customer> cbCustomer;
    private JComboBox<Room> cbRoom;
    private JSpinner spinnerCheckIn, spinnerCheckOut;

    private JTable tablePending, tableConfirmed;
    private DefaultTableModel modelPending, modelConfirmed;

    private boolean editMode = false;
    private String editingReservationId = null;

    private final Color GOLD = new Color(212, 175, 55);
    private final String ID_PREFIX = "246-";

    private JTabbedPane tabbedPane;
    private JButton btnAddSave, btnConfirm;

    public ReservationPanel(DataStore dataStore) {
        this.dataStore = dataStore;
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addLabel(form, gbc, row, "Reservation ID:");
        txtId = new JTextField(ID_PREFIX);
        gbc.gridx = 1;
        form.add(txtId, gbc);

        row++;
        addLabel(form, gbc, row, "Customer:");
        cbCustomer = new JComboBox<>();
        gbc.gridx = 1;
        form.add(cbCustomer, gbc);

        row++;
        addLabel(form, gbc, row, "Room:");
        cbRoom = new JComboBox<>();
        gbc.gridx = 1;
        form.add(cbRoom, gbc);

        row++;
        addLabel(form, gbc, row, "Check-in:");
        spinnerCheckIn = createDateSpinner();
        gbc.gridx = 1;
        form.add(spinnerCheckIn, gbc);

        row++;
        addLabel(form, gbc, row, "Check-out:");
        spinnerCheckOut = createDateSpinner();
        gbc.gridx = 1;
        form.add(spinnerCheckOut, gbc);

        // Update available rooms whenever the date changes
        spinnerCheckIn.addChangeListener(e -> updateAvailableRooms());
        spinnerCheckOut.addChangeListener(e -> updateAvailableRooms());

        row++;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);

        btnAddSave = createButton("Add Reservation", this::handleSaveAction);
        btnConfirm = createButton("Confirm Reservation", this::confirmReservation);

        btnPanel.add(btnAddSave);
        btnPanel.add(createButton("Edit Selected", this::editReservation));
        btnPanel.add(btnConfirm);
        btnPanel.add(createButton("Cancel Reservation", this::cancelReservation));
        btnPanel.add(createButton("Clear/Reset Form", this::clearForm));

        btnPanel.setPreferredSize(new Dimension(1000, 45));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        topContainer.add(form);

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        searchContainer.setOpaque(false);
        txtSearch = new JTextField(18);
        txtSearch.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD),
                "Search Records", 0, 0, null, GOLD));
        searchContainer.add(txtSearch);

        topContainer.add(searchContainer);
        add(topContainer, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = GOLD;
                lightHighlight = GOLD;
                shadow = GOLD.darker();
                focus = new Color(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y,
                                              int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(isSelected ? GOLD : new Color(212, 175, 55, 120));
                g2.fillRect(x, y, w, h);
                g2.dispose();
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
        });

        tabbedPane.addChangeListener(e ->
                btnConfirm.setEnabled(tabbedPane.getSelectedIndex() == 0));

        modelPending = createModel();
        tablePending = createGlassTable(modelPending);

        modelConfirmed = createModel();
        tableConfirmed = createGlassTable(modelConfirmed);

        tabbedPane.addTab("Pending Reservations", createTransparentScrollPane(tablePending));
        tabbedPane.addTab("Confirmed Reservations", createTransparentScrollPane(tableConfirmed));

        add(tabbedPane, BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshTables(); }
            public void removeUpdate(DocumentEvent e) { refreshTables(); }
            public void changedUpdate(DocumentEvent e) { refreshTables(); }
        });

        loadCombos();
        refreshTables();
        clearForm();
    }

    /* ================= TABLE & UI HELPERS ================= */

    private JTable createGlassTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };

        table.setOpaque(false);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean isS,
                    boolean hasF, int r, int c) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, v, isS, hasF, r, c);

                lbl.setHorizontalAlignment(SwingConstants.CENTER);

                if (isS) {
                    lbl.setOpaque(true);
                    lbl.setBackground(new Color(212, 175, 55, 180));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setOpaque(false);
                    lbl.setForeground(Color.BLACK);
                }

                lbl.setBorder(BorderFactory.createLineBorder(GOLD, 1));
                return lbl;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(GOLD);
        header.setForeground(Color.WHITE);

        return table;
    }

    private JScrollPane createTransparentScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private DefaultTableModel createModel() {
        return new DefaultTableModel(
                new Object[]{"Reservation ID", "Customer Name", "Room ID", "CheckIn", "CheckOut"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JSpinner createDateSpinner() {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        return sp;
    }

    private void addLabel(JPanel p, GridBagConstraints g, int y, String t) {
        JLabel lbl = new JLabel(t);
        lbl.setForeground(GOLD);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.gridx = 0;
        g.gridy = y;
        p.add(lbl, g);
    }

    private JButton createButton(String text, Runnable r) {
        JButton b = new JButton(text);
        b.setBackground(GOLD);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.addActionListener(e -> r.run());
        return b;
    }

    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String m) {
        JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ================= RESERVATION LOGIC ================= */

    private void handleSaveAction() {
        if (editMode) saveEditedReservation();
        else addReservation();
    }

    private void addReservation() {
        Reservation r = buildReservation(null);
        if (r == null) return;

        if (checkDateConflict(r.getRoom(), r.getCheckIn(), r.getCheckOut(), null)) {
            warn("This room is already reserved for the selected dates.");
            return;
        }

        dataStore.addPendingReservation(r);
        saveDataAndRefresh("New Reservation Added!");
    }

    private void editReservation() {
        JTable table = tabbedPane.getSelectedIndex() == 0 ? tablePending : tableConfirmed;
        int row = table.getSelectedRow();
        if (row == -1) { warn("Select a Reservation first."); return; }

        editingReservationId = table.getValueAt(row, 0).toString();

        List<Reservation> list = tabbedPane.getSelectedIndex() == 0
                ? dataStore.getPendingReservations()
                : dataStore.getConfirmedReservations();

        for (Reservation r : list) {
            if (r.getReservationId().equals(editingReservationId)) {
                txtId.setText(r.getReservationId());
                txtId.setEditable(false);
                cbCustomer.setSelectedItem(r.getCustomer());
                cbRoom.setSelectedItem(r.getRoom());
                spinnerCheckIn.setValue(Date.from(
                        r.getCheckIn().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                spinnerCheckOut.setValue(Date.from(
                        r.getCheckOut().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                editMode = true;
                btnAddSave.setText("Update Changes");
                updateAvailableRooms();
                break;
            }
        }
    }

    private void saveEditedReservation() {
        Reservation updated = buildReservation(editingReservationId);
        if (updated == null) return;

        if (checkDateConflict(updated.getRoom(), updated.getCheckIn(), updated.getCheckOut(), editingReservationId)) {
            warn("This room is already reserved for the selected dates.");
            return;
        }

        List<Reservation> list = tabbedPane.getSelectedIndex() == 0
                ? dataStore.getPendingReservations()
                : dataStore.getConfirmedReservations();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getReservationId().equals(editingReservationId)) {
                list.set(i, updated);
                break;
            }
        }

        saveDataAndRefresh("Changes Saved!");
        clearForm();
    }

    private boolean checkDateConflict(Room room, LocalDate in, LocalDate out, String ignoreId) {
        for (Reservation r : dataStore.getPendingReservations()) {
            if (r.getRoom().equals(room) &&
                !r.getReservationId().equals(ignoreId) &&
                datesOverlap(r.getCheckIn(), r.getCheckOut(), in, out)) return true;
        }
        for (Reservation r : dataStore.getConfirmedReservations()) {
            if (r.getRoom().equals(room) &&
                !r.getReservationId().equals(ignoreId) &&
                datesOverlap(r.getCheckIn(), r.getCheckOut(), in, out)) return true;
        }
        return false;
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2.minusDays(1)) && !start2.isAfter(end1.minusDays(1));
    }

    private void confirmReservation() {
        int row = tablePending.getSelectedRow();
        if (row == -1) { warn("Select a pending reservation."); return; }

        String id = tablePending.getValueAt(row, 0).toString();
        Reservation r = dataStore.getPendingReservations()
                .stream().filter(x -> x.getReservationId().equals(id)).findFirst().orElse(null);

        if (r != null) {
            dataStore.getPendingReservations().remove(r);
            dataStore.addConfirmedReservation(r);
            r.getRoom().setStatus(Room.RoomStatus.BOOKED);
            saveDataAndRefresh("Reservation Confirmed!");
        }
    }

    private void cancelReservation() {
        JTable table = tabbedPane.getSelectedIndex() == 0 ? tablePending : tableConfirmed;
        int row = table.getSelectedRow();
        if (row == -1) return;

        String id = table.getValueAt(row, 0).toString();

        for (Reservation r : tabbedPane.getSelectedIndex() == 0 ? dataStore.getPendingReservations() : dataStore.getConfirmedReservations()) {
            if (r.getReservationId().equals(id)) {
                r.getRoom().setStatus(Room.RoomStatus.AVAILABLE);
                if (tabbedPane.getSelectedIndex() == 0)
                    dataStore.getPendingReservations().remove(r);
                else
                    dataStore.getConfirmedReservations().remove(r);
                break;
            }
        }
        saveDataAndRefresh("Reservation Removed.");
    }

    private Reservation buildReservation(String ignoreId) {
        String id = txtId.getText().trim();
        Customer c = (Customer) cbCustomer.getSelectedItem();
        Room room = (Room) cbRoom.getSelectedItem();

        if (id.isEmpty() || id.equals(ID_PREFIX) || c == null || room == null) {
            warn("Incomplete information.");
            return null;
        }

        LocalDate in = ((Date) spinnerCheckIn.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate out = ((Date) spinnerCheckOut.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        if (!out.isAfter(in)) {
            warn("Check-out must be after check-in.");
            return null;
        }

        return new Reservation(id, c, room, in, out);
    }

    private void refreshTables() {
        String q = txtSearch.getText().toLowerCase();
        updateModel(modelPending, dataStore.getPendingReservations(), q);
        updateModel(modelConfirmed, dataStore.getConfirmedReservations(), q);
    }

    private void updateModel(DefaultTableModel model, List<Reservation> list, String q) {
        model.setRowCount(0);
        for (Reservation r : list) {
            if (r.getCustomer().getName().toLowerCase().contains(q) ||
                r.getRoom().getRoomId().toLowerCase().contains(q)) {
                model.addRow(new Object[]{
                        r.getReservationId(),
                        r.getCustomer().getName(),
                        r.getRoom().getRoomId(),
                        r.getCheckIn(),
                        r.getCheckOut()
                });
            }
        }
    }

    private void loadCombos() {
        cbCustomer.removeAllItems();
        dataStore.getCustomers().forEach(cbCustomer::addItem);
        updateAvailableRooms();
    }

    private void saveDataAndRefresh(String msg) {
        try {
            dataStore.saveAll("customers.dat", "rooms.dat", "reservations.dat");
            refreshTables();
            info(msg);
        } catch (IOException e) {
            warn("Save failed.");
        }
    }

    private void clearForm() {
        txtId.setText(ID_PREFIX);
        txtId.setEditable(true);
        cbCustomer.setSelectedIndex(-1);
        cbRoom.removeAllItems();
        editMode = false;
        editingReservationId = null;
        btnAddSave.setText("Add Reservation");
        updateAvailableRooms();
    }

    // ================= NEW: RoomStatus update internally handled =================
    private void updateAvailableRooms() {
        cbRoom.removeAllItems();
        Date inDate = (Date) spinnerCheckIn.getValue();
        Date outDate = (Date) spinnerCheckOut.getValue();
        if (inDate == null || outDate == null) return;

        LocalDate in = inDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate out = outDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!out.isAfter(in)) return;

        for (Room room : dataStore.getRooms()) {
            room.setStatus(Room.RoomStatus.AVAILABLE);

            for (Reservation r : dataStore.getPendingReservations()) {
                if (r.getRoom().equals(room) && datesOverlap(r.getCheckIn(), r.getCheckOut(), in, out)) {
                    room.setStatus(Room.RoomStatus.BOOKED);
                    break;
                }
            }
            for (Reservation r : dataStore.getConfirmedReservations()) {
                if (r.getRoom().equals(room) && datesOverlap(r.getCheckIn(), r.getCheckOut(), in, out)) {
                    room.setStatus(Room.RoomStatus.BOOKED);
                    break;
                }
            }

            if (room.getStatus() == Room.RoomStatus.AVAILABLE) {
                cbRoom.addItem(room);
            }
        }
        cbRoom.setSelectedIndex(-1);
    }
}
