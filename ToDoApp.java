import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

public class ToDoApp extends JFrame {
    private DefaultListModel<Task> listModel = new DefaultListModel<>();
    private JList<Task> taskList = new JList<>(listModel);
    private JTextField taskField = new JTextField(20);
    private JButton addButton = new JButton("Add");
    private JButton deleteButton = new JButton("Delete");
    private JButton saveButton = new JButton("Save");
    private JButton loadButton = new JButton("Load");
    private JButton clearButton = new JButton("Clear All");

    public ToDoApp() {
        super("To-Do App (Java Swing)");
        setLayout(new BorderLayout(8, 8));

        // Top panel: input field + Add button
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        top.add(taskField, BorderLayout.CENTER);
        top.add(addButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center panel: task list
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskCellRenderer());
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Bottom panel: control buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottom.add(deleteButton);
        bottom.add(saveButton);
        bottom.add(loadButton);
        bottom.add(clearButton);
        add(bottom, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> addTask());
        taskField.addActionListener(e -> addTask()); // Press Enter to add
        deleteButton.addActionListener(e -> deleteSelected());
        saveButton.addActionListener(e -> saveToFile());
        loadButton.addActionListener(e -> loadFromFile());
        clearButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear all tasks?") == JOptionPane.YES_OPTION) {
                listModel.clear();
            }
        });

        // Double-click toggles task done/undone
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = taskList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        Task t = listModel.get(idx);
                        t.done = !t.done;
                        taskList.repaint();
                    }
                }
            }
        });

        // Press Delete key to remove a task
        taskList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) deleteSelected();
            }
        });

        // Window setup
        setSize(420, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Add new task
    private void addTask() {
        String text = taskField.getText().trim();
        if (!text.isEmpty()) {
            listModel.addElement(new Task(text));
            taskField.setText("");
            taskField.requestFocusInWindow();
        }
    }

    // Delete selected task
    private void deleteSelected() {
        int idx = taskList.getSelectedIndex();
        if (idx >= 0) listModel.remove(idx);
    }

    // Save tasks to file
    private void saveToFile() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("tasks.txt"))) {
            for (int i = 0; i < listModel.size(); i++) {
                Task t = listModel.get(i);
                bw.write((t.done ? "1" : "0") + "\t" + t.text);
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Tasks saved to tasks.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }

    // Load tasks from file
    private void loadFromFile() {
        Path file = Paths.get("tasks.txt");
        if (!Files.exists(file)) {
            JOptionPane.showMessageDialog(this, "No saved tasks found.");
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(file)) {
            listModel.clear();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                boolean done = parts[0].equals("1");
                String txt = parts.length > 1 ? parts[1] : "";
                Task t = new Task(txt);
                t.done = done;
                listModel.addElement(t);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
        }
    }

    // Task object
    private static class Task {
        String text;
        boolean done;
        Task(String t) { text = t; }
    }

    // Custom renderer for strike-through effect
    private static class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Task) {
                Task t = (Task) value;
                if (t.done) {
                    label.setText("<html><strike>" + escapeHTML(t.text) + "</strike></html>");
                } else {
                    label.setText("<html>" + escapeHTML(t.text) + "</html>");
                }
            }
            return label;
        }
        private static String escapeHTML(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoApp().setVisible(true));
    }
}
