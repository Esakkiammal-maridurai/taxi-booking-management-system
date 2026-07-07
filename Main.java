import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// 1. Taxi Class with Availability Status
class Taxi {
    private String taxiId;
    private String driverName;
    private String currentLocation;
    private double totalEarnings;
    private boolean isAvailable; // TRUE = Free, FALSE = Busy

    public Taxi(String taxiId, String driverName, String currentLocation, double totalEarnings, boolean isAvailable) {
        this.taxiId = taxiId;
        this.driverName = driverName;
        this.currentLocation = currentLocation;
        this.totalEarnings = totalEarnings;
        this.isAvailable = isAvailable;
    }

    public String getTaxiId() { return taxiId; }
    public String getDriverName() { return driverName; }
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String loc) { this.currentLocation = loc; }
    public double getTotalEarnings() { return totalEarnings; }
    public void addEarnings(double amount) { this.totalEarnings += amount; }
    
    public boolean getIsAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    // Save format split by commas (including boolean status)
    public String toFileString() {
        return taxiId + "," + driverName + "," + currentLocation + "," + totalEarnings + "," + isAvailable;
    }

    @Override
    public String toString() {
        String status = isAvailable ? "FREE" : "BUSY";
        return "ID: " + taxiId + " | Driver: " + driverName + " | Location: " + currentLocation + " | Earnings: $" + totalEarnings + " | Status: " + status;
    }
}

// 2. Main GUI Class
public class Main extends JFrame {
    private List<Taxi> taxis = new ArrayList<>();
    private List<String> bookingHistory = new ArrayList<>();

    private final double RATE_PER_KM = 2.0; 
    private final String TAXI_FILE = "taxis.txt";
    private final String HISTORY_FILE = "history.txt";

    private JTextArea displayArea;
    private JTextField txtTaxiId, txtDriverName, txtLocation, txtCustomer, txtPickup, txtDrop, txtDistance;

    public Main() {
        setTitle("Taxi Booking System - Auto Driver Assignment");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10));

        loadDataFromFile();

        // --- TITLE PANEL ---
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(41, 128, 185));
        JLabel lblTitle = new JLabel("TAXI BOOKING SYSTEM (AUTO-ASSIGN DRIVER)");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(lblTitle);
        add(titlePanel, BorderLayout.NORTH);

        // --- CONTROL PANEL ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Operations"));
        controlPanel.setPreferredSize(new Dimension(340, 550));

        // 1. Register Sub-Panel
        JPanel regPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        regPanel.setBorder(BorderFactory.createTitledBorder("Register Taxi"));
        regPanel.add(new JLabel("Taxi ID:")); txtTaxiId = new JTextField(); regPanel.add(txtTaxiId);
        regPanel.add(new JLabel("Driver Name:")); txtDriverName = new JTextField(); regPanel.add(txtDriverName);
        regPanel.add(new JLabel("Location:")); txtLocation = new JTextField(); regPanel.add(txtLocation);
        JButton btnRegister = new JButton("Register Taxi");
        regPanel.add(new JLabel("")); regPanel.add(btnRegister);

        // 2. Booking Sub-Panel (TAXI ID FIELD REMOVED - AUTO ASSIGNED NOW)
        JPanel bookPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        bookPanel.setBorder(BorderFactory.createTitledBorder("Book a Ride (Auto-Assign)"));
        bookPanel.add(new JLabel("Customer Name:")); txtCustomer = new JTextField(); bookPanel.add(txtCustomer);
        bookPanel.add(new JLabel("Pickup Location:")); txtPickup = new JTextField(); bookPanel.add(txtPickup);
        bookPanel.add(new JLabel("Drop Location:")); txtDrop = new JTextField(); bookPanel.add(txtDrop);
        bookPanel.add(new JLabel("Distance (KM):")); txtDistance = new JTextField(); bookPanel.add(txtDistance);
        JButton btnBook = new JButton("Auto-Assign & Book");
        bookPanel.add(new JLabel("")); bookPanel.add(btnBook);

        // 3. Quick System Buttons Panel
        JPanel systemButtonsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JButton btnViewAll = new JButton("View All Taxis");
        JButton btnHistory = new JButton("Booking History");
        JButton btnCount = new JButton("Total Taxis Count");
        JButton btnEarnings = new JButton("Check Taxi Earnings");
        JButton btnRelease = new JButton("Release/Free a Taxi"); // New Feature Button
        
        systemButtonsPanel.add(btnViewAll);
        systemButtonsPanel.add(btnHistory);
        systemButtonsPanel.add(btnCount);
        systemButtonsPanel.add(btnEarnings);
        systemButtonsPanel.add(btnRelease);

        controlPanel.add(regPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(bookPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(systemButtonsPanel);

        add(controlPanel, BorderLayout.WEST);

        // --- DISPLAY PANEL ---
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("System Output Console"));
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        displayPanel.add(scrollPane, BorderLayout.CENTER);

        add(displayPanel, BorderLayout.CENTER);

        displayArea.setText("System Ready. Free drivers will be auto-assigned upon booking.");

        // --- ACTION LISTENERS ---

        // Register Taxi
        btnRegister.addActionListener(e -> {
            String id = txtTaxiId.getText().trim();
            String name = txtDriverName.getText().trim();
            String loc = txtLocation.getText().trim();

            if (id.isEmpty() || name.isEmpty() || loc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (Taxi t : taxis) {
                if (t.getTaxiId().equalsIgnoreCase(id)) {
                    displayArea.setText("Error: Taxi ID " + id + " already exists!");
                    return;
                }
            }
            taxis.add(new Taxi(id, name, loc, 0.0, true)); // Default true (Free)
            saveDataToFile();
            displayArea.setText("Taxi Registered Successfully!\nDriver: " + name + "\nStatus: FREE");
            clearRegistrationFields();
        });

        // Book Taxi with AUTO ASSIGNMENT
        btnBook.addActionListener(e -> {
            String cust = txtCustomer.getText().trim();
            String pUp = txtPickup.getText().trim();
            String drp = txtDrop.getText().trim();
            String distStr = txtDistance.getText().trim();

            if (cust.isEmpty() || pUp.isEmpty() || drp.isEmpty() || distStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all booking fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double distance = Double.parseDouble(distStr);
                if (distance <= 0) {
                    JOptionPane.showMessageDialog(this, "Distance must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Core Logic: Find the first FREE driver
                Taxi assignedTaxi = null;
                for (Taxi t : taxis) {
                    if (t.getIsAvailable()) {
                        assignedTaxi = t;
                        break; // Available driver kedaichachu, loop out!
                    }
                }

                if (assignedTaxi == null) {
                    displayArea.setText("SORRY! No drivers are free right now. Please release a taxi or try again later.");
                    JOptionPane.showMessageDialog(this, "All drivers are currently BUSY!", "No Taxi Available", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Process booking for the auto-assigned taxi
                double autoCalculatedFare = distance * RATE_PER_KM;
                assignedTaxi.setCurrentLocation(drp);
                assignedTaxi.addEarnings(autoCalculatedFare);
                assignedTaxi.setAvailable(false); // Mark as BUSY

                String historyRecord = "Customer: " + cust + " | Assigned Driver: " + assignedTaxi.getDriverName() + 
                                       " (ID: " + assignedTaxi.getTaxiId() + ") | " + pUp + " -> " + drp + " | Fare: $" + autoCalculatedFare;
                bookingHistory.add(historyRecord);

                saveDataToFile(); 
                displayArea.setText("Booking Confirmed Automatically!\n" + historyRecord);
                clearBookingFields();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for distance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Release/Free a Taxi (Make a busy driver free again)
        btnRelease.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(this, "Enter Taxi ID to make it FREE:");
            if (id != null && !id.trim().isEmpty()) {
                Taxi t = findTaxi(id.trim());
                if (t != null) {
                    if (t.getIsAvailable()) {
                        displayArea.setText("Taxi ID " + id + " is already FREE.");
                    } else {
                        t.setAvailable(true);
                        saveDataToFile();
                        displayArea.setText("Taxi ID " + id + " (" + t.getDriverName() + ") is now FREE and ready for new trips!");
                    }
                } else {
                    displayArea.setText("Taxi ID '" + id + "' not found.");
                }
            }
        });

        // View All Taxis
        btnViewAll.addActionListener(e -> {
            if (taxis.isEmpty()) {
                displayArea.setText("No taxis registered yet.");
                return;
            }
            StringBuilder sb = new StringBuilder("=== REGISTERED TAXIS & AVAILABILITY ===\n");
            for (Taxi t : taxis) sb.append(t.toString()).append("\n");
            displayArea.setText(sb.toString());
        });

        // View Booking History
        btnHistory.addActionListener(e -> {
            if (bookingHistory.isEmpty()) {
                displayArea.setText("No bookings recorded yet.");
                return;
            }
            StringBuilder sb = new StringBuilder("=== TRIP BOOKING HISTORY ===\n");
            for (String h : bookingHistory) sb.append(h).append("\n");
            displayArea.setText(sb.toString());
        });

        // Display Total Count
        btnCount.addActionListener(e -> displayArea.setText("Total Number of Registered Taxis: " + taxis.size()));

        // Display Earnings
        btnEarnings.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(this, "Enter Taxi ID to check earnings:");
            if (id != null && !id.trim().isEmpty()) {
                Taxi t = findTaxi(id.trim());
                if (t != null) {
                    displayArea.setText("Taxi ID: " + t.getTaxiId() + "\nDriver: " + t.getDriverName() + "\nTotal Earnings: $" + t.getTotalEarnings());
                } else {
                    displayArea.setText("Taxi ID '" + id + "' not found.");
                }
            }
        });
    }

    // --- FILE HANDLING ---
    private void saveDataToFile() {
        try {
            BufferedWriter taxiWriter = new BufferedWriter(new FileWriter(TAXI_FILE));
            for (Taxi t : taxis) {
                taxiWriter.write(t.toFileString());
                taxiWriter.newLine();
            }
            taxiWriter.close();

            BufferedWriter historyWriter = new BufferedWriter(new FileWriter(HISTORY_FILE));
            for (String h : bookingHistory) {
                historyWriter.write(h);
                historyWriter.newLine();
            }
            historyWriter.close();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadDataFromFile() {
        try {
            File fileTaxis = new File(TAXI_FILE);
            if (fileTaxis.exists()) {
                BufferedReader taxiReader = new BufferedReader(new FileReader(fileTaxis));
                String line;
                while ((line = taxiReader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 5) { // Now expects 5 parameters
                        taxis.add(new Taxi(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), Boolean.parseBoolean(parts[4])));
                    }
                }
                taxiReader.close();
            }

            File fileHistory = new File(HISTORY_FILE);
            if (fileHistory.exists()) {
                BufferedReader historyReader = new BufferedReader(new FileReader(fileHistory));
                String line;
                while ((line = historyReader.readLine()) != null) {
                    bookingHistory.add(line);
                }
                historyReader.close();
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private Taxi findTaxi(String id) {
        for (Taxi t : taxis) {
            if (t.getTaxiId().equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    private void clearRegistrationFields() {
        txtTaxiId.setText(""); txtDriverName.setText(""); txtLocation.setText("");
    }

    private void clearBookingFields() {
        txtCustomer.setText(""); txtPickup.setText(""); txtDrop.setText(""); txtDistance.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
