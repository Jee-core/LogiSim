package org.yourcompany.yourproject.frontend;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Hierarchy extends JPanel {

    public DefaultListModel<String> model;
    public JList<String> list;

    public Hierarchy() {
        setLayout(new BorderLayout());

        // Much smaller compact size
        setPreferredSize(new Dimension(150, 180));
        setMinimumSize(new Dimension(140, 160));
        setMaximumSize(new Dimension(180, 220));

        // Modern border with better styling
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(100, 100, 150), 1),
                        BorderFactory.createEmptyBorder(2, 4, 2, 4)),
                "PROJECTS",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 10),
                new Color(220, 220, 255)));

        // Set a dark modern background
        setBackground(new Color(45, 45, 55));

        model = new DefaultListModel<>();
        list = new JList<>(model);

        // Modern list styling
        list.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        list.setBackground(new Color(35, 35, 45));
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(new Color(70, 130, 180));
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(16); // Smaller row height

        // Enable both horizontal and vertical scrolling
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(140, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Style the scrollbars
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        // Custom scrollbar styling
        verticalScrollBar.setPreferredSize(new Dimension(10, 0));
        horizontalScrollBar.setPreferredSize(new Dimension(0, 10));

        verticalScrollBar.setBackground(new Color(45, 45, 55));
        horizontalScrollBar.setBackground(new Color(45, 45, 55));

        // Remove scrollbar borders for cleaner look
        verticalScrollBar.setBorder(BorderFactory.createEmptyBorder());
        horizontalScrollBar.setBorder(BorderFactory.createEmptyBorder());

        // Add some sample data to demonstrate scrolling

        add(scrollPane, BorderLayout.CENTER);
    }

}