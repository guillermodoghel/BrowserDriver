package com.browserdriver.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class Tests {
	private static int count;
	private static JLabel lblNewLabel;
	private static JLabel lblNewLabel_2 ;
	public static void main(String[] args) throws IllegalArgumentException, IOException, DPFPImageQualityException {
		JFrame painInTheAssFrame;
		painInTheAssFrame = new JFrame("");
		painInTheAssFrame.setResizable(false);
		painInTheAssFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	        @Override
	        public void windowClosing(java.awt.event.WindowEvent e) {
	            System.out.println("Uncomment following to open another window!");

	            e.getWindow().dispose();
	            System.out.println("JFrame Closed!");
	        }
	    });

		painInTheAssFrame.setBounds(100, 100, 650, 400);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setBackground(Color.WHITE);
		
				JLabel lblNewLabel_1 = new JLabel("CAPTURANDO HUELLA");
				contentPane.add(lblNewLabel_1, BorderLayout.NORTH);
				lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
				lblNewLabel_1.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		
		 lblNewLabel = new JLabel(new ImageIcon("./fingerLoad2.gif"));
		lblNewLabel.setBackground(Color.WHITE);
		contentPane.add(lblNewLabel, BorderLayout.CENTER);
		painInTheAssFrame.setContentPane(contentPane);
		
		lblNewLabel_2 = new JLabel("Levanta y apoya EL MISMO dedo varias veces sobre el lector. Huellas tomadas: "+count+" de 4");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		contentPane.add(lblNewLabel_2, BorderLayout.SOUTH);
		painInTheAssFrame.setLocationRelativeTo(null);
		painInTheAssFrame.setVisible(true);
		painInTheAssFrame.toFront();
		painInTheAssFrame.requestFocus();
		painInTheAssFrame.repaint();
		painInTheAssFrame.setAlwaysOnTop(true);
	}

}
