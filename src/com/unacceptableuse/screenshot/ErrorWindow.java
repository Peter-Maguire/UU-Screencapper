package com.unacceptableuse.screenshot;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JButton;

public class ErrorWindow {

	private JFrame frmAnErrorOccured;

	
	/**
	 * Create the application.
	 */
	public ErrorWindow(String errordesc, String errorbody) {
		initialize(errordesc, errorbody);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String errordesc, String errorbody) {
		frmAnErrorOccured = new JFrame();
		frmAnErrorOccured.setTitle("An Error Occurred");
		frmAnErrorOccured.setBounds(100, 100, 759, 535);
		frmAnErrorOccured.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblErrorSummary = new JLabel(errordesc);
		frmAnErrorOccured.getContentPane().add(lblErrorSummary, BorderLayout.NORTH);
		
		JTextArea txtrStackTrace = new JTextArea();
		txtrStackTrace.setText(errorbody);
		frmAnErrorOccured.getContentPane().add(txtrStackTrace, BorderLayout.CENTER);
		
		JButton btnClose = new JButton("Close");
		frmAnErrorOccured.getContentPane().add(btnClose, BorderLayout.SOUTH);
		btnClose.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(-1);
				
			}});
		frmAnErrorOccured.setVisible(true);
		
	}

	
}
