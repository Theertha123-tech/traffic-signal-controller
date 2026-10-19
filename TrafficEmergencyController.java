import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TrafficEmergencyController extends JFrame {
    private static final int LANE_COUNT = 4;

    private final Queue<String>[] laneQueues = new LinkedList[LANE_COUNT];
    private final PriorityQueue<Vehicle> emergencyQueue =
            new PriorityQueue<>(Comparator.comparingInt(v -> v.priority));

    private final JTextArea[] laneDisplays = new JTextArea[LANE_COUNT];
    private final JLabel[] signalLabels = new JLabel[LANE_COUNT];
    private final JLabel statusLabel = new JLabel("System ready");
    private final JLabel emergencyLabel = new JLabel("Emergency vehicles waiting: 0");
    private final JComboBox<String> emergencyLaneChoice =
            new JComboBox<>(new String[]{"North", "East", "South", "West"});
    private final JComboBox<String> densityChoice =
            new JComboBox<>(new String[]{"Low", "Medium", "High"});

    private final String[] laneNames = {"North", "East", "South", "West"};
    private int activeLane = 0;
    private int vehicleNumber = 1;
    private boolean emergencyMode = false;

    private static class Vehicle {
        String name;
        int lane;
        int priority;

        Vehicle(String name, int lane, int priority) {
            this.name = name;
            this.lane = lane;
            this.priority = priority;
        }
    }

    @SuppressWarnings("unchecked")
    public TrafficEmergencyController() {
        super("Smart Multi-Level Traffic & Emergency Signal Controller");
        for (int i = 0; i < LANE_COUNT; i++) {
            laneQueues[i] = new LinkedList<>();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "SMART TRAFFIC & EMERGENCY SIGNAL CONTROLLER",
                SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        JPanel lanesPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        lanesPanel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        for (int i = 0; i < LANE_COUNT; i++) {
            final int lane = i;
            JPanel lanePanel = new JPanel(new BorderLayout(5, 5));
            lanePanel.setBorder(BorderFactory.createTitledBorder(laneNames[i] + " Lane"));

            signalLabels[i] = new JLabel("RED", SwingConstants.CENTER);
            signalLabels[i].setOpaque(true);
            signalLabels[i].setBackground(Color.RED);
            signalLabels[i].setForeground(Color.WHITE);
            signalLabels[i].setFont(new Font("Arial", Font.BOLD, 16));
            signalLabels[i].setPreferredSize(new Dimension(100, 32));

            laneDisplays[i] = new JTextArea(5, 20);
            laneDisplays[i].setEditable(false);
            laneDisplays[i].setFont(new Font("Monospaced", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(laneDisplays[i]);

            JButton addCarButton = new JButton("Add Normal Vehicle");
            addCarButton.addActionListener(e -> {
                laneQueues[lane].offer("Car-" + vehicleNumber++);
                refreshDisplays();
                statusLabel.setText("Normal vehicle added to " + laneNames[lane] + " lane.");
            });

            JPanel top = new JPanel(new BorderLayout());
            top.add(signalLabels[i], BorderLayout.NORTH);
            top.add(new JLabel("Vehicles waiting:", SwingConstants.CENTER), BorderLayout.SOUTH);

            lanePanel.add(top, BorderLayout.NORTH);
            lanePanel.add(scroll, BorderLayout.CENTER);
            lanePanel.add(addCarButton, BorderLayout.SOUTH);
            lanesPanel.add(lanePanel);
        }
        add(lanesPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(0, 1, 5, 5));
        controls.setBorder(BorderFactory.createTitledBorder("Control Panel"));

        JPanel emergencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emergencyPanel.add(new JLabel("Emergency vehicle lane:"));
        emergencyPanel.add(emergencyLaneChoice);
        JButton addAmbulance = new JButton("Add Ambulance");
        addAmbulance.addActionListener(e -> addEmergency("Ambulance"));
        JButton addFireEngine = new JButton("Add Fire Engine");
        addFireEngine.addActionListener(e -> addEmergency("Fire Engine"));
        JButton addPolice = new JButton("Add Police Vehicle");
        addPolice.addActionListener(e -> addEmergency("Police Vehicle"));
        emergencyPanel.add(addAmbulance);
        emergencyPanel.add(addFireEngine);
        emergencyPanel.add(addPolice);

        JPanel signalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        signalPanel.add(new JLabel("Traffic density:"));
        signalPanel.add(densityChoice);
        JButton nextSignal = new JButton("Next Signal");
        nextSignal.addActionListener(e -> moveToNextSignal());
        JButton passVehicle = new JButton("Pass Vehicle");
        passVehicle.addActionListener(e -> passVehicle());
        JButton reset = new JButton("Reset Queues");
        reset.addActionListener(e -> resetQueues());
        signalPanel.add(nextSignal);
        signalPanel.add(passVehicle);
        signalPanel.add(reset);

        controls.add(emergencyPanel);
        controls.add(signalPanel);
        controls.add(emergencyLabel);
        controls.add(statusLabel);
        add(controls, BorderLayout.SOUTH);

        refreshDisplays();
        updateSignals();
    }

    private void addEmergency(String type) {
        int lane = emergencyLaneChoice.getSelectedIndex();
        // Lower number means higher priority: ambulance, fire engine, police.
        int priority = type.equals("Ambulance") ? 1
                : type.equals("Fire Engine") ? 2 : 3;
        emergencyQueue.offer(new Vehicle(type + "-" + vehicleNumber++, lane, priority));
        refreshDisplays();
        statusLabel.setText(type + " added to " + laneNames[lane]
                + " lane. Emergency priority is enabled.");
    }

    private void moveToNextSignal() {
        if (!emergencyQueue.isEmpty()) {
            Vehicle emergency = emergencyQueue.peek();
            activeLane = emergency.lane;
            emergencyMode = true;
            statusLabel.setText("Emergency priority: " + emergency.name
                    + " in " + laneNames[activeLane] + " lane.");
        } else {
            emergencyMode = false;
            activeLane = (activeLane + 1) % LANE_COUNT;
            String density = (String) densityChoice.getSelectedItem();
            statusLabel.setText(density + " traffic: " + laneNames[activeLane]
                    + " lane has the green signal.");
        }
        updateSignals();
    }

    private void passVehicle() {
        if (!emergencyQueue.isEmpty()) {
            Vehicle emergency = emergencyQueue.peek();
            if (emergencyMode && emergency.lane == activeLane) {
                emergencyQueue.poll();
                statusLabel.setText(emergency.name + " passed through the intersection.");
                emergencyMode = false;
                refreshDisplays();
                updateSignals();
                return;
            }
        }

        if (laneQueues[activeLane].isEmpty()) {
            statusLabel.setText("No normal vehicles waiting in "
                    + laneNames[activeLane] + " lane.");
        } else {
            String vehicle = laneQueues[activeLane].poll();
            statusLabel.setText(vehicle + " passed from "
                    + laneNames[activeLane] + " lane.");
        }
        refreshDisplays();
        updateSignals();
    }

    private void resetQueues() {
        for (Queue<String> queue : laneQueues) {
            queue.clear();
        }
        emergencyQueue.clear();
        emergencyMode = false;
        activeLane = 0;
        refreshDisplays();
        updateSignals();
        statusLabel.setText("All queues have been reset.");
    }

    private void refreshDisplays() {
        for (int i = 0; i < LANE_COUNT; i++) {
            StringBuilder text = new StringBuilder();
            int position = 1;
            for (String vehicle : laneQueues[i]) {
                text.append(position++).append(". ").append(vehicle).append('\n');
            }
            for (Vehicle v : emergencyQueue) {
                if (v.lane == i) {
                    text.append("! ").append(v.name).append(" [EMERGENCY]\n");
                }
            }
            if (text.length() == 0) {
                text.append("No vehicles waiting.");
            }
            laneDisplays[i].setText(text.toString());
        }
        emergencyLabel.setText("Emergency vehicles waiting: " + emergencyQueue.size());
    }

    private void updateSignals() {
        for (int i = 0; i < LANE_COUNT; i++) {
            if (i == activeLane) {
                signalLabels[i].setText(emergencyMode ? "GREEN - EMERGENCY" : "GREEN");
                signalLabels[i].setBackground(new Color(0, 150, 0));
            } else {
                signalLabels[i].setText("RED");
                signalLabels[i].setBackground(Color.RED);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrafficEmergencyController app = new TrafficEmergencyController();
            app.setVisible(true);
        });
    }
}
