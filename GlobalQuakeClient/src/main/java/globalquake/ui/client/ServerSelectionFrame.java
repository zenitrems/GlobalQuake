package globalquake.ui.client;

import globalquake.client.ClientSocket;
import globalquake.client.GlobalQuakeClient;
import globalquake.core.GlobalQuake;
import globalquake.core.exception.RuntimeApplicationException;
import globalquake.core.geo.taup.TauPTravelTimeCalculator;
import globalquake.core.intensity.IntensityTable;
import globalquake.intensity.ShakeMap;
import globalquake.main.Main;
import globalquake.core.regions.Regions;
import globalquake.sounds.Sounds;
import globalquake.ui.GQFrame;
import globalquake.utils.Scale;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.Executors;

public class ServerSelectionFrame extends GQFrame {

    private JTextField addressField;
    private JTextField portField;

    private final ClientSocket client;
    private JButton connectButton;

    public ServerSelectionFrame() {
        client = new ClientSocket();
        setTitle(Main.fullName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400,200));

        add(createServerSelectionPanel());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createServerSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        var grid=  new GridLayout(2,1);
        grid.setVgap(5);
        JPanel addressPanel = new JPanel(grid);
        addressPanel.setBorder(BorderFactory.createTitledBorder("Server address"));

        JPanel ipPanel = new JPanel();
        ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.X_AXIS));
        ipPanel.add(new JLabel("IP Address: "));
        ipPanel.add(addressField = new JTextField("0.0.0.0",20));

        addressPanel.add(ipPanel);

        JPanel portPanel = new JPanel();
        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
        portPanel.add(new JLabel("Port: "));
        portPanel.add(portField = new JTextField("12345",20));

        addressPanel.add(portPanel);

        panel.add(addressPanel);

        var gridl2 = new GridLayout(1,2);
        gridl2.setVgap(5);
        JPanel buttonsPanel = new JPanel(gridl2);
        buttonsPanel.setBorder(new EmptyBorder(5,5,5,5));

        connectButton = new JButton("Connect");
        connectButton.addActionListener(actionEvent1 -> connect());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(actionEvent -> {
            ServerSelectionFrame.this.dispose();
            new MainFrame().setVisible(true);
        });

        buttonsPanel.add(connectButton);
        buttonsPanel.add(backButton);

        panel.add(buttonsPanel);

        return panel;
    }

    private void connect() {
        Executors.newSingleThreadExecutor().submit(() -> {
            addressField.setEnabled(false);
            portField.setEnabled(false);
            connectButton.setEnabled(false);
            connectButton.setText("Connecting...");
            try {
                client.connect(addressField.getText(), Integer.parseInt(portField.getText()));
                client.runReconnectService();
                ServerSelectionFrame.this.dispose();
                launchClientUI();
            } catch (Exception e) {
                GlobalQuake.getErrorHandler().handleWarning(new RuntimeApplicationException("Failed to connect to the server: %s".formatted(e.getMessage())));
                connectButton.setText("Connect");
            } finally {
                addressField.setEnabled(true);
                portField.setEnabled(true);
                connectButton.setEnabled(true);
            }
        });
    }

    private void launchClientUI() {
        new GlobalQuakeClient().createFrame();
    }

    public static void main(String[] args) throws Exception{
        init();
        new ServerSelectionFrame();
    }

    private static void init() throws Exception{
        Regions.init();
        Scale.load();
        ShakeMap.init();
        Sounds.load();
        IntensityTable.init();
        TauPTravelTimeCalculator.init();
    }

}
